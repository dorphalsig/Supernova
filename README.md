# Supernova – Android TV App Skeleton (regen)

Changes in this regen:
- Removed RepoImpl and decoupled Providers from :data
- Wired Application class in AndroidManifest (android:name=".App")
- Compose plugin manages compiler extension; no explicit composeOptions

Modules:
- :domain — interfaces
- :data — Room stubs (can wire later)
- :network — OkHttp scaffold
- :sync — WorkManager worker placeholder
- :app — Compose TV shell & composition root (Providers with in-memory repo)
