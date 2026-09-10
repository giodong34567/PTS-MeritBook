# Chấm Điểm Tổ — Đặc tả (SPEC)

App Android quản lý điểm thi đua của các thành viên trong tổ. Dùng offline hoàn toàn,
không cần internet, không tài khoản. Dữ liệu lưu trong máy (SQLite/Room) và **không bao
giờ bị xoá tự động**, nên có thể tổng hợp lại theo tuần / tháng / học kì bất cứ lúc nào.

- Người dùng: tổ trưởng lớp 9, quản lý ~10 thành viên.
- Thiết bị đích: điện thoại Xiaomi Redmi (Android 8.0 trở lên), cài bằng file APK, dùng lâu dài.

## 1. Mô hình dữ liệu

### Member (Thành viên)
| Trường | Kiểu | Ghi chú |
|---|---|---|
| id | Long | khoá chính |
| name | String | họ tên |
| active | Boolean | còn trong tổ hay không (mặc định true) |
| sortOrder | Int | thứ tự hiển thị |
| createdAt | Long | thời điểm tạo (epoch ms) |

### Category (Loại điểm)
| Trường | Kiểu | Ghi chú |
|---|---|---|
| id | Long | khoá chính |
| name | String | tên loại, ví dụ "Phát biểu xây dựng bài" |
| points | Int | số điểm; **dương = cộng, âm = trừ** |
| active | Boolean | đang dùng hay không |
| sortOrder | Int | thứ tự |

Loại điểm mặc định tạo ở lần mở app đầu tiên (người dùng sửa/thêm/xoá thoải mái trong Cài đặt):

| Tên | Điểm |
|---|---|
| Điểm tốt (8–10) | +1 |
| Phát biểu xây dựng bài | +1 |
| Giúp đỡ bạn / việc tốt | +1 |
| Nói chuyện riêng trong giờ | −1 |
| Không làm bài tập | −1 |
| Điểm kém (dưới 5) | −1 |
| Vi phạm nội quy khác | −1 |

### ScoreEntry (Lượt chấm điểm)
| Trường | Kiểu | Ghi chú |
|---|---|---|
| id | Long | khoá chính |
| memberId | Long | FK → Member, xoá thành viên thì xoá theo (CASCADE) |
| categoryId | Long? | FK → Category, xoá loại thì để null (SET NULL) |
| categoryName | String | **chụp lại tên loại tại thời điểm chấm** |
| points | Int | **chụp lại số điểm tại thời điểm chấm** (sửa loại điểm sau này không làm sai lịch sử) |
| dateEpochDay | Long | ngày áp dụng điểm = `LocalDate.toEpochDay()` (có index để truy vấn theo khoảng) |
| note | String? | ghi chú tuỳ chọn (môn học, tiết, lý do…) |
| createdAt | Long | thời điểm nhập (epoch ms) |

### Semester (Học kì)
| Trường | Kiểu | Ghi chú |
|---|---|---|
| id | Long | khoá chính |
| name | String | ví dụ "HK1 (2025–2026)" |
| startEpochDay | Long | ngày bắt đầu |
| endEpochDay | Long | ngày kết thúc |

Người dùng tự nhập học kì trong Cài đặt (mặc định để trống).

### Quy ước tuần
- Tuần bắt đầu **Thứ Hai**. Một tuần được định danh bằng ngày Thứ Hai của tuần đó.
- Khoảng của tuần chứa ngày `d`: `monday = d - (d.dayOfWeek - 1)` ngày, đến `monday + 6`.

## 2. Truy vấn tổng hợp (một truy vấn dùng chung cho tuần/tháng/kì)

`aggregate(start, end)` trả về danh sách `MemberScore`:
`memberId, memberName, plusCount, minusCount, plusPoints, minusPoints, net`
= LEFT JOIN Member với ScoreEntry lọc `dateEpochDay BETWEEN start AND end`,
GROUP BY thành viên, `net = SUM(points)`, sắp xếp theo `net` giảm dần rồi tên.

- Tuần: `start/end` = Thứ Hai … Chủ Nhật của tuần đang xem.
- Tháng: `start/end` = ngày đầu … ngày cuối tháng.
- Kì: `start/end` = `startEpochDay … endEpochDay` của học kì được chọn.

## 3. Màn hình

