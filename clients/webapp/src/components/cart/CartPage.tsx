/**
 * Converted from: src/WebApp/Components/Pages/Cart.razor
 *
 * Shopping cart page.
 */
import { Link } from 'react-router-dom'
import { useCart } from '../../context/CartContext'

export default function CartPage() {
  const { items, removeItem, updateQuantity, total, clearCart } = useCart()

  if (items.length === 0) {
    return (
      <div className="text-center py-12">
        <h1 className="text-3xl font-bold mb-4">Your Cart is Empty</h1>
        <p className="text-gray-600 mb-8">Add some items to get started!</p>
        <Link
          to="/catalog"
          className="bg-primary text-white px-6 py-3 rounded hover:bg-opacity-90"
        >
          Browse Catalog
        </Link>
      </div>
    )
  }

  return (
    <div>
      <h1 className="text-3xl font-bold mb-8">Shopping Cart</h1>

      <div className="bg-white rounded-lg shadow-md overflow-hidden">
        <table className="w-full">
          <thead className="bg-gray-50">
            <tr>
              <th className="px-6 py-3 text-left">Product</th>
              <th className="px-6 py-3 text-center">Quantity</th>
              <th className="px-6 py-3 text-right">Price</th>
              <th className="px-6 py-3 text-right">Total</th>
              <th className="px-6 py-3"></th>
            </tr>
          </thead>
          <tbody className="divide-y">
            {items.map(item => (
              <tr key={item.id}>
                <td className="px-6 py-4">
                  <div className="flex items-center">
                    <img
                      src={item.pictureUrl}
                      alt={item.productName}
                      className="w-16 h-16 object-contain mr-4"
                      onError={(e) => {
                        (e.target as HTMLImageElement).src = 'https://via.placeholder.com/64?text=No+Image'
                      }}
                    />
                    <span className="font-medium">{item.productName}</span>
                  </div>
                </td>
                <td className="px-6 py-4">
                  <div className="flex items-center justify-center">
                    <button
                      onClick={() => updateQuantity(item.id, item.quantity - 1)}
                      className="px-2 py-1 bg-gray-200 rounded"
                    >
                      -
                    </button>
                    <span className="mx-4">{item.quantity}</span>
                    <button
                      onClick={() => updateQuantity(item.id, item.quantity + 1)}
                      className="px-2 py-1 bg-gray-200 rounded"
                    >
                      +
                    </button>
                  </div>
                </td>
                <td className="px-6 py-4 text-right">${item.unitPrice.toFixed(2)}</td>
                <td className="px-6 py-4 text-right font-bold">
                  ${(item.unitPrice * item.quantity).toFixed(2)}
                </td>
                <td className="px-6 py-4 text-right">
                  <button
                    onClick={() => removeItem(item.id)}
                    className="text-red-500 hover:text-red-700"
                  >
                    Remove
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <div className="mt-8 flex justify-between items-center">
        <button
          onClick={clearCart}
          className="text-red-500 hover:text-red-700"
        >
          Clear Cart
        </button>

        <div className="text-right">
          <p className="text-2xl font-bold mb-4">
            Total: ${total.toFixed(2)}
          </p>
          <Link
            to="/checkout"
            className="bg-primary text-white px-8 py-3 rounded hover:bg-opacity-90 inline-block"
          >
            Proceed to Checkout
          </Link>
        </div>
      </div>
    </div>
  )
}
