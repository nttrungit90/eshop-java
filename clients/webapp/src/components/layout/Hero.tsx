/**
 * Hero banner shown on the catalog page. Matches the .NET Blazor
 * "Ready for a new adventure?" header section, using the same imagery.
 */
export default function Hero() {
  return (
    <section className="relative overflow-hidden mb-8 rounded-lg shadow-md">
      <img
        src="/images/header-home.webp"
        alt=""
        className="w-full h-64 object-cover"
      />
      <div className="absolute inset-0 bg-gradient-to-r from-primary/80 to-primary/20 flex items-center">
        <div className="px-8 text-white max-w-xl">
          <h1 className="text-4xl md:text-5xl font-extrabold mb-3">
            Ready for a new adventure?
          </h1>
          <p className="text-lg opacity-90">
            Start the season with the latest in clothing and equipment.
          </p>
        </div>
      </div>
    </section>
  )
}
