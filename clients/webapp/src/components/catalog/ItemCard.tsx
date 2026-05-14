/**
 * Catalog grid card — matches Blazor CatalogListItem.razor.
 *
 * The whole card is a link to /item/{id}; there's no inline Add-to-Cart button
 * (that lives on the detail page). This mirrors the .NET reference UX where
 * clicking a product opens its detail page, then "Log in to purchase" or
 * "Add to shopping bag" is the next action.
 */
import { Link } from 'react-router-dom'
import { CatalogItem } from '../../types'
import { catalogApi } from '../../api/catalogApi'

interface ItemCardProps {
  item: CatalogItem
}

export default function ItemCard({ item }: ItemCardProps) {
  const picUrl = catalogApi.pictureUrl(item)

  return (
    <Link
      to={`/item/${item.id}`}
      className="block bg-white rounded-lg shadow-md overflow-hidden hover:shadow-xl hover:-translate-y-0.5 transition-all"
    >
      <div className="h-48 bg-white flex items-center justify-center p-4">
        <img
          src={picUrl}
          alt={item.name}
          className="max-h-full max-w-full object-contain"
          onError={(e) => {
            ;(e.target as HTMLImageElement).style.opacity = '0.2'
          }}
        />
      </div>
      <div className="px-4 pb-4 flex items-center gap-2">
        <span className="font-semibold text-base truncate flex-1" title={item.name}>
          {item.name}
        </span>
        <span className="text-base font-semibold text-gray-700 whitespace-nowrap">
          ${item.price.toFixed(2)}
        </span>
      </div>
      {item.availableStock === 0 && (
        <p className="px-4 pb-3 text-red-600 text-xs font-medium">Out of stock</p>
      )}
    </Link>
  )
}
