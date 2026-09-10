# Chấm Điểm Tổ

Ứng dụng Android quản lý điểm thi đua nề nếp & học tập của các thành viên trong tổ (lớp 9).
Dùng **offline hoàn toàn**, không cần internet, không tài khoản. Dữ liệu lưu trong máy và
**không bị xoá theo tuần**, nên có thể tổng hợp lại theo **tuần / tháng / học kì** bất cứ lúc nào.

> Dành cho tổ trưởng quản lý khoảng 10 bạn. Chấm theo tuần, lưu lại để cộng dồn cả kì.

---

## Tính năng

- **Tuần này**: bảng xếp hạng thành viên theo điểm thi đua trong tuần; chạm 1 phát để ghi nhanh điểm cho hôm nay.
- **Nhập điểm**: chọn ngày, chọn nhiều bạn cùng lúc, chọn loại điểm, thêm ghi chú.
- **Tổng hợp**: xem theo **Tuần / Tháng / Kì**; **Chia sẻ** báo cáo dạng chữ hoặc **Lưu file CSV** (mở bằng Excel).
- **Lịch sử**: xem lại, sửa, xoá từng lượt điểm; lọc theo thành viên và theo tháng.
- **Cài đặt**: quản lý thành viên, loại điểm, học kì, điểm gốc mỗi tuần; sao lưu / phục hồi toàn bộ dữ liệu ra file `.json`.
- Giao diện tiếng Việt, sáng/tối theo hệ thống. Không xin bất kỳ quyền nào (không truy cập mạng, danh bạ, vị trí…).

---

## Cách tính điểm

Mỗi thành viên **bắt đầu mỗi tuần với 100 điểm gốc** (có thể chỉnh trong Cài đặt).

```
Điểm thi đua tuần = Điểm gốc − Tổng điểm trừ (vi phạm) + Tổng điểm cộng (thành tích)
```

Loại điểm mặc định (nạp sẵn lần chạy đầu, sửa/thêm/xoá thoải mái):

| Cộng điểm | Điểm |
|---|---|
| Điểm KT/TX đạt 9–10 | +7 |
| Điểm KT/TX đạt 7–8 | +4 |
| Giờ học đạt loại Tốt | +2 |
| Phát biểu xây dựng bài | +2 |

| Trừ điểm | Điểm |
|---|---|
| Nghỉ học không phép | −10 |
| Vắng chào cờ / sinh hoạt tập thể (không lý do) | −10 |
| Nói tục, gây mất đoàn kết | −10 |
| Không truy bài / mất trật tự 15 phút đầu giờ | −5 |
| Đi học muộn | −5 |
| Trực nhật muộn / không sạch sẽ | −5 |
| Vứt rác bừa bãi / ăn quà vặt trong trường | −5 |
| Không xếp hàng / không tập thể dục giữa giờ | −5 |
| Không soạn bài / không làm bài tập / không ghi bài | −5 |
| Giờ học chỉ đạt loại Khá | −3 |
| Không đồng phục / khăn quàng đỏ | −3 |

**Tổng hợp tháng / kì**: app lấy điểm thi đua của từng tuần trong khoảng thời gian rồi tính **trung bình các tuần đã chấm**
(tuần không có lượt điểm nào của cả tổ được coi là tuần nghỉ, không tính).

---

## Cài đặt lên điện thoại (Android)

Cần điện thoại Android **8.0 trở lên**. Không cần cáp USB, không cần máy tính.

1. Tải file `ChamDiemTo-v1.0.apk` ở mục **[Releases](../../releases)** của repo này về điện thoại
   (hoặc tự chuyển file qua Zalo / Google Drive / Gmail…).
2. Mở **Quản lý tệp** → thư mục **Download** → bấm vào file `.apk`.
3. Máy hỏi *"Cho phép cài từ nguồn này?"* → bật cho phép → **Cài đặt**.
4. Nếu hiện *"Đã chặn ứng dụng để bảo vệ thiết bị"* (Google Play Protect):
   - Bấm **Chi tiết → Vẫn cài đặt**; hoặc
   - Mở **CH Play → ảnh đại diện → Play Protect → ⚙ → tắt "Quét ứng dụng bằng Play Protect"**, cài xong bật lại.
5. (Máy Xiaomi/Redmi) nếu hỏi *"Gửi ứng dụng để MIUI quét?"* → bỏ tick, chọn **Vẫn cài**.

### Lần đầu mở app

