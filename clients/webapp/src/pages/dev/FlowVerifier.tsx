/**
 * End-to-end flow verifier — a development UI that walks the entire happy path
 * (health → catalog → basket → cardtypes → draft → order → idempotency replay →
 * status saga → webhook). Each step shows live status, timing, and the request/
 * response payload. Designed to be re-run any time backend code changes so you
 * can spot regressions visually without crawling docker logs.
 *
 * Mounted at /dev/flow (unlinked from main nav). Requires the user to be logged
 * in — the verifier reuses the existing OIDC session for the Bearer token.
 */
import { useEffect, useMemo, useRef, useState } from 'react'
import { useAuth } from 'react-oidc-context'
import client from '../../api/client'
import { useDocumentTitle } from '../../hooks/useDocumentTitle'
import { usePageHeader } from '../../components/layout/PageHeaderContext'

type StepStatus = 'pending' | 'running' | 'success' | 'failure' | 'skipped'

interface StepResult {
  name: string
  status: StepStatus
  durationMs?: number
  detail?: string         // one-line human summary
  payload?: unknown       // full request/response — shown when expanded
  error?: string
}

interface FlowContext {
  log: (i: number, patch: Partial<StepResult>) => void
  results: StepResult[]
  // shared between steps
  itemId?: number
  itemName?: string
  itemPrice?: number
  itemPicUrl?: string
  buyerId?: string
  userName?: string
  requestId?: string
  orderNumber?: number
}

// Same-origin health paths — nginx proxies each to the matching service's actuator
const HEALTH_TARGETS = [
  { name: 'catalog', url: '/health/catalog' },
  { name: 'ordering', url: '/health/ordering' },
  { name: 'basket', url: '/health/basket' },
  { name: 'webhooks-service', url: '/health/webhooks-service' },
  { name: 'order-processor', url: '/health/order-processor' },
  { name: 'payment-processor', url: '/health/payment-processor' },
  { name: 'webhooks-client', url: '/health/webhooks-client' },
  { name: 'mobile-bff', url: '/health/mobile-bff' },
] as const

const STEP_NAMES = [
  '1. Health check (all services)',
  '2. Auth — reuse OIDC session',
  '3. GET /api/catalog/items (anonymous, paginated)',
  '4. GET /api/orders/cardtypes',
  '5. POST /api/orders/draft (total preview)',
  '6. POST /api/basket (add 1 item)',
  '7. GET /api/basket/{buyerId} (verify saved)',
  '8. POST /api/orders (DDD: outbox + domain events + idempotency)',
  '9. Replay POST /api/orders with same x-requestid (idempotency)',
  '10. GET /api/orders (verify new order in list)',
  '11. Wait for status saga: Submitted → AwaitingValidation → StockConfirmed → Paid',
] as const

function nowMs() { return performance.now() }

function summariseError(err: unknown): { message: string; payload?: unknown } {
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const e = err as any
  if (e?.response) {
    return {
      message: `HTTP ${e.response.status} ${e.response.statusText} — ${e.config?.url ?? ''}`,
      payload: e.response.data,
    }
  }
  if (e?.message) return { message: e.message }
  return { message: String(err) }
}

