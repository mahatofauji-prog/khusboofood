# 🍱 Khusboo Food Android App

Official Android Application for **Khusboo Food** — Location-Based Food Marketplace in Purulia, West Bengal.

---

## ⬇️ Download APK (Direct Download)

You can download and install the latest APK directly from GitHub:

### Option 1: Via GitHub Releases (Recommended for Phone)
👉 **[Go to GitHub Releases](https://github.com/mahatofauji-prog/khusboofood/releases)**
1. Tap on the latest release (e.g. `Khushboo Food App v1.0.x`).
2. Under **Assets**, click on **`KhusbooFood.apk`** to download.
3. Open the downloaded APK on your Android phone and install. (If prompted, enable *"Install unknown apps"* for your browser).

### Option 2: Via GitHub Actions Artifacts
👉 **[Go to GitHub Actions Runs](https://github.com/mahatofauji-prog/khusboofood/actions)**
1. Click on the latest green workflow run.
2. Scroll to the bottom under **Artifacts**.
3. Click **`KhusbooFood-Debug-APK`** to download the ZIP file containing the APK.

---

## 🌟 Key Features

- **Circular Category Navigation**: Horizontal scrollable categories with high-definition circular food images & gold borders.
- **Vertical Delicacies Feed**: Real-time kitchen delicacies loaded directly from the database under *"Why Khushboo Food"*.
- **Dynamic Weight Selector**: 250g, 500g, 1kg, 2kg with live dynamic price calculation (`selectedPrice = basePricePerKg × selectedWeightInKg`).
- **Interactive Product Details**: Hero images, store distance, real-time availability indicator, full quantity selector, and instant *"Add to Cart"* & *"Buy Now"*.
- **Multi-Store & Cart Conflict Engine**: Automatically detects orders across different store radius and prevents mixing without confirmation.
- **Location-Based Delivery**: Precise GPS & locality selection in Purulia Town.
- **Customer Onboarding**: Verified mobile registration with OTP simulation and address management.

---

## 🛠️ Build Locally

To build the APK locally using Gradle:

```bash
# Clone the repository
git clone https://github.com/mahatofauji-prog/khusboofood.git
cd khusboofood

# Build debug APK
./gradlew assembleDebug

# Output APK path:
# app/build/outputs/apk/debug/app-debug.apk
```
