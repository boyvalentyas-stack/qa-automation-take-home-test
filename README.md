# QA Automation Challenge

Repository ini berisi hasil pengerjaan **Take-Home Test QA Engineer (Automation)**.
Karena kandidat tidak diberi akses ke staging internal, seluruh task dikerjakan terhadap aplikasi demo publik:

- **Web UI**: [saucedemo.com](https://www.saucedemo.com) — flow Login → Add to Cart → Checkout
- **API**: [reqres.in](https://reqres.in) — endpoint `users` (GET, POST, PUT, DELETE)

---

## 📁 Struktur Repository

```
├── katalon/                # Bagian B - Web Automation (Katalon Studio project)
├── API?postman/            # Bagian C - API Testing (Collection + Environment)
├── Performance.JMeter/     # Bagian D - Performance Testing (Test Plan + report)
└── README.md               # Dokumen ringkasan, bug report, dan Gherkin feature file
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

## ✅ Bagian C — API Testing (Postman)

Lokasi: [`postman/ReqRes API Tests.postman_collection.json`](./postman/ReqRes%20API%20Tests.postman_collection.json) + [`postman/ReqRes Environment.postman_environment.json`](./postman/ReqRes%20Environment.postman_environment.json)

### Request yang dicakup

| Request | Method | Endpoint | Test Coverage |
|---|---|---|---|
| create user | POST | `{{base_url}}/users` | Status 201, struktur response (`id`, `name`, `job`, `createdAt`), name/job sesuai request body, chaining: simpan `id` ke variable `user_id` |
| list users | GET | `{{base_url}}/users?page=2` | Status 200, struktur pagination (`page`, `per_page`, `total`, `total_pages`, `data`), **looping** validasi tiap object dalam array `data` (field `id`, `name`, `year`, `color`), **conditional** untuk field opsional `avatar` |
| update user | PUT | `{{base_url}}/users/{{user_id}}` | Status 200, struktur response (`name`, `job`, `updatedAt`), name/job sesuai request body — menggunakan `user_id` hasil chaining dari create user |
| delete user | DELETE | `{{base_url}}/users/{{user_id}}` | Status 204, response body kosong |
| user not found | GET | `{{base_url}}/users/23` | Status 404 (negative case), response body berupa object kosong |

### Chaining
`user_id` di-set otomatis lewat `pm.environment.set("user_id", pm.response.json().id)` di test script **create user**, lalu dipakai sebagai path variable di request **update user** dan **delete user**.

### Environment Variables
Tidak ada base URL hardcoded — semua lewat environment `ReqRes Environment`:

| Variable | Keterangan |
|---|---|
| `base_url` | Base URL API (`https://reqres.in/api`) |
| `user_id` | Diisi otomatis via chaining dari response create user |
| `api_key` | API key reqres.in (lihat catatan di Bagian D) |

### Cara Menjalankan
1. Import `ReqRes API Tests.postman_collection.json` dan `ReqRes Environment.postman_environment.json` ke Postman.
2. Isi value `base_url` dan `api_key` di environment (lihat [Assumptions & Questions](#-assumptions--questions-for-poba)).
3. Jalankan collection secara berurutan (create → list → update → delete → not found) via Collection Runner, karena update dan delete bergantung pada `user_id` hasil create.

---

## ✅ Bagian D — Performance Testing (JMeter)

Lokasi: [`jmeter/GET_Users_LoadTest.jmx`](./jmeter/GET_Users_LoadTest.jmx) + screenshot report di [`jmeter/report-screenshot/`](./jmeter/report-screenshot)

### Konfigurasi Test Plan

| Elemen | Konfigurasi |
|---|---|
| Thread Group | 20 users, ramp-up 10 detik, 1 loop |
| HTTP Header Manager | `x-api-key` (lihat catatan di bawah) |
| HTTP Request Defaults | Protocol `https`, Domain `reqres.in` |
| Loop Controller | Loop count 3, membungkus `GET /api/users` → total 60 request (20 × 3) |
| If Controller | Kondisi `${__javaScript("${responseTime}" > 1000)}`, membungkus Response Assertion — logic kondisional untuk menandai response yang melebihi threshold 1000ms |
| Listener | Summary Report & Aggregate Report |

### Hasil Run

Dua kali run dilakukan untuk observasi:

| Metrik | Run 1 | Run 2 (setelah warm-up) |
|---|---|---|
| # Samples | 60 | 60 |
| Average | 281 ms | 163 ms |
| Median | 45 ms | 59 ms |
| 90% Line | 792 ms | 382 ms |
| 95% Line | 1291 ms | 674 ms |
| 99% Line | 1672 ms | 1048 ms |
| Max | 4659 ms | 1976 ms |
| Error % | 0.00% | 0.00% |
| Throughput | 5.4/sec | 6.1/sec |

Run 1 menunjukkan latency signifikan lebih tinggi di semua percentile dibanding Run 2, terindikasi sebagai **cold start effect** (koneksi/DNS resolve pertama kali lebih lambat) — bukan indikasi masalah sistemik, karena Run 2 dengan kondisi identik jauh lebih stabil.

### Jawaban Singkat: Apakah hasil ini acceptable?

Hasil ini **acceptable** untuk skala kecil (20 user, error 0%, average ~163ms pada run yang representatif). Pada run pertama sempat terlihat lonjakan signifikan di response time (max hingga 4659ms) yang teridentifikasi sebagai cold start effect — setelah run ulang, latency turun stabil (max 1976ms, 99th percentile 1048ms). **Red flag** yang tetap perlu diawasi di production adalah gap antara median (59ms) dan 99th percentile (1048ms) — mengindikasikan sebagian kecil request masih jauh lebih lambat dari mayoritas, yang berpotensi membesar seiring peningkatan concurrent user.

### ⚠️ Catatan Penting: reqres.in Memerlukan API Key

Saat pengerjaan, ditemukan bahwa **reqres.in telah mengubah kebijakan API (per 2025 relaunch)** sehingga mewajibkan header `x-api-key` untuk semua request ke `/api/*`. Hal ini tidak disebutkan di dokumen challenge asli, kemungkinan karena dibuat sebelum perubahan tersebut terjadi (reqres.in awalnya adalah fake-API tanpa autentikasi). Sebagai penyesuaian:
- Ditambahkan **HTTP Header Manager** di level Thread Group (JMeter) dan header `x-api-key` di tiap request (Postman)
- API key didapat gratis via signup di [reqres.in/signup](https://reqres.in/signup)

**Security note**: API key **tidak** disertakan dalam file `.jmx`/environment Postman yang di-commit ke repository ini — silakan isi sendiri via `HTTP Header Manager` (JMeter) atau environment variable `api_key` (Postman) sebelum menjalankan.

---

## ✅ Bagian E — Bug Report

### Bug Report #1

| Field | Detail |
|---|---|
| **Summary** | Automated regression test "Add to Cart" gagal pada run terbaru tanpa perubahan kode/environment, namun fitur berjalan normal saat dicek manual |
| **Priority** | Low–Medium (tidak mengindikasikan bug produk) |
| **Severity** | Minor (mengganggu keandalan test suite, bukan fungsionalitas aplikasi) |
| **Steps to Reproduce** | 1. Jalankan regression suite otomatis untuk flow "Add to Cart" di saucedemo.com<br>2. Bandingkan hasil run terbaru dengan run-run sebelumnya (yang selalu pass)<br>3. Lakukan verifikasi manual: login → pilih produk → klik "Add to Cart" → cek badge cart bertambah |
| **Actual Result** | Automated test melaporkan FAIL pada step "Add to Cart", tapi verifikasi manual menunjukkan fitur berfungsi normal (badge cart bertambah, item masuk ke cart) |
| **Expected Result** | Automated test seharusnya konsisten PASS selama tidak ada perubahan kode aplikasi maupun environment |
| **Environment** | saucedemo.com, Katalon Studio (browser: Chrome), tidak ada perubahan test script/environment sejak run sebelumnya |

**Klasifikasi: Automation issue (false positive)** — bukan bug aplikasi. Alasan:
- Verifikasi manual mengonfirmasi fitur berjalan normal, sehingga akar masalah bukan di aplikasi.
- Kemungkinan penyebab: race condition/timing (elemen belum fully loaded saat script mengklik), state leftover dari test sebelumnya (cart belum ter-reset karena test sebelumnya gagal di tengah jalan), atau flakiness locator (elemen dinamis berubah ID/attribute sementara).
- Rekomendasi: tambahkan smart wait yang lebih ketat sebelum assertion, pastikan ada langkah cleanup/reset state di awal setiap test (independent test), dan re-run test 2–3x untuk konfirmasi apakah flaky sebelum menutup tiket sebagai false positive.

### Bug Report #2

| Field | Detail |
|---|---|
| **Summary** | Field `createdAt` pada response POST /api/users tidak konsisten formatnya (kadang ISO 8601, kadang Unix timestamp) dibanding dokumentasi API |
| **Priority** | Medium |
| **Severity** | Moderate (berpotensi merusak parsing/integrasi di sisi consumer/client) |
| **Steps to Reproduce** | 1. Kirim beberapa kali request POST ke `/api/users` dengan body berbeda-beda<br>2. Perhatikan status code response (201 Created)<br>3. Bandingkan format field `createdAt` di tiap response satu sama lain, dan bandingkan dengan format yang tercantum di dokumentasi API |
| **Actual Result** | Status code 201 konsisten, tapi format `createdAt` berubah-ubah — kadang string ISO 8601 (misal `2026-09-15T10:23:00.000Z`), kadang angka Unix timestamp (misal `1757930580`) |
| **Expected Result** | Format `createdAt` harus konsisten sesuai dokumentasi API di semua response, agar client/consumer bisa parsing dengan reliable tanpa perlu handle multiple format |
| **Environment** | API: reqres.in/api/users, diuji via Postman Collection, `base_url` sebagai environment variable |

**Catatan tambahan**: Ini murni bug aplikasi/API (bukan automation issue), karena inkonsistensi terjadi di response server itu sendiri, konsisten teramati lewat tool berbeda (Postman test script), dan tidak bergantung pada kondisi test runner.

---

## ❓ Assumptions & Questions for PO/BA

Berikut ambiguitas yang ditemukan selama pengerjaan:

1. **Definisi "checkout sukses"** — Saat ini validasi hanya berdasarkan munculnya pesan *"Thank you for your order!"*, tanpa verifikasi tambahan bahwa cart ter-reset ke kosong setelah checkout selesai.
   → *Apakah verifikasi cart reset perlu ditambahkan sebagai bagian dari acceptance criteria checkout sukses?*

2. **Perilaku checkout dengan cart kosong** — SauceDemo secara default tidak benar-benar memblokir user untuk mengakses halaman checkout meski cart kosong (tidak ada validasi eksplisit dari sisi aplikasi). Step saat ini hanya memverifikasi keberadaan tombol Checkout di halaman Cart, bukan mencegah navigasi lebih lanjut.
   → *Apakah ekspektasi "user should not be able to complete the checkout" ini valid untuk SauceDemo, atau perlu didefinisikan ulang skenario edge case-nya?*

3. **Exact match vs partial match pesan error** — Assertion pesan error menggunakan `.contains()`, dan pada pesan konfirmasi checkout dilakukan `.replace("!", "")` sebelum dibandingkan, mengindikasikan teks asli dari aplikasi tidak selalu identik 100% dengan yang dituliskan di feature file.
   → *Apakah validasi pesan cukup partial match (mengandung kata kunci), atau harus exact match?*

4. **`performance_glitch_user`** sengaja dimasukkan ke Examples table untuk mensimulasikan delay/lag pada aplikasi.
   → *Apakah smart wait dengan timeout 10 detik sudah dianggap cukup mengakomodasi delay dari user ini, atau perlu timeout khusus yang lebih panjang untuk skenario ini?*

5. **reqres.in kini mewajibkan API key** — dokumen challenge asli mengasumsikan reqres.in adalah public fake-API tanpa autentikasi, namun per 2025 semua request ke `/api/*` wajib membawa `x-api-key`.
   → *Karena aplikasi demo publik ini di luar kendali tim, apakah acceptable untuk kandidat menyesuaikan test (menambahkan API key) seperti yang dilakukan di sini, atau apakah ada demo API alternatif yang sebaiknya dipakai ke depannya untuk challenge serupa?*

---

## 🛠️ Tools yang Digunakan
- Katalon Studio (Free/Community Edition) — Web Automation
- Cucumber/Gherkin — BDD test scenario
- Postman — API Testing
- Apache JMeter 5.6.3 — Performance Testing