export default function FlowVerifier() {
  useDocumentTitle('Flow verifier | AdventureWorks')
  usePageHeader('Flow verifier', 'End-to-end smoke test')
  const auth = useAuth()
  const [results, setResults] = useState<StepResult[]>(
    STEP_NAMES.map((n) => ({ name: n, status: 'pending' })),
  )
  const [running, setRunning] = useState(false)
  const [expanded, setExpanded] = useState<Record<number, boolean>>({})
  const ctx = useRef<FlowContext | null>(null)

  function reset() {
    setResults(STEP_NAMES.map((n) => ({ name: n, status: 'pending' })))
    setExpanded({})
  }

  function log(i: number, patch: Partial<StepResult>) {
    setResults((prev) => {
      const next = prev.slice()
      next[i] = { ...next[i], ...patch }
      return next
    })
  }

  async function timeStep<T>(i: number, label: string, body: () => Promise<T>): Promise<T | null> {
    log(i, { status: 'running', detail: undefined, payload: undefined, error: undefined })
    const t0 = nowMs()
    try {
      const out = await body()
      log(i, { status: 'success', durationMs: Math.round(nowMs() - t0), detail: label })
      return out
    } catch (err) {
      const { message, payload } = summariseError(err)
      log(i, {
        status: 'failure',
        durationMs: Math.round(nowMs() - t0),
        error: message,
        payload,
      })
      throw err
    }
  }

  async function run() {
    if (running) return
    setRunning(true)
    reset()
    ctx.current = {
      log,
      results,
    }

    try {
      // 1 — health
      await timeStep(0, '', async () => {
        const checks = await Promise.all(
          HEALTH_TARGETS.map(async (t) => {
            try {
              const r = await fetch(t.url, { method: 'GET' })
              return { name: t.name, ok: r.ok, status: r.status }
            } catch (e) {
              return { name: t.name, ok: false, status: 0, error: String(e) }
            }
          }),
        )
        const failed = checks.filter((c) => !c.ok)
        const okCount = checks.length - failed.length
        const label = failed.length === 0
          ? `${okCount}/${checks.length} services healthy`
          : `${okCount}/${checks.length} healthy — failed: ${failed.map((f) => f.name).join(', ')}`
        log(0, { detail: label, payload: checks })
        if (failed.length > 0) throw new Error('Health check failed')
      })

      // 2 — auth check (no fetch; just confirm we have a token)
      await timeStep(1, '', async () => {
        if (!auth.isAuthenticated || !auth.user?.access_token) {
          throw new Error('Not authenticated. Click "Log in" in the header first, then re-run.')
        }
        const sub = (auth.user.profile.sub as string) || ''
        const name = (auth.user.profile.name as string) || (auth.user.profile.preferred_username as string) || 'unknown'
        ctx.current!.buyerId = sub
        ctx.current!.userName = name
        log(1, {
          detail: `Logged in as ${name} (sub=${sub.slice(0, 8)}…)`,
          payload: { sub, name, scope: auth.user.scope, expires_at: auth.user.expires_at },
        })
      })

      // 3 — catalog list
      await timeStep(2, '', async () => {
        const { data } = await client.get('/api/catalog/items', { params: { pageIndex: 0, pageSize: 1 } })
        const item = data.data?.[0]
        if (!item) throw new Error('Catalog returned no items')
        ctx.current!.itemId = item.id
        ctx.current!.itemName = item.name
        ctx.current!.itemPrice = item.price
        ctx.current!.itemPicUrl = `/api/catalog/items/${item.id}/pic?api-version=1.0`
        log(2, {
          detail: `count=${data.count}, first: id=${item.id} "${item.name}" ($${item.price.toFixed(2)})`,
          payload: data,
        })
      })

      // 4 — card types
      await timeStep(3, '', async () => {
        const { data } = await client.get('/api/orders/cardtypes')
        log(3, {
          detail: `${data.length} card types: ${data.map((c: { name: string }) => c.name).join(', ')}`,
          payload: data,
        })
      })

      // 5 — draft
      await timeStep(4, '', async () => {
        const draft = await client.post('/api/orders/draft', {
          buyerId: ctx.current!.buyerId,
          items: [{
            productId: ctx.current!.itemId,
            productName: ctx.current!.itemName,
            unitPrice: ctx.current!.itemPrice,
            discount: 0,
            quantity: 2,
            pictureUrl: ctx.current!.itemPicUrl,
          }],
        })
        log(4, {
          detail: `Computed total $${draft.data.total} for ${draft.data.orderItems.length} item(s)`,
          payload: draft.data,
        })
      })

      // 6 — add to basket
      await timeStep(5, '', async () => {
        const { data } = await client.post('/api/basket', {
          buyerId: ctx.current!.buyerId,
          items: [{
            id: crypto.randomUUID(),
            productId: ctx.current!.itemId,
            productName: ctx.current!.itemName,
            unitPrice: ctx.current!.itemPrice,
            quantity: 2,
            pictureUrl: ctx.current!.itemPicUrl,
          }],
        })
        log(5, { detail: `Basket saved with ${data.items?.length ?? 0} item(s)`, payload: data })
      })

      // 7 — verify basket
      await timeStep(6, '', async () => {
        const { data } = await client.get(`/api/basket/${ctx.current!.buyerId}`)
        if (!data.items || data.items.length === 0) throw new Error('Basket round-trip lost the items')
        log(6, { detail: `${data.items.length} item(s) round-tripped`, payload: data })
      })

      // 8 — create order
      ctx.current!.requestId = crypto.randomUUID()
      await timeStep(7, '', async () => {
        const oneYr = new Date()
        oneYr.setUTCFullYear(oneYr.getUTCFullYear() + 1)
        const profile = auth.user!.profile as Record<string, unknown>
        const payload = {
          userId: ctx.current!.buyerId,
          userName: ctx.current!.userName,
          buyer: ctx.current!.buyerId,
          city: (profile.address_city as string) || 'Redmond',
          street: (profile.address_street as string) || '15703 NE 61st Ct',
          state: (profile.address_state as string) || 'WA',
          country: (profile.address_country as string) || 'USA',
          zipCode: (profile.address_zip_code as string) || '98052',
          cardNumber: '4012888888881881',
          cardHolderName: ctx.current!.userName,
          cardExpiration: oneYr.toISOString(),
          cardSecurityNumber: '123',
          cardTypeId: 1,
          items: [{
            id: crypto.randomUUID(),
            productId: ctx.current!.itemId,
            productName: ctx.current!.itemName,
            unitPrice: ctx.current!.itemPrice,
            oldUnitPrice: 0,
            quantity: 2,
            pictureUrl: ctx.current!.itemPicUrl,
          }],
        }
        const res = await client.post('/api/orders', payload, {
          headers: { 'x-requestid': ctx.current!.requestId },
        })
        log(7, {
          detail: `HTTP ${res.status} — x-requestid: ${ctx.current!.requestId}`,
          payload: { request: payload, response_status: res.status, response_body: res.data },
        })
      })

      // 9 — idempotency replay (same x-requestid)
      await timeStep(8, '', async () => {
        const listBefore = await client.get('/api/orders')
        const maxBefore = listBefore.data[0]?.orderNumber ?? 0
        const res = await client.post('/api/orders', { /* body can be anything — idempotent path short-circuits */ }, {
          headers: { 'x-requestid': ctx.current!.requestId! },
        })
        const listAfter = await client.get('/api/orders')
        const maxAfter = listAfter.data[0]?.orderNumber ?? 0
        const noDup = maxBefore === maxAfter
        if (!noDup) throw new Error(`Idempotency broken — replay created a new order (max before=${maxBefore}, after=${maxAfter})`)
        log(8, {
          detail: `HTTP ${res.status}, max order id unchanged (${maxBefore}) — no duplicate created ✓`,
          payload: { response_status: res.status, max_order_id_before: maxBefore, max_order_id_after: maxAfter },
        })
      })

      // 10 — find the order
      await timeStep(9, '', async () => {
        const { data } = await client.get('/api/orders')
        if (!data || data.length === 0) throw new Error('Order list is empty after create')
        // newest order is our order — pick top of list (sorted desc)
        ctx.current!.orderNumber = data[0].orderNumber
        log(9, {
          detail: `New order #${data[0].orderNumber} present at top of list (status: ${data[0].status})`,
          payload: data.slice(0, 5),
        })
      })

      // 11 — poll status until Paid (or timeout)
      const orderNumber = ctx.current!.orderNumber!
      const t0 = nowMs()
      const seen = new Set<string>()
      const TIMEOUT_MS = 120_000   // 2 minutes
      const POLL_MS = 3_000
      log(10, {
        status: 'running',
        detail: `Polling /api/orders for status of #${orderNumber}…`,
      })
      const timeline: Array<{ status: string; tSec: number }> = []
      try {
        // eslint-disable-next-line no-constant-condition
        while (true) {
          const { data } = await client.get('/api/orders')
          const me = data.find((o: { orderNumber: number }) => o.orderNumber === orderNumber)
          const status = me?.status ?? '?'
          if (!seen.has(status)) {
            seen.add(status)
            timeline.push({ status, tSec: Math.round((nowMs() - t0) / 100) / 10 })
            log(10, {
              status: 'running',
              detail: `→ ${Array.from(seen).join(' → ')}`,
              payload: { timeline },
            })
          }
          if (status === 'Paid') {
            log(10, {
              status: 'success',
              durationMs: Math.round(nowMs() - t0),
              detail: `Saga complete: ${timeline.map((t) => t.status).join(' → ')}`,
              payload: { timeline },
            })
            break
          }
          if (status === 'Cancelled') throw new Error('Saga ended in Cancelled')
          if (nowMs() - t0 > TIMEOUT_MS) throw new Error(`Timed out after ${TIMEOUT_MS / 1000}s waiting for Paid (last status: ${status})`)
          await new Promise((r) => setTimeout(r, POLL_MS))
        }
      } catch (err) {
        const { message, payload } = summariseError(err)
        log(10, { status: 'failure', durationMs: Math.round(nowMs() - t0), error: message, payload: payload ?? { timeline } })
        throw err
      }
    } catch {
      // step already logged the failure — mark remaining as skipped
      setResults((prev) =>
        prev.map((r) => (r.status === 'pending' ? { ...r, status: 'skipped' as StepStatus } : r)),
      )
    } finally {
      setRunning(false)
    }
  }

  const summary = useMemo(() => {
    const total = results.length
    const ok = results.filter((r) => r.status === 'success').length
    const fail = results.filter((r) => r.status === 'failure').length
    const skipped = results.filter((r) => r.status === 'skipped').length
    return { total, ok, fail, skipped }
  }, [results])

  useEffect(() => {
    // when the user logs out and back in, reset so it doesn't look stale
    if (!auth.isAuthenticated) reset()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [auth.isAuthenticated])

  return (
    <div className="max-w-4xl">
      <div className="flex items-center gap-3 mb-6">
        <button
          onClick={run}
          disabled={running}
          className="btn-brand"
        >
          {running ? 'Running…' : '▶ Run all'}
        </button>
        <button
          onClick={reset}
          disabled={running}
          className="text-sm border border-black px-4 py-2 rounded hover:bg-gray-100 disabled:opacity-50"
        >
          Reset
        </button>
        <div className="ml-auto text-sm text-gray-700">
          ✓ {summary.ok} &nbsp; ✗ {summary.fail} &nbsp; ⊘ {summary.skipped} / {summary.total}
        </div>
      </div>

      {!auth.isAuthenticated && (
        <div className="bg-yellow-50 border border-yellow-300 p-3 rounded mb-6 text-sm">
          You're not logged in. Click <strong>Log in</strong> in the header (alice / Pass123$), then come back here.
        </div>
      )}

      <div className="space-y-2">
        {results.map((r, i) => (
          <StepCard
            key={i}
            step={r}
            expanded={!!expanded[i]}
            onToggle={() => setExpanded((p) => ({ ...p, [i]: !p[i] }))}
          />
        ))}
      </div>
    </div>
  )
}

function StepCard({ step, expanded, onToggle }: { step: StepResult; expanded: boolean; onToggle: () => void }) {
  const STATUS_ICON: Record<StepStatus, string> = {
    pending: '○',
    running: '⏳',
    success: '✓',
    failure: '✗',
    skipped: '⊘',
  }
  const STATUS_COLOR: Record<StepStatus, string> = {
    pending: 'border-gray-300 text-gray-600',
    running: 'border-blue-400 text-blue-700 bg-blue-50',
    success: 'border-green-400 text-green-800 bg-green-50',
    failure: 'border-red-400 text-red-800 bg-red-50',
    skipped: 'border-gray-300 text-gray-400',
  }

  return (
    <div className={`border-l-4 rounded ${STATUS_COLOR[step.status]} bg-white shadow-sm`}>
      <button
        onClick={onToggle}
        className="w-full text-left px-4 py-3 flex items-start gap-3"
      >
        <span className="text-xl leading-none">{STATUS_ICON[step.status]}</span>
        <div className="flex-1 min-w-0">
          <div className="font-semibold">{step.name}</div>
          {step.detail && <div className="text-sm text-gray-700 mt-0.5 break-words">{step.detail}</div>}
          {step.error && <div className="text-sm text-red-700 mt-0.5 font-mono break-words">{step.error}</div>}
        </div>
        <div className="text-xs text-gray-500 whitespace-nowrap shrink-0">
          {step.durationMs != null && `${step.durationMs} ms`}
        </div>
      </button>
      {expanded && step.payload != null && (
        <pre className="text-xs bg-gray-900 text-green-200 p-3 mx-1 mb-2 rounded overflow-x-auto whitespace-pre-wrap">
{JSON.stringify(step.payload, null, 2)}
        </pre>
      )}
    </div>
  )
}
