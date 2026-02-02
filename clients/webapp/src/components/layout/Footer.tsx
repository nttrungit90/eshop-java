/**
 * Converted from: src/WebApp/Components/Layout/FooterBar.razor
 *
 * Application footer.
 */
export default function Footer() {
  return (
    <footer className="bg-gray-800 text-white py-8">
      <div className="container mx-auto px-4">
        <div className="flex flex-col md:flex-row justify-between items-center">
          <div className="mb-4 md:mb-0">
            <span className="text-xl font-bold">eShop</span>
            <p className="text-gray-400 text-sm mt-1">
              Java/Spring Boot conversion of .NET eShop
            </p>
          </div>
          <div className="text-gray-400 text-sm">
            &copy; {new Date().getFullYear()} eShop Reference Application
          </div>
        </div>
      </div>
    </footer>
  )
}
