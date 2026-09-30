# 🕒 Personal Desktop Dashboard

A sleek, lightweight, Dark Mode desktop utility app designed to track your daily budget allocations and manage your chronological schedule blocks seamlessly. 
*Disclaimer, I used AI to create this app, as I am still learning, and this was just supposed to be a fun project.*

---

## 🚀 How to Install and Run (Windows)

Follow these 3 quick steps to get the app running on your computer in under 2 minutes!

### 1️⃣ Step 1: Download the Java Engine
Because this is a native desktop app, your computer needs a basic Java runtime helper to read it.
1. Click this link to go to the official [Adoptium Temurin Downloads Page](https://adoptium.net).
2. Keep the default settings (**Java 21 LTS** and **Windows**), and click the blue button to download the **`.msi` installer** file.
3. Open the downloaded installer file and click Next.
4. ⚠️ **CRUCIAL STEP:** When you see the feature list window, look for **`Add to PATH`** and **`Set JAVA_HOME variable`**. Click the little icons next to both of them and change them to **"Will be installed on local hard drive"** (this ensures Windows knows how to launch your scripts automatically!).
5. Click Next and Finish to complete the installation.

### 2️⃣ Step 2: Download the Dashboard App Files
1. Look at the right-hand sidebar of this GitHub page, click on **Releases**, and open the latest version.
2. Under the **Assets** section at the bottom, download **BOTH** files:
   * `DashboardApp-1.0-SNAPSHOT.jar`
   * `Launch.bat`
3. Create a brand-new folder on your desktop named `My Dashboard`.
4. Move **both downloaded files completely inside this folder** so they are sitting right next to each other.

### 3️⃣ Step 3: Unblock & Launch!
Windows automatically flags files downloaded from web browsers as "untrusted." We just need to tell Windows they are safe:
1. **Right-click** on `Launch.bat` ➔ Select **Properties**.
2. Look at the very bottom of the window, check the box that says **`Unblock`**, and click **Apply** and **OK**.
3. Do the exact same thing for the `DashboardApp-1.0-SNAPSHOT.jar` file (**Right-click ➔ Properties ➔ Check Unblock ➔ OK**).
4. **Double-click `Launch.bat` to open your app!**

---

## 💎 Features Included
* 💼 **Finance Tracker:** Log individual incomes and expenses to see your running balance ledger. Set up your custom Tithe or Savings targets and log money toward them to monitor your fulfillment bars.
* 📋 **To-Do Time Shifter:** Schedule your tasks down to the exact minute. Use the control buttons to move your entire day's schedule forward or backward by **1 Hour** or **10 Minutes** simultaneously!
* 🕒 **Live System Clock:** A persistent digital header clock that keeps your dashboard and task tracking accurate to the second.
* 💾 **Auto-Save Database:** The app automatically writes your logs to a private text file in your folder every time you close it, meaning your profiles reload exactly where you left them next time!
