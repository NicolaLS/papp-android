# Layered Model Control Flow

Control Flow follows a layered flow, where the UI (Components/ViewModels) call business logic in Domain.
`UI --> Domain --> Data`

Business logic is in the ViewModel and Use-Cases.

# Dependency Direction

Dependency direction follows a "plugin" approach:
`UI --> Domain <-- Data`

The Domain defines/owns interfaces for the data layer so that dependency is inverted. You can
find `WalletRepository` and `WalletRepositoryImpl` which is an interface owned by domain, and
an implementation owned by data respectively.

# Connected Wallets (Clients/DTO's)

Architecture gets a bit weird because users can connect different wallets to the app. They all have
different API client implementations and use their own DTO's. Because of this we can't return the DTO
from  `WalletApi` functions but have to define another interface for each type that we'll need in `domain/model`.

Usually we'd return a DTO per client method (e.g `payBolt11Invoice`) but we can't return `PaymentSendResponse`
immediately so we return `IntoPaymentSendResponse` instead. So for every type in `domain/model` we'll define an
additional interface that can be its substitue.
