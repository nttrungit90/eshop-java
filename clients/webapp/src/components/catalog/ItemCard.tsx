/**
 * Converted from: src/WebApp/Components/CatalogListItem.razor
 *
 * Individual catalog item card.
 */
import { CatalogItem } from '../../types'
import { useCart } from '../../context/CartContext'
import { catalogApi } from '../../api/catalogApi'

interface ItemCardProps {
  item: CatalogItem
}

export default function ItemCard({ item }: ItemCardProps) {
  const { addItem } = useCart()
  const picUrl = catalogApi.pictureUrl(item)

  function handleAddToCart() {
    addItem({
      id: '',
      productId: item.id,
      productName: item.name,
      unitPrice: item.price,
      quantity: 1,
      pictureUrl: picUrl,
    })
  }

  return (
    <div className="bg-white rounded-lg shadow-md overflow-hidden hover:shadow-lg transition-shadow">
      <div className="h-48 bg-gray-100 flex items-center justify-center">
        <img
          src={picUrl}
          alt={item.name}
          className="max-h-full max-w-full object-contain"
          onError={(e) => {
            (e.target as HTMLImageElement).src = 'https://via.placeholder.com/200?text=No+Image'
          }}
        />
      </div>
      <div className="p-4">
        <h3 className="font-semibold text-lg mb-2 truncate" title={item.name}>
          {item.name}
        </h3>
        <p className="text-gray-600 text-sm mb-2 line-clamp-2">
          {item.description}
        </p>
        <div className="flex items-center justify-between">
          <span className="text-xl font-bold text-primary">
            ${item.price.toFixed(2)}
          </span>
          <button
            onClick={handleAddToCart}
            disabled={item.availableStock === 0}
            className="bg-primary text-white px-4 py-2 rounded hover:bg-opacity-90 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {item.availableStock === 0 ? 'Out of Stock' : 'Add to Cart'}
          </button>
        </div>
        {item.availableStock > 0 && item.availableStock < 10 && (
          <p className="text-orange-500 text-sm mt-2">
            Only {item.availableStock} left in stock!
          </p>
        )}
      </div>
    </div>
  )
}
