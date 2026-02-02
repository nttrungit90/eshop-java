/**
 * Converted from: src/WebApp/Components/Pages/Catalog.razor
 *
 * Catalog page displaying products.
 */
import { useState, useEffect } from 'react'
import { catalogApi } from '../../api/catalogApi'
import { CatalogItem, CatalogBrand, CatalogType } from '../../types'
import ItemCard from './ItemCard'

export default function CatalogPage() {
  const [items, setItems] = useState<CatalogItem[]>([])
  const [brands, setBrands] = useState<CatalogBrand[]>([])
  const [types, setTypes] = useState<CatalogType[]>([])
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
        catalogApi.getItems(page, 12),
        catalogApi.getCatalogBrands(),
        catalogApi.getCatalogTypes()
      ])
      setItems(itemsResponse.content || [])
      setTotalPages(itemsResponse.totalPages || 1)
      setBrands(brandsData)
      setTypes(typesData)
    } catch (error) {
      console.error('Failed to load catalog', error)
    } finally {
      setLoading(false)
    }
  }

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="text-lg">Loading catalog...</div>
      </div>
    )
  }

  return (
    <div>
      <h1 className="text-3xl font-bold mb-8">Catalog</h1>

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
    </div>
  )
}
