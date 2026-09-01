# BizPalm   Mobile Inventory & POS System

BizPalm is an Android-based Point of Sale (POS) and Inventory Management System built for small to medium-sized businesses   retail stores, sari-sari stores, pharmacies, and general merchandise shops. It runs entirely offline on a phone or tablet, so business owners don't need expensive hardware or a stable internet connection.

**Front-end repo:** [BizPalm-Mobile](https://github.com/Chie03-dev/BizPalm-Mobile)

---

  Technology Used

**Language:** Kotlin & Java
**Platform:** Android (runs locally on phone/tablet, no server required)
**Architecture:** MVVM (Model-View-ViewModel)
**Local Database:** Room Persistence Library (SQLite)

**Key libraries:**
- **Google ML Kit**   on-device barcode scanning (EAN-13, UPC-A, QR, and more)
- **CameraX**   camera handling for the scanner
- **Apache Commons Math**   statistical engine for sales predictions and analytics
- **MPAndroidChart**   charts and graphs for the analytics dashboard
- **MediaPipe GenAI (Gemini Nano)**   on-device AI assistant, no cloud dependency
- **iText Core**   PDF generation for receipts and reports

---

#Features

-**Barcode Scanning**   scan products instantly to add them to a sale, no manual typing
-**Inventory Management**   track stock, pricing, cost, and category per product
-**Sales & Transactions**   record sales, calculate change, support cash.
-**Analytics Dashboard**   revenue trends and predictions powered by linear regression
-**Business Health Alerts**   detects sales spikes or drops using Z-score analysis
-**Growth Hacks**   suggests product bundles customers tend to buy together (market basket analysis)
-**Restock Tips**   predicts when a product will run out and suggests reorder quantities
-**Profit Insights**   ranks products by markup and profit margin
-**AI Business Consultant**   ask natural-language questions about your store, answered fully on-device
-**PDF Receipts & Reports**   generate and print/share itemized receipts and sales summaries
-**Fully Offline**   all data lives on the device, no internet needed

---

 The Process

1. **Product Scan**   Customer presents an item → CameraX captures frames → ML Kit detects the barcode → the app queries the local database → the product is added to the cart.
2. **Checkout**   Cashier confirms the cart → payment is validated → the transaction and its line items are saved to the local database → stock quantities are updated → a PDF receipt is generated.
3. **Analytics Refresh**   Whenever transaction data changes, the app automatically recalculates revenue predictions, health alerts, product bundles, and restock timing, then updates the dashboard charts.
4. **AI Consultation**   The owner asks a question → the app builds a prompt using the store's own data → Gemini Nano runs the query locally → an answer appears in the chat UI.

The app follows a **local-first, offline-first design**: everything works without internet, and no financial data ever leaves the device.

---

 How to Run

1. Clone the repository:
   ```bash
   git clone https://github.com/Chie03-dev/BizPalm-Mobile.git
   ```
2. Open the project in **Android Studio**.
3. Let Gradle sync and download dependencies.
4. Connect an Android device (or start an emulator)   Android 8.0 (API 26) or higher recommended.
5. Click **Run** to build and install the app.

> Note: On-device AI (Gemini Nano) and camera-based barcode scanning require a physical device or an emulator with camera and ML support enabled. Some AI features may not work on all emulators.

---

  How to Improve

If you'd like to build on BizPalm, here are some good starting points:

- **Cloud sync/backup**   add an optional remote sync layer on top of the Repository classes without touching the ViewModels or UI (the architecture is already set up for this).
- **Multi-user support**   add staff accounts and permission levels (owner vs. cashier).
- **Better restock automation**   connect the restock suggestions directly to a supplier ordering flow.
- **More payment integrations**   expand beyond Cash/GCash/PayMaya to other e-wallets or bank transfers.
- **Bluetooth printer support**   expand PDF receipt generation to print directly to thermal printers.
- **Testing**   add unit tests for ViewModels (regression, Z-score, and Apriori logic are all isolated and testable).
- **UI/UX polish**   improve accessibility, add dark mode, or support tablet-optimized layouts.
- **Localization**   add support for multiple languages/currencies beyond PHP.

Contributions and forks are welcome   feel free to open an issue or pull request.

---

  Contact

**Developer:** Alchie O. Andilab
**Email:** alchieandilab2003@gmail.com
**GitHub:** [@Chie03-dev](https://github.com/Chie03-dev)
