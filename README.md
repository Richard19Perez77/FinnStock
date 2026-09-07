# FinnStock

Jetpack Compose market dashboard backed by Finnhub's free REST API.

The home screen has a horizontal ticker banner (TSM, AAPL, NVDA, GOOGL, MSFT, AMZN) and a market-news list underneath. Pull to refresh reloads both sections independently; a shuffle toggle reorders headlines.

## Stack

Kotlin, Jetpack Compose, Hilt, Retrofit, MVI (contract / reducer / ViewModel), Clean Architecture layers under `feature/stocks`.

See [ARCHITECTURE.md](ARCHITECTURE.md) for data flow and how a details screen would plug in.

## Run

Open the project in Android Studio and run the `app` configuration.

```bash
./gradlew :app:assembleDebug
```
