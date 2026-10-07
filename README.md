# BizPalm Mobile Inventory and POS System

BizPalm is an Android-based Point of Sale and Inventory Management System built for small to medium-sized businesses, retail stores, sari-sari stores, pharmacies, and general merchandise shops. It operates entirely offline on any phone or tablet, requiring no expensive hardware or stable internet connection.

---

## App Screenshots

### Login Screen
![Login Screen](https://raw.githubusercontent.com/Chie03-dev/BizPalm-Mobile/main/assets/screenshots/login.png)

### Registration Screen
![Registration Screen](https://raw.githubusercontent.com/Chie03-dev/BizPalm-Mobile/main/assets/screenshots/register.png)

---

## Core Features

- **Barcode Scanning**: Scan products instantly using the device camera to add items to the sale without manual typing.
- **Inventory Management**: Track stock levels, pricing, cost, and product categories with automated low stock alerts.
- **Sales and Transactions**: Record sales transactions, calculate change automatically, and support cash, online payments, and customer loans.
- **Analytics Dashboard**: View revenue trends, sales summaries, and predictive analytics powered by linear regression.
- **Business Health Alerts**: Detect sales spikes and demand drops using statistical Z-score analysis.
- **Restock Recommendations**: Predict when products will run out and calculate reorder quantities based on sales velocity.
- **PDF Receipts and Reports**: Generate itemized receipts and sales summaries for printing or sharing.
- **Fully Offline Architecture**: All data resides securely on the device database with no reliance on cloud servers.

---

## Technology Stack

- **Languages**: Kotlin and Java
- **Platform**: Android (API Level 26 and above)
- **Architecture**: MVVM (Model-View-ViewModel) pattern
- **Local Database**: Room Persistence Library (SQLite)
- **Barcode Recognition**: Google ML Kit Barcode Scanning
- **Camera Handling**: CameraX API
- **Statistical Engine**: Apache Commons Math (linear regression and descriptive statistics)
- **Data Visualization**: MPAndroidChart
- **PDF Generation**: iText Core

---

## System Architecture

BizPalm follows a local-first, offline-first design pattern. 

1. **Data Layer**: Room database entities and data access objects (DAOs) manage local persistence. Repositories act as the single source of truth between the database and the UI.
2. **ViewModel Layer**: ViewModels manage UI state and execute background computations using Kotlin coroutines.
3. **UI Layer**: Activities and fragments observe view model states and render modern Material Design components.

---

## How to Run

1. Clone the repository:
   ```bash
   git clone https://github.com/Chie03-dev/BizPalm-Mobile.git
   ```
2. Open the project in Android Studio.
3. Allow Gradle to sync and download dependencies.
4. Connect an Android device or start an emulator running Android 8.0 (API 26) or higher.
5. Click Run to build and install the application.

---

## Contact

- **Developer**: Alchie O. Andilab
- **Email**: alchieandilab2003@gmail.com
- **GitHub**: [@Chie03-dev](https://github.com/Chie03-dev)
