/**
 * Converted from: src/WebApp/Components/Pages/Catalog.razor
 *
 * Catalog page displaying products.
 */
import { useState, useEffect } from 'react'
import { catalogApi } from '../../api/catalogApi'
import { CatalogItem, CatalogBrand, CatalogType } from '../../types'
import ItemCard from './ItemCard'
import Hero from '../layout/Hero'
import { useDocumentTitle } from '../../hooks/useDocumentTitle'

const PAGE_SIZE = 12

export default function CatalogPage() {
  useDocumentTitle('AdventureWorks')
  const [items, setItems] = useState<CatalogItem[]>([])
  // brands/types are loaded so filtering can be added later — not used in UI yet
  const [, setBrands] = useState<CatalogBrand[]>([])
  const [, setTypes] = useState<CatalogType[]>([])
  const [loading, setLoading] = useState(true)
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)

  useEffect(() => {
    loadData()
  }, [page])

  async function loadData() {
    setLoading(true)
    try {
      const [itemsResponse, brandsData, typesData] = await Promise.all([
        catalogApi.getItems(page, PAGE_SIZE),
        catalogApi.getCatalogBrands(),
        catalogApi.getCatalogTypes(),
      ])
      setItems(itemsResponse.data || [])
      setTotalPages(Math.max(1, Math.ceil((itemsResponse.count || 0) / (itemsResponse.pageSize || PAGE_SIZE))))
      setBrands(brandsData)
      setTypes(typesData)
    } catch (error) {
      console.error('Failed to load catalog', error)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div>
      <Hero />

      {loading ? (
        <div className="flex items-center justify-center h-64">
          <div className="text-lg">Loading catalog…</div>
        </div>
      ) : (
        <>
      <h2 className="text-2xl font-bold mb-6">Featured products</h2>

      <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6">
        {items.map(item => (
          <ItemCard key={item.id} item={item} />
        ))}
      </div>

      {items.length === 0 && (
        <div className="text-center text-gray-500 py-12">
          No products found
        </div>
      )}

      {totalPages > 1 && (
        <div className="flex justify-center mt-8 space-x-2">
          <button
            onClick={() => setPage(p => Math.max(0, p - 1))}
            disabled={page === 0}
            className="px-4 py-2 bg-primary text-white rounded disabled:opacity-50"
          >
            Previous
          </button>
          <span className="px-4 py-2">
            Page {page + 1} of {totalPages}
          </span>
          <button
            onClick={() => setPage(p => Math.min(totalPages - 1, p + 1))}
            disabled={page >= totalPages - 1}
            className="px-4 py-2 bg-primary text-white rounded disabled:opacity-50"
          >
            Next
          </button>
        </div>
      )}
        </>
      )}
    </div>
  )
}
