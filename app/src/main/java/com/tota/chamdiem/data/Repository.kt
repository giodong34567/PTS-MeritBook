package com.tota.chamdiem.data

import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

/** Loại điểm mặc định tạo ở lần chạy đầu tiên (theo bảng nề nếp & học tập của lớp). */
object Seed {
    const val DEFAULT_BASE_SCORE = 100

    fun categories(): List<Category> = listOf(
        // ----- Cộng điểm (thành tích) -----
        Category(name = "Điểm KT/TX đạt 9–10", points = 7, sortOrder = 0),
        Category(name = "Điểm KT/TX đạt 7–8", points = 4, sortOrder = 1),
        Category(name = "Giờ học đạt loại Tốt", points = 2, sortOrder = 2),
        Category(name = "Phát biểu xây dựng bài", points = 2, sortOrder = 3),
        // ----- Trừ điểm (vi phạm) -----
        Category(name = "Nghỉ học không phép", points = -10, sortOrder = 10),
        Category(name = "Vắng chào cờ / sinh hoạt tập thể (không lý do)", points = -10, sortOrder = 11),
        Category(name = "Nói tục, gây mất đoàn kết", points = -10, sortOrder = 12),
        Category(name = "Không truy bài / mất trật tự 15 phút đầu giờ", points = -5, sortOrder = 13),
        Category(name = "Đi học muộn", points = -5, sortOrder = 14),
        Category(name = "Trực nhật muộn / không sạch sẽ", points = -5, sortOrder = 15),
        Category(name = "Vứt rác bừa bãi / ăn quà vặt trong trường", points = -5, sortOrder = 16),
        Category(name = "Không xếp hàng / không tập thể dục giữa giờ", points = -5, sortOrder = 17),
        Category(name = "Không soạn bài / không làm bài tập / không ghi bài", points = -5, sortOrder = 18),
        Category(name = "Giờ học chỉ đạt loại Khá", points = -3, sortOrder = 19),
        Category(name = "Không đồng phục / khăn quàng đỏ", points = -3, sortOrder = 20),
    )
}

class Repository(private val db: AppDatabase) {

    val members = db.memberDao().observeAll()
    val activeMembers = db.memberDao().observeActive()
    val categories = db.categoryDao().observeAll()
    val activeCategories = db.categoryDao().observeActive()
    val semesters = db.semesterDao().observeAll()
    val settings = db.settingDao().observe()

    fun scores(start: Long, end: Long) = db.scoreEntryDao().observeScores(start, end)

    fun weekly(start: Long, end: Long) = db.scoreEntryDao().observeWeekly(start, end)

    fun entries(start: Long, end: Long, memberId: Long?) =
        db.scoreEntryDao().observeEntries(start, end, memberId)

    suspend fun ensureSeed() {
        if (db.categoryDao().count() == 0) {
            db.categoryDao().insertAll(Seed.categories())
        }
        if (db.settingDao().get() == null) {
            db.settingDao().upsert(Setting(id = 1, weekBaseScore = Seed.DEFAULT_BASE_SCORE))
        }
    }

    suspend fun setWeekBaseScore(value: Int) {
        val current = db.settingDao().get() ?: Setting()
        db.settingDao().upsert(current.copy(weekBaseScore = value))
    }

    // ---- Thành viên ----
    suspend fun addMember(name: String): Long =
        db.memberDao().insert(Member(name = name.trim()))

    suspend fun updateMember(m: Member) = db.memberDao().update(m)

    suspend fun deleteMember(m: Member) = db.memberDao().delete(m)

    // ---- Loại điểm ----
    suspend fun addCategory(name: String, points: Int): Long =
        db.categoryDao().insert(Category(name = name.trim(), points = points))

    suspend fun updateCategory(c: Category) = db.categoryDao().update(c)

    suspend fun deleteCategory(c: Category) = db.categoryDao().delete(c)

    // ---- Học kì ----
    suspend fun addSemester(name: String, start: Long, end: Long): Long =
        db.semesterDao().insert(Semester(name = name.trim(), startEpochDay = start, endEpochDay = end))

    suspend fun updateSemester(s: Semester) = db.semesterDao().update(s)

    suspend fun deleteSemester(s: Semester) = db.semesterDao().delete(s)

    // ---- Lượt điểm ----
    suspend fun logEntry(
        memberIds: List<Long>,
        category: Category,
        date: LocalDate,
        note: String?,
    ) {
        val day = date.toEpochDay()
        val now = System.currentTimeMillis()
        val cleanNote = note?.trim()?.takeIf { it.isNotEmpty() }
        val list = memberIds.map { mid ->
            ScoreEntry(
                memberId = mid,
                categoryId = category.id,
                categoryName = category.name,
                points = category.points,
                dateEpochDay = day,
                note = cleanNote,
                createdAt = now,
            )
        }
        db.scoreEntryDao().insertAll(list)
    }

    suspend fun getEntry(id: Long): ScoreEntry? = db.scoreEntryDao().getById(id)

    suspend fun updateEntryFields(id: Long, points: Int, date: LocalDate, note: String?) =
        db.scoreEntryDao().updateFields(id, points, date.toEpochDay(), note?.trim()?.takeIf { it.isNotEmpty() })