1. **Cài đặt → Thành viên**: thêm các bạn trong tổ.
2. **Cài đặt → Loại điểm**: xem lại bảng điểm, chỉnh cho khớp quy định lớp nếu cần.
3. **Cài đặt → Điểm gốc mỗi tuần**: mặc định 100, đổi nếu cần.
4. **Cài đặt → Học kì**: thêm mốc ngày bắt đầu / kết thúc HK1, HK2 để xem tổng hợp theo kì.

---

## Cách dùng nhanh

| Muốn làm gì | Vào đâu |
|---|---|
| Ghi nhanh 1 điểm cho 1 bạn hôm nay | **Tuần này** → chạm tên bạn → chọn loại điểm |
| Ghi 1 loại điểm cho nhiều bạn / ngày khác | **Nhập điểm** |
| Xem ai đang cao/thấp trong tuần | **Tuần này** (số to bên phải = 100 + cộng − trừ) |
| Báo cáo tháng / học kì | **Tổng hợp** → chọn tab, bấm **Chia sẻ** hoặc **Lưu CSV** |
| Sửa / xoá một lượt chấm nhầm | **Lịch sử** → chạm vào lượt đó |
| Sao lưu trước khi đổi máy | **Cài đặt → Xuất dữ liệu (.json)** |
| Khôi phục trên máy mới | **Cài đặt → Nhập dữ liệu** → chọn file `.json` |

> **Nên xuất file `.json` định kỳ** (cuối mỗi tháng chẳng hạn) và cất ở Google Drive.
> Toàn bộ dữ liệu chỉ nằm trong máy; gỡ app hoặc mất máy là mất hết.

---

## Build từ mã nguồn

### Yêu cầu

- JDK 17
- Android SDK có **Platform API 36** và **Build-Tools 36.1.0**
  (cài qua Android Studio hoặc `sdkmanager`)
- Không cần cài Gradle sẵn — dùng Gradle Wrapper kèm trong repo.

### Các bước

```bash
# 1. Trỏ tới Android SDK
echo "sdk.dir=/duong/dan/toi/Android/Sdk" > local.properties

# 2. Tạo khoá ký (chỉ làm 1 lần, giữ file này thật kỹ)
keytool -genkeypair -v -keystore chamdiem-release.jks \
  -alias chamdiem -keyalg RSA -keysize 2048 -validity 10000 \
  -storepass 'MAT_KHAU_CUA_BAN' -keypass 'MAT_KHAU_CUA_BAN' \
  -dname "CN=Cham Diem To, O=Lop 9, C=VN"

# 3. Khai báo khoá ký
cp keystore.properties.example keystore.properties
#   rồi sửa storePassword / keyPassword trong keystore.properties

# 4. Build APK phát hành đã ký
./gradlew :app:assembleRelease
```

APK nằm ở: `app/build/outputs/apk/release/app-release.apk`

Build bản gỡ lỗi (không cần khoá ký): `./gradlew :app:assembleDebug`

### Ra bản cập nhật

- Tăng `versionCode` và `versionName` trong [`app/build.gradle.kts`](app/build.gradle.kts).
- Build lại bằng **đúng file `chamdiem-release.jks`** cũ → bản mới cài đè lên bản cũ, **không mất dữ liệu**.
- Nếu ký bằng khoá khác, Android sẽ bắt gỡ app cũ trước (mất dữ liệu).

> ⚠️ `keystore.properties` và `*.jks` đã bị loại khỏi Git (xem `.gitignore`).
> Sao lưu file `.jks` + mật khẩu ở nơi an toàn — mất là không ra được bản cập nhật cài đè.

---

## Công nghệ

- Kotlin + Jetpack Compose (Material 3)
- Room (SQLite) lưu dữ liệu cục bộ
- Kiến trúc: một `AppViewModel` + `Repository`, dữ liệu chảy qua Kotlin `Flow`
- `minSdk 26`, `targetSdk 36`, không có quyền `INTERNET`

## Cấu trúc thư mục

```
app/src/main/java/com/tota/chamdiem/
├── data/          # Room: Entities, DAO, Repository, sao lưu JSON
├── ui/
│   ├── screens/   # Home, AddEntry, Report, History, Members, Settings
│   ├── theme/     # màu sắc, theme sáng/tối
│   ├── AppRoot.kt # điều hướng + thanh dưới
│   └── AppViewModel.kt
├── util/          # DateUtils, Reporting (tính toán + xuất CSV/chữ)
└── MainActivity.kt
```

Đặc tả chi tiết: [SPEC.md](SPEC.md)

---

## Giấy phép

Dự án cá nhân dùng cho việc quản lý tổ ở lớp. Bạn được tự do sao chép, sửa, dùng lại.
