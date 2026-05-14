/**
 * Catalog landing page — mirrors .NET WebApp/Components/Pages/Catalog/Catalog.razor.
 *
 * Layout: Hero banner across the top, then a sidebar with Brand/Type filter
 * chips on the left and a paginated product grid on the right. Filter
 * selections live in the URL (?brand= / ?type=) — same pattern Blazor uses
 * with SupplyParameterFromQuery.
 */
import { useEffect, useState } from 'react'
import { useSearchParams, Link } from 'react-router-dom'
import { catalogApi } from '../../api/catalogApi'
import { CatalogItem, CatalogBrand, CatalogType } from '../../types'
import ItemCard from './ItemCard'
import CatalogSearch from './CatalogSearch'
import { useDocumentTitle } from '../../hooks/useDocumentTitle'
import { usePageHeader } from '../layout/PageHeaderContext'

const PAGE_SIZE = 12

export default function CatalogPage() {
  useDocumentTitle('AdventureWorks')
  usePageHeader('Ready for a new adventure?', 'Start the season with the latest in clothing and equipment.')
  const [params] = useSearchParams()

  const brandId = params.get('brand') ? parseInt(params.get('brand')!, 10) : null
  const typeId = params.get('type') ? parseInt(params.get('type')!, 10) : null
  const page = params.get('page') ? Math.max(0, parseInt(params.get('page')!, 10) - 1) : 0

  const [items, setItems] = useState<CatalogItem[]>([])
  const [brands, setBrands] = useState<CatalogBrand[]>([])
  const [types, setTypes] = useState<CatalogType[]>([])
  const [loading, setLoading] = useState(true)
  const [count, setCount] = useState(0)

  // Brands + types only need to load once
  useEffect(() => {
    Promise.all([catalogApi.getCatalogBrands(), catalogApi.getCatalogTypes()])
      .then(([b, t]) => {
        setBrands(b)
        setTypes(t)
      })
      .catch((err) => console.error('Failed to load brands/types', err))
  }, [])

  // Items reload whenever brand/type/page changes
  useEffect(() => {
    setLoading(true)
    catalogApi
      .getItems(page, PAGE_SIZE, brandId, typeId)
      .then((res) => {
        setItems(res.data || [])
        setCount(res.count || 0)
      })
      .catch((err) => console.error('Failed to load catalog', err))
      .finally(() => setLoading(false))
  }, [brandId, typeId, page])

  const totalPages = Math.max(1, Math.ceil(count / PAGE_SIZE))

  function pageUri(targetPage: number): string {
    const next = new URLSearchParams(params)
    if (targetPage <= 0) next.delete('page')
    else next.set('page', String(targetPage + 1))
    const qs = next.toString()
    return qs ? `?${qs}` : '/catalog'
  }

  return (
    <div>
      <div className="flex flex-col md:flex-row gap-8">
        <CatalogSearch brands={brands} types={types} brandId={brandId} typeId={typeId} />

        <section className="flex-1 min-w-0">
          {loading ? (
            <div className="flex items-center justify-center h-64">
              <div className="text-lg">Loading catalog…</div>
            </div>
          ) : (
            <>
              <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {items.map((item) => (
                  <ItemCard key={item.id} item={item} />
                ))}
              </div>

              {items.length === 0 && (
                <div className="text-center text-gray-500 py-12">No products found</div>
              )}

              {totalPages > 1 && (
                <div className="flex justify-center items-center mt-8 gap-2 flex-wrap">
                  {Array.from({ length: totalPages }).map((_, i) => (
                    <Link
                      key={i}
                      to={pageUri(i)}
                      className={
                        i === page
                          ? 'px-3 py-1.5 rounded bg-primary text-white text-sm font-semibold'
                          : 'px-3 py-1.5 rounded bg-gray-100 text-gray-800 hover:bg-gray-200 text-sm'
                      }
                    >
                      {i + 1}
                    </Link>
                  ))}
                </div>
              )}
            </>
          )}
        </section>
      </div>
    </div>
  )
}