    suspend fun updateEntry(e: ScoreEntry) = db.scoreEntryDao().update(e)

    suspend fun deleteEntry(e: ScoreEntry) = db.scoreEntryDao().delete(e)

    // ---- Sao lưu / phục hồi ----
    suspend fun exportJson(): String {
        val root = JSONObject()
        root.put("format", "chamdiem-to")
        root.put("version", 1)
        root.put("exportedAt", System.currentTimeMillis())

        val setting = db.settingDao().get() ?: Setting()
        root.put("weekBaseScore", setting.weekBaseScore)

        root.put("members", JSONArray().apply {
            db.memberDao().getAll().forEach { m ->
                put(
                    JSONObject()
                        .put("id", m.id)
                        .put("name", m.name)
                        .put("active", m.active)
                        .put("sortOrder", m.sortOrder)
                        .put("createdAt", m.createdAt)
                )
            }
        })
        root.put("categories", JSONArray().apply {
            db.categoryDao().getAll().forEach { c ->
                put(
                    JSONObject()
                        .put("id", c.id)
                        .put("name", c.name)
                        .put("points", c.points)
                        .put("active", c.active)
                        .put("sortOrder", c.sortOrder)
                )
            }
        })
        root.put("semesters", JSONArray().apply {
            db.semesterDao().getAll().forEach { s ->
                put(
                    JSONObject()
                        .put("id", s.id)
                        .put("name", s.name)
                        .put("startEpochDay", s.startEpochDay)
                        .put("endEpochDay", s.endEpochDay)
                )
            }
        })
        root.put("entries", JSONArray().apply {
            db.scoreEntryDao().getAll().forEach { e ->
                put(
                    JSONObject()
                        .put("id", e.id)
                        .put("memberId", e.memberId)
                        .put("categoryId", e.categoryId ?: JSONObject.NULL)
                        .put("categoryName", e.categoryName)
                        .put("points", e.points)
                        .put("dateEpochDay", e.dateEpochDay)
                        .put("note", e.note ?: JSONObject.NULL)
                        .put("createdAt", e.createdAt)
                )
            }
        })
        return root.toString(2)
    }

    /** Thay thế toàn bộ dữ liệu hiện tại bằng nội dung file sao lưu. */
    suspend fun importJson(text: String) {
        val root = JSONObject(text)

        val members = root.optJSONArray("members") ?: JSONArray()
        val categories = root.optJSONArray("categories") ?: JSONArray()
        val semesters = root.optJSONArray("semesters") ?: JSONArray()
        val entries = root.optJSONArray("entries") ?: JSONArray()

        val memberList = ArrayList<Member>(members.length())
        for (i in 0 until members.length()) {
            val o = members.getJSONObject(i)
            memberList.add(
                Member(
                    id = o.getLong("id"),
                    name = o.getString("name"),
                    active = o.optBoolean("active", true),
                    sortOrder = o.optInt("sortOrder", 0),
                    createdAt = o.optLong("createdAt", System.currentTimeMillis()),
                )
            )
        }
        val categoryList = ArrayList<Category>(categories.length())
        for (i in 0 until categories.length()) {
            val o = categories.getJSONObject(i)
            categoryList.add(
                Category(
                    id = o.getLong("id"),
                    name = o.getString("name"),
                    points = o.getInt("points"),
                    active = o.optBoolean("active", true),
                    sortOrder = o.optInt("sortOrder", 0),
                )
            )
        }
        val semesterList = ArrayList<Semester>(semesters.length())
        for (i in 0 until semesters.length()) {
            val o = semesters.getJSONObject(i)
            semesterList.add(
                Semester(
                    id = o.getLong("id"),
                    name = o.getString("name"),
                    startEpochDay = o.getLong("startEpochDay"),
                    endEpochDay = o.getLong("endEpochDay"),
                )
            )
        }
        val entryList = ArrayList<ScoreEntry>(entries.length())
        for (i in 0 until entries.length()) {
            val o = entries.getJSONObject(i)
            entryList.add(
                ScoreEntry(
                    id = o.getLong("id"),
                    memberId = o.getLong("memberId"),
                    categoryId = if (o.isNull("categoryId")) null else o.getLong("categoryId"),
                    categoryName = o.optString("categoryName", ""),
                    points = o.getInt("points"),
                    dateEpochDay = o.getLong("dateEpochDay"),
                    note = if (o.isNull("note")) null else o.getString("note"),
                    createdAt = o.optLong("createdAt", System.currentTimeMillis()),
                )
            )
        }

        db.scoreEntryDao().clear()
        db.memberDao().clear()
        db.categoryDao().clear()
        db.semesterDao().clear()

        db.memberDao().insertAll(memberList)
        db.categoryDao().insertAll(categoryList)
        db.semesterDao().insertAll(semesterList)
        // Chỉ giữ lượt điểm có thành viên tương ứng.
        val memberIds = memberList.mapTo(HashSet()) { it.id }
        db.scoreEntryDao().insertAll(entryList.filter { it.memberId in memberIds })

        val base = root.optInt("weekBaseScore", Seed.DEFAULT_BASE_SCORE)
        db.settingDao().upsert(Setting(id = 1, weekBaseScore = base))
        if (categoryList.isEmpty()) ensureSeed()
    }
}
