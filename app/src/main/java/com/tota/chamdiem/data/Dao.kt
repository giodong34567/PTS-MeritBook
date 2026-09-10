package com.tota.chamdiem.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MemberDao {
    @Query("SELECT * FROM members ORDER BY active DESC, sortOrder, name COLLATE NOCASE")
    fun observeAll(): Flow<List<Member>>

    @Query("SELECT * FROM members WHERE active = 1 ORDER BY sortOrder, name COLLATE NOCASE")
    fun observeActive(): Flow<List<Member>>

    @Query("SELECT COUNT(*) FROM members")
    suspend fun count(): Int

    @Query("SELECT * FROM members")
    suspend fun getAll(): List<Member>

    @Insert suspend fun insert(m: Member): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<Member>)

    @Update suspend fun update(m: Member)

    @Delete suspend fun delete(m: Member)

    @Query("DELETE FROM members")
    suspend fun clear()
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY active DESC, points < 0, sortOrder, name COLLATE NOCASE")
    fun observeAll(): Flow<List<Category>>

    @Query("SELECT * FROM categories WHERE active = 1 ORDER BY points < 0, sortOrder, name COLLATE NOCASE")
    fun observeActive(): Flow<List<Category>>

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun count(): Int

    @Query("SELECT * FROM categories")
    suspend fun getAll(): List<Category>

    @Insert suspend fun insert(c: Category): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<Category>)

    @Update suspend fun update(c: Category)

    @Delete suspend fun delete(c: Category)

    @Query("DELETE FROM categories")
    suspend fun clear()
}

@Dao
interface SemesterDao {
    @Query("SELECT * FROM semesters ORDER BY startEpochDay DESC")
    fun observeAll(): Flow<List<Semester>>

    @Query("SELECT * FROM semesters")
    suspend fun getAll(): List<Semester>

    @Insert suspend fun insert(s: Semester): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<Semester>)

    @Update suspend fun update(s: Semester)

    @Delete suspend fun delete(s: Semester)

    @Query("DELETE FROM semesters")
    suspend fun clear()
}

@Dao
interface SettingDao {
    @Query("SELECT * FROM settings WHERE id = 1")
    fun observe(): Flow<Setting?>

    @Query("SELECT * FROM settings WHERE id = 1")
    suspend fun get(): Setting?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(s: Setting)
}

@Dao
interface ScoreEntryDao {
    @Insert suspend fun insert(e: ScoreEntry): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<ScoreEntry>)

    @Update suspend fun update(e: ScoreEntry)

    @Delete suspend fun delete(e: ScoreEntry)

    @Query("DELETE FROM score_entries")
    suspend fun clear()

    @Query("SELECT * FROM score_entries")
    suspend fun getAll(): List<ScoreEntry>

    @Query("SELECT * FROM score_entries WHERE id = :id")
    suspend fun getById(id: Long): ScoreEntry?

    @Query("UPDATE score_entries SET points = :points, dateEpochDay = :day, note = :note WHERE id = :id")
    suspend fun updateFields(id: Long, points: Int, day: Long, note: String?)

    @Query(
        """
        SELECT e.id AS id, e.memberId AS memberId, m.name AS memberName,
               e.categoryName AS categoryName, e.points AS points,
               e.dateEpochDay AS dateEpochDay, e.note AS note, e.createdAt AS createdAt
        FROM score_entries e
        JOIN members m ON m.id = e.memberId
        WHERE (:memberId IS NULL OR e.memberId = :memberId)
          AND e.dateEpochDay BETWEEN :start AND :end
        ORDER BY e.dateEpochDay DESC, e.createdAt DESC
        """
    )
    fun observeEntries(start: Long, end: Long, memberId: Long?): Flow<List<EntryWithMember>>

    @Query(
        """
        SELECT m.id AS memberId, m.name AS memberName,
          COALESCE(SUM(CASE WHEN e.points > 0 THEN 1 ELSE 0 END), 0) AS plusCount,
          COALESCE(SUM(CASE WHEN e.points < 0 THEN 1 ELSE 0 END), 0) AS minusCount,
          COALESCE(SUM(CASE WHEN e.points > 0 THEN e.points ELSE 0 END), 0) AS plusPoints,
          COALESCE(SUM(CASE WHEN e.points < 0 THEN e.points ELSE 0 END), 0) AS minusPoints,
          COALESCE(SUM(e.points), 0) AS net
        FROM members m
        LEFT JOIN score_entries e
          ON e.memberId = m.id AND e.dateEpochDay BETWEEN :start AND :end
        WHERE m.active = 1
        GROUP BY m.id, m.name
        ORDER BY net DESC, m.name COLLATE NOCASE ASC
        """
    )
    fun observeScores(start: Long, end: Long): Flow<List<MemberScore>>

    @Query(
        """
        SELECT m.id AS memberId, m.name AS memberName,
          ((e.dateEpochDay + 3) / 7) AS weekIndex,
          COALESCE(SUM(CASE WHEN e.points > 0 THEN 1 ELSE 0 END), 0) AS plusCount,
          COALESCE(SUM(CASE WHEN e.points < 0 THEN 1 ELSE 0 END), 0) AS minusCount,
          COALESCE(SUM(CASE WHEN e.points > 0 THEN e.points ELSE 0 END), 0) AS plusPoints,
          COALESCE(SUM(CASE WHEN e.points < 0 THEN e.points ELSE 0 END), 0) AS minusPoints,
          COALESCE(SUM(e.points), 0) AS net
        FROM members m
        JOIN score_entries e ON e.memberId = m.id
        WHERE m.active = 1 AND e.dateEpochDay BETWEEN :start AND :end
        GROUP BY m.id, m.name, weekIndex
        """
    )
    fun observeWeekly(start: Long, end: Long): Flow<List<WeekMemberAgg>>
}

@Database(
    entities = [Member::class, Category::class, ScoreEntry::class, Semester::class, Setting::class],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun memberDao(): MemberDao
    abstract fun categoryDao(): CategoryDao
    abstract fun scoreEntryDao(): ScoreEntryDao
    abstract fun semesterDao(): SemesterDao
    abstract fun settingDao(): SettingDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "chamdiem.db",
            ).build().also { INSTANCE = it }
        }
    }
}
