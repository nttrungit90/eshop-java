/**
 * Cross-page state for the site header's title + subtitle text.
 *
 * Mirrors Blazor's <SectionContent SectionName="page-header-title">…</SectionContent>
 * pattern: each page declares its title/subtitle via the usePageHeader hook,
 * the global SiteHeader reads them from this context.
 */
import { createContext, useContext, useEffect, useState, ReactNode } from 'react'

interface PageHeaderState {
  title: string
  subtitle: string
}

interface PageHeaderApi extends PageHeaderState {
  setHeader: (state: PageHeaderState) => void
}

const PageHeaderContext = createContext<PageHeaderApi | undefined>(undefined)

export function PageHeaderProvider({ children }: { children: ReactNode }) {
  const [state, setState] = useState<PageHeaderState>({ title: '', subtitle: '' })
  return (
    <PageHeaderContext.Provider
      value={{ ...state, setHeader: (s) => setState(s) }}
    >
      {children}
    </PageHeaderContext.Provider>
  )
}

export function usePageHeader(title: string, subtitle = '') {
  const ctx = useContext(PageHeaderContext)
  useEffect(() => {
    ctx?.setHeader({ title, subtitle })
  }, [title, subtitle, ctx])
}

export function usePageHeaderState(): PageHeaderState {
  const ctx = useContext(PageHeaderContext)
  return { title: ctx?.title ?? '', subtitle: ctx?.subtitle ?? '' }
}
