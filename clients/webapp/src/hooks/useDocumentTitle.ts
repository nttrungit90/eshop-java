/**
 * Small hook that sets document.title for the current page.
 * Mirrors Blazor's <PageTitle>…</PageTitle> per-page directive.
 */
import { useEffect } from 'react'

export function useDocumentTitle(title: string) {
  useEffect(() => {
    document.title = title
  }, [title])
}