### 3.1 Trang chủ — "Tuần này"
- Thanh đầu: `‹  10/11 – 16/11  ›` đổi tuần; nút **Tuần này** quay về tuần hiện tại.
- Hai chip tổng: tổng lượt cộng ▲ và tổng lượt trừ ▼ của cả tổ trong tuần.
- Danh sách thành viên (sắp theo `net` giảm dần):
  - Tên, điểm `net` lớn (xanh nếu ≥ 0, đỏ nếu < 0), dòng phụ `▲ 3   ▼ 1`.
  - **Chạm** vào một thành viên → bảng trượt (bottom sheet) các nút loại điểm → 1 chạm ghi điểm cho **hôm nay**.
  - **Giữ** một thành viên → xem các lượt điểm trong tuần của bạn đó.
- Nút **+** (FAB) → màn hình Nhập điểm đầy đủ.

### 3.2 Nhập điểm
- Chọn **ngày** (mặc định hôm nay).
- Chọn **một hoặc nhiều thành viên** (checkbox) — nhập nhanh cho cả nhóm cùng lúc.
- Chọn **loại điểm** (danh sách loại đang bật, tách nhóm Cộng / Trừ).
- **Ghi chú** tuỳ chọn.
- **Lưu** → tạo 1 `ScoreEntry` cho mỗi thành viên đã chọn (có chụp tên + điểm của loại).

### 3.3 Lịch sử
- Danh sách mọi lượt điểm, mới nhất trước: tên, loại, điểm (màu), ngày, ghi chú.
- Lọc theo **thành viên** và theo **tháng**.
- Chạm để **sửa**, gạt/nút để **xoá** (có xác nhận).

### 3.4 Tổng hợp
- Chuyển tab: **Tuần | Tháng | Kì**.
  - Tuần / Tháng: nút `‹ ›` chọn kỳ.
  - Kì: chọn học kì trong danh sách (nếu chưa có → nhắc thêm ở Cài đặt).
- Bảng xếp hạng: hạng, tên, ▲ số lượt, ▼ số lượt, `net`.
- **Chia sẻ**: xuất báo cáo dạng chữ + CSV, gửi qua Intent chia sẻ của Android.
- **Lưu file**: lưu CSV ra thư mục tuỳ chọn (SAF `ACTION_CREATE_DOCUMENT`).

### 3.5 Thành viên
- Danh sách; thêm (nhập tên), đổi tên, bật/tắt "còn trong tổ".
- Xoá thành viên (cảnh báo: xoá luôn lịch sử điểm của bạn đó).

### 3.6 Cài đặt
- **Loại điểm**: xem/thêm/sửa/xoá/bật-tắt (tên + số điểm ±).
- **Học kì**: xem/thêm/sửa/xoá (tên + ngày bắt đầu/kết thúc).
- **Sao lưu & phục hồi**:
  - *Xuất dữ liệu (.json)* — ghi toàn bộ CSDL ra file (đổi máy vẫn giữ được dữ liệu).
  - *Nhập dữ liệu* — đọc file .json, thay thế dữ liệu hiện tại (có xác nhận).
- **Giới thiệu**: phiên bản app.

## 4. Kỹ thuật

- Ngôn ngữ: Kotlin. Giao diện: Jetpack Compose + Material 3 (sáng/tối theo hệ thống).
- Lưu trữ: Room (SQLite). ViewModel + Kotlin Flow.
- `minSdk 26` (Android 8.0), `targetSdk 36`, `compileSdk 36`.
- **Không có quyền INTERNET** — app chạy hoàn toàn offline, không hết hạn.
- Build: Gradle wrapper (kèm sẵn trong project).
- APK phát hành ký bằng keystore cố định (`chamdiem-release.jks`, hạn ~27 năm) để các
  bản cập nhật sau cài đè lên được. `minifyEnabled = false` cho chắc chắn, app nhỏ.
- Kết quả: `app/build/outputs/apk/release/app-release.apk`.

## 5. Cài lên máy Redmi

1. Chép file `app-release.apk` vào điện thoại.
2. Cài đặt → mở khoá "Cài ứng dụng từ nguồn không xác định" cho trình duyệt/trình quản lý file.
3. (MIUI/HyperOS) khi cài có thể phải tắt tạm "Quét ứng dụng của MIUI" hoặc bỏ tick
   "Gửi ứng dụng để quét".
4. Mở app **Chấm Điểm Tổ**, thêm thành viên và bắt đầu chấm.
5. Định kỳ dùng **Cài đặt → Xuất dữ liệu (.json)** để sao lưu.
