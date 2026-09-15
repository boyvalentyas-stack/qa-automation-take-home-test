# QA Automation Challenge

Repository ini berisi hasil pengerjaan **Take-Home Test QA Engineer (Automation)**.
Karena kandidat tidak diberi akses ke staging internal, seluruh task dikerjakan terhadap aplikasi demo publik:

- **Web UI**: [saucedemo.com](https://www.saucedemo.com) — flow Login → Add to Cart → Checkout
- **API**: [reqres.in](https://reqres.in) — endpoint `users` (GET, POST, PUT, DELETE)

---

## 📁 Struktur Repository

```
├── katalon/              # Bagian B - Web Automation (Katalon Studio project)
├── API/postman/          # Bagian C - API Testing (Collection + Environment)
├── Performance/JMeter/   # Bagian D - Performance Testing (Test Plan + report)
├── Docs/JIRA/            # Dokumen ringkasan, bug report, dan Gherkin feature file
└── README.md
```

---

## ✅ Bagian A — Test Scenario (Gherkin)

File: [`docs/LoginAddToCartCheckout.feature`](./docs/LoginAddToCartCheckout.feature)

Flow yang diuji: **Login → Add to Cart → Checkout** pada SauceDemo.

| Scenario | Tipe | Keterangan |
|---|---|---|
| Successful login and checkout with valid users | Positive (Scenario Outline) | Menggunakan 2 kombinasi user (`standard_user`, `performance_glitch_user`) via Examples table |
| Login fails with locked out user | Negative | Login dengan `locked_out_user` |
| Login fails with incorrect password | Negative | Password salah untuk `standard_user` |
| Attempt checkout with an empty cart | Edge case | Checkout tanpa menambahkan item apapun |
| Checkout fails with incomplete form data | Edge case | First name dikosongkan saat isi form checkout |

Total: **5 scenario definitions** (2 di antaranya dijalankan dengan data berbeda lewat Scenario Outline).

---

## ✅ Bagian B — Web Automation (Katalon Studio)

Lokasi: [`katalon/`](./katalon)

### Object Repository
Disusun per halaman/screen, bukan folder datar:
```
Object Repository/
├── LoginPage/Page_Swag Labs/
├── ProductsPage/Page_Swag Labs/
├── CartPage/Page_Swag Labs/
└── CheckoutPage/Page_Swag Labs/
```

### Wait Strategy (No Hard-coded Delay)
Seluruh step menggunakan smart wait bawaan Katalon — **tidak ada `Thread.sleep`**:
- `WebUI.waitForElementVisible(...)`
- `WebUI.waitForElementClickable(...)`

Contoh elemen dinamis yang ditangani secara eksplisit:
- Transisi ke Products Page setelah login
- Tombol **Finish** yang hanya muncul jika validasi form checkout berhasil (lihat logic conditional di bawah)

### Logic Pemrograman yang Diterapkan
Requirement meminta minimal **looping dan/atau conditional**. Yang sudah diimplementasikan:

**Conditional (if-else)** — digunakan di beberapa step definition:
1. `userSeesErrorMessage()` — cek apakah error muncul di halaman Login atau di halaman Checkout, baru ambil teksnya sesuai lokasi yang ditemukan.
2. `userCompletesCheckout()` — cek apakah tombol **Finish** muncul (checkout valid) atau tidak (form invalid), lalu log informasi yang sesuai.
3. `userGoesToEmptyCart()` — cek keberadaan tombol Checkout saat cart kosong.

> **Catatan pengembangan lanjutan**: Requirement juga menyarankan contoh looping untuk menambahkan beberapa item ke cart secara dinamis. Versi saat ini menambahkan 1 item secara statis melalui parameter Gherkin. Ini bisa dikembangkan lebih lanjut dengan menerima list item dan looping `WebUI.click()` untuk tiap item — dicatat sebagai area improvement, bukan blocker karena requirement "looping **dan/atau** conditional" sudah terpenuhi lewat conditional di atas.

### Cara Menjalankan
1. Buka project di Katalon Studio (Free/Community edition).
2. Jalankan test suite yang menjalankan Cucumber feature `LoginAddToCartCheckout.feature`.
3. Report hasil run tersedia di folder `Reports/` setelah eksekusi (screenshot report terbaru disertakan di `katalon/report-screenshot/`).

---

## ❓ Assumptions & Questions for PO/BA

Berikut ambiguitas yang ditemukan selama pengerjaan Bagian A & B:

1. **Definisi "checkout sukses"** — Saat ini validasi hanya berdasarkan munculnya pesan *"Thank you for your order!"*, tanpa verifikasi tambahan bahwa cart ter-reset ke kosong setelah checkout selesai.
   → *Apakah verifikasi cart reset perlu ditambahkan sebagai bagian dari acceptance criteria checkout sukses?*

2. **Perilaku checkout dengan cart kosong** — SauceDemo secara default tidak benar-benar memblokir user untuk mengakses halaman checkout meski cart kosong (tidak ada validasi eksplisit dari sisi aplikasi). Step saat ini hanya memverifikasi keberadaan tombol Checkout di halaman Cart, bukan mencegah navigasi lebih lanjut.
   → *Apakah ekspektasi "user should not be able to complete the checkout" ini valid untuk SauceDemo, atau perlu didefinisikan ulang skenario edge case-nya?*

3. **Exact match vs partial match pesan error** — Assertion pesan error menggunakan `.contains()`, dan pada pesan konfirmasi checkout dilakukan `.replace("!", "")` sebelum dibandingkan, mengindikasikan teks asli dari aplikasi tidak selalu identik 100% dengan yang dituliskan di feature file.
   → *Apakah validasi pesan cukup partial match (mengandung kata kunci), atau harus exact match?*

4. **`performance_glitch_user`** sengaja dimasukkan ke Examples table untuk mensimulasikan delay/lag pada aplikasi.
   → *Apakah smart wait dengan timeout 10 detik sudah dianggap cukup mengakomodasi delay dari user ini, atau perlu timeout khusus yang lebih panjang untuk skenario ini?*

---

## ✅ Bagian C — API Test (Postman)

---

## 🚧 Bagian Lain

- **Bagian C (Postman)**, **Bagian D (JMeter)**, dan **Bagian E (Bug Report)** — *in progress, akan ditambahkan menyusul.*

---

## 🛠️ Tools yang Digunakan
- Katalon Studio (Free/Community Edition)
- Cucumber/Gherkin untuk BDD test scenario
- Postman (rencana)
- Apache JMeter (rencana)
