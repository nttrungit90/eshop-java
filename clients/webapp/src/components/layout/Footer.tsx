/**
 * Site footer — matches the .NET Blazor FooterBar with the small AdventureWorks
 * mark and a tagline. The Java migration of dotnet/eShop runs the same scenarios.
 */
export default function Footer() {
  return (
    <footer className="bg-primary text-white py-8 mt-12">
      <div className="container mx-auto px-4 flex flex-col md:flex-row justify-between items-center gap-4">
        <div className="flex items-center gap-3">
          <img src="/images/logo-footer.svg" alt="AdventureWorks" className="h-8" />
          <span className="text-sm opacity-80">
            Built on Java / Spring Boot — a faithful migration of <code>dotnet/eShop</code>
          </span>
        </div>
        <div className="text-sm opacity-70">
          &copy; {new Date().getFullYear()} AdventureWorks Reference App
        </div>
      </div>
    </footer>
  )
}
