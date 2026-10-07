# Mobile Inventory and POS System

BizPalm is a production-grade, local-first Android Point of Sale (POS) and Inventory Management System engineered for small to medium-sized retail shops, sari-sari stores, pharmacies, and general merchandise businesses. It operates 100% offline on mobile devices, ensuring zero latency, absolute data privacy, and complete independence from unstable internet connections or expensive cloud infrastructure.

---

## App Gallery

<p align="center">
  <img src="https://raw.githubusercontent.com/Chie03-dev/BizPalm-Mobile/main/assets/screenshots/login.png" width="300" alt="Login Screen">
  <br>
  <b>1. Login Screen</b>
</p>

<p align="center">
  <img src="https://raw.githubusercontent.com/Chie03-dev/BizPalm-Mobile/main/assets/screenshots/menu.png" width="300" alt="Dashboard Menu">
  <br>
  <b>2. Dashboard Menu</b>
</p>

<p align="center">
  <img src="https://raw.githubusercontent.com/Chie03-dev/BizPalm-Mobile/main/assets/screenshots/inventory.png" width="300" alt="Inventory Management">
  <br>
  <b>3. Inventory Management</b>
</p>

<p align="center">
  <img src="https://raw.githubusercontent.com/Chie03-dev/BizPalm-Mobile/main/assets/screenshots/camera.png" width="300" alt="Barcode Scanner Camera">
  <br>
  <b>4. Barcode Scanner Camera</b>
</p>

<p align="center">
  <img src="https://raw.githubusercontent.com/Chie03-dev/BizPalm-Mobile/main/assets/screenshots/cameraaddtocart.png" width="300" alt="Camera Scan and Cart">
  <br>
  <b>5. Camera Scan and Cart Integration</b>
</p>

<p align="center">
  <img src="https://raw.githubusercontent.com/Chie03-dev/BizPalm-Mobile/main/assets/screenshots/transaction.png" width="300" alt="Transaction History">
  <br>
  <b>6. Transaction History</b>
</p>

<p align="center">
  <img src="https://raw.githubusercontent.com/Chie03-dev/BizPalm-Mobile/main/assets/screenshots/map.png" width="300" alt="Nearby Stores Map">
  <br>
  <b>7. Nearby Stores Map</b>
</p>

<p align="center">
  <img src="https://raw.githubusercontent.com/Chie03-dev/BizPalm-Mobile/main/assets/screenshots/notification.png" width="300" alt="Notifications and Alerts">
  <br>
  <b>8. Notifications and Alerts</b>
</p>

---

## Core Features

- **Instant Barcode Scanning**: Capture EAN-13, UPC-A, and QR barcodes instantly via CameraX and ML Kit without manual typing.
- **Inventory Control**: Track stock quantities, unit costs, selling prices, categories, and low stock thresholds in real time.
- **POS Checkout & Transactions**: Process cash payments, calculate change, handle online payments (GCash/Maya), and record customer loans (utang) with digital signatures.
- **Statistical Analytics**: Forecast revenue trends using linear regression and detect sales anomalies using Z-score analysis.
- **PDF Receipt Generation**: Build and share itemized sales receipts and business summaries locally via iText Core.
- **Fully Offline Design**: Complete operational autonomy with zero cloud dependency.

---

## Technical Stack & Library Selection Rationale

Choosing the right SDKs and libraries is critical for building a high-performance offline mobile system. Here is the technical breakdown of why each tool was chosen:

- **Room Persistence Library (SQLite)**: Chosen over cloud backends to guarantee instant query speeds, ACID compliance, and complete offline availability. Room provides compile-time verification of SQL queries and seamless LiveData integration.
- **Google ML Kit & CameraX**: Chosen for barcode scanning because CameraX abstracts away complex device-specific camera lifecycle boilerplate, while ML Kit runs high-performance barcode detection entirely on-device without external web service roundtrips.
- **Apache Commons Math**: Selected for advanced retail analytics. Its statistical regression and descriptive statistics engines allow the app to compute local sales predictions and market basket analysis (Apriori bundling) without needing heavy backend machine learning frameworks.
- **MPAndroidChart**: Integrated to render rich, responsive sales charts and revenue graphs directly on the analytics dashboard.
- **iText Core**: Chosen for programmatic PDF generation to enable thermal printing and digital receipt sharing.

---

## Developer Journal and Technical Insights

Building BizPalm was an intensive journey into systems design, local data architecture, and hardware interoperability. Here are the core engineering lessons learned during development:

### 1. Embracing Local-First Architecture
Designing for offline-first usage forces a strict separation of concerns. By relying on Room DAOs and Repositories as the single source of truth, the UI layer remains completely decoupled from storage details. If cloud synchronization is added in the future, it can be layered directly into the repository layer without touching a single ViewModel or UI component.

### 2. Optimizing the Barcode Scanning Pipeline
Bridging CameraX frame analysis with ML Kit required careful threading management. Running frame analysis on background executors (`Dispatchers.IO`) prevents blocking the main UI thread during continuous camera preview scanning, resulting in fluid 60fps barcode detection.

### 3. Implementing On-Device Statistical Forecasting
Instead of relying on remote cloud LLMs or heavy APIs for business intelligence, leveraging Apache Commons Math allowed the implementation of deterministic linear regression directly inside the ViewModel. Computing projected sales locally ensures that sensitive financial metrics never leave the device, maximizing user privacy and performance.

---

## How to Run

1. Clone the repository:
   ```bash
   git clone https://github.com/Chie03-dev/BizPalm-Mobile.git
   ```
2. Open the project in Android Studio.
3. Allow Gradle to sync and download all dependencies.
4. Connect an Android device or start an emulator running Android 8.0 (API 26) or higher.
5. Click Run to build and install the application.

---

## Contact

- **Developer**: Alchie O. Andilab
- **Email**: alchieandilab2003@gmail.com
- **GitHub**: [@Chie03-dev](https://github.com/Chie03-dev)
