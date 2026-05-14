/**
 * Catalog sidebar filter — ports .NET WebAppComponents/Catalog/CatalogSearch.razor.
 *
 * Renders "All" + a chip per brand and per type. Selecting a chip mutates
 * the URL query string (?brand= / ?type=), which the parent CatalogPage
 * watches to reload the filtered grid.
 */
import { useSearchParams, Link } from 'react-router-dom'
import { CatalogBrand, CatalogType } from '../../types'

interface Props {
  brands: CatalogBrand[]
  types: CatalogType[]
  brandId: number | null
  typeId: number | null
}

export default function CatalogSearch({ brands, types, brandId, typeId }: Props) {
  const [params] = useSearchParams()

  function withParam(key: 'brand' | 'type', value: number | null): string {
    const next = new URLSearchParams(params)
    next.delete('page')   // reset paging when the filter changes
    if (value == null) next.delete(key)
    else next.set(key, String(value))
    const qs = next.toString()
    return qs ? `?${qs}` : ''
  }

  return (
    <aside className="w-full md:w-56 md:shrink-0">
      <div className="flex items-center gap-2 mb-4">
        <img src="/icons/filters.svg" alt="" className="h-5 w-5" />
        <span className="font-semibold">Filters</span>
      </div>

      <Group title="Brand">
        <Chip to={withParam('brand', null)} active={brandId == null}>All</Chip>
        {brands.map((b) => (
          <Chip key={b.id} to={withParam('brand', b.id)} active={brandId === b.id}>
            {b.brand}
          </Chip>
        ))}
      </Group>

      <Group title="Type">
        <Chip to={withParam('type', null)} active={typeId == null}>All</Chip>
        {types.map((t) => (
          <Chip key={t.id} to={withParam('type', t.id)} active={typeId === t.id}>
            {t.type}
          </Chip>
        ))}
      </Group>
    </aside>
  )
}

function Group({ title, children }: { title: string; children: React.ReactNode }) {
  return (
    <div className="mb-6">
      <h3 className="text-sm font-semibold mb-2">{title}</h3>
      <div className="flex flex-wrap gap-1 pt-3 border-t border-gray-700">
        {children}
      </div>
    </div>
  )
}

function Chip({ to, active, children }: { to: string; active: boolean; children: React.ReactNode }) {
  return (
    <Link
      to={to}
      className={
        active
          ? 'inline-flex items-center px-3 py-1.5 rounded-full text-sm bg-primary text-white'
          : 'inline-flex items-center px-3 py-1.5 rounded-full text-sm bg-gray-100 text-gray-800 hover:bg-gray-200'
      }
    >
      {children}
    </Link>
  )
}
