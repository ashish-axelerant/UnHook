// UnHook — Room database with DAOs for all entities
package com.unhook.app.data.db

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
import com.unhook.app.data.model.BlockedApp
import com.unhook.app.data.model.ChoreItem
import com.unhook.app.data.model.Partner
import com.unhook.app.data.model.PointEvent
import com.unhook.app.data.model.ReminderMessage
import com.unhook.app.data.model.User
import com.unhook.app.data.model.WishItem
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE isMe = 1 LIMIT 1")
    fun getMe(): Flow<User?>

    @Query("SELECT * FROM users WHERE isMe = 1 LIMIT 1")
    suspend fun getMeOnce(): User?

    @Insert
    suspend fun insert(user: User): Long

    @Update
    suspend fun update(user: User)
}

@Dao
interface PartnerDao {
    @Query("SELECT * FROM partners LIMIT 1")
    fun getPartner(): Flow<Partner?>

    @Query("SELECT * FROM partners LIMIT 1")
    suspend fun getPartnerOnce(): Partner?

    @Insert
    suspend fun insert(partner: Partner): Long

    @Update
    suspend fun update(partner: Partner)
}

@Dao
interface PointEventDao {
    @Query("SELECT * FROM point_events WHERE userId = :userId ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentEvents(userId: Int, limit: Int = 5): Flow<List<PointEvent>>

    @Query("SELECT * FROM point_events WHERE userId = :userId ORDER BY timestamp DESC")
    fun getAllEvents(userId: Int): Flow<List<PointEvent>>

    @Query("SELECT * FROM point_events WHERE userId = :userId AND timestamp >= :since ORDER BY timestamp DESC")
    fun getEventsSince(userId: Int, since: Long): Flow<List<PointEvent>>

    @Query("SELECT COALESCE(SUM(points), 0) FROM point_events WHERE userId = :userId AND timestamp >= :since AND points > 0")
    suspend fun getPointsEarnedSince(userId: Int, since: Long): Int

    @Query("SELECT COALESCE(SUM(points), 0) FROM point_events WHERE userId = :userId AND timestamp >= :since AND points < 0")
    suspend fun getPointsLostSince(userId: Int, since: Long): Int

    @Query("SELECT COUNT(*) FROM point_events WHERE userId = :userId AND timestamp >= :since AND points > 0")
    suspend fun getResistCountSince(userId: Int, since: Long): Int

    @Insert
    suspend fun insert(event: PointEvent): Long
}

@Dao
interface BlockedAppDao {
    @Query("SELECT * FROM blocked_apps ORDER BY appName ASC")
    fun getAll(): Flow<List<BlockedApp>>

    @Query("SELECT packageName FROM blocked_apps WHERE isEnabled = 1")
    suspend fun getEnabledPackageNames(): List<String>

    @Query("SELECT appName FROM blocked_apps WHERE packageName = :packageName LIMIT 1")
    suspend fun getAppName(packageName: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(app: BlockedApp)

    @Update
    suspend fun update(app: BlockedApp)

    @Delete
    suspend fun delete(app: BlockedApp)

    @Query("SELECT COUNT(*) FROM blocked_apps")
    suspend fun count(): Int
}

@Dao
interface ReminderMessageDao {
    @Query("SELECT * FROM reminder_messages ORDER BY id ASC")
    fun getAll(): Flow<List<ReminderMessage>>

    @Query("SELECT * FROM reminder_messages ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandom(): ReminderMessage?

    @Insert
    suspend fun insert(message: ReminderMessage): Long

    @Delete
    suspend fun delete(message: ReminderMessage)

    @Query("SELECT COUNT(*) FROM reminder_messages")
    suspend fun count(): Int
}

@Dao
interface ChoreItemDao {
    @Query("SELECT * FROM chore_items WHERE createdByMe = :createdByMe ORDER BY id ASC")
    fun getItems(createdByMe: Boolean = true): Flow<List<ChoreItem>>

    @Query("SELECT * FROM chore_items ORDER BY id ASC")
    fun getAll(): Flow<List<ChoreItem>>

    @Query("SELECT * FROM chore_items WHERE isCompleted = 0 ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomPending(): ChoreItem?

    @Query("SELECT COUNT(*) FROM chore_items WHERE createdByMe = :createdByMe")
    suspend fun count(createdByMe: Boolean = true): Int

    @Insert
    suspend fun insert(item: ChoreItem): Long

    @Update
    suspend fun update(item: ChoreItem)

    @Delete
    suspend fun delete(item: ChoreItem)
}

@Dao
interface WishItemDao {
    @Query("SELECT * FROM wish_items WHERE createdByMe = :createdByMe ORDER BY id ASC")
    fun getItems(createdByMe: Boolean = true): Flow<List<WishItem>>

    @Query("SELECT * FROM wish_items ORDER BY id ASC")
    fun getAll(): Flow<List<WishItem>>

    @Query("SELECT * FROM wish_items WHERE isGranted = 0 ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomPending(): WishItem?

    @Query("SELECT COUNT(*) FROM wish_items WHERE createdByMe = :createdByMe")
    suspend fun count(createdByMe: Boolean = true): Int

    @Insert
    suspend fun insert(item: WishItem): Long

    @Update
    suspend fun update(item: WishItem)

    @Delete
    suspend fun delete(item: WishItem)
}

@Database(
    entities = [
        User::class, Partner::class, PointEvent::class, BlockedApp::class,
        ReminderMessage::class, ChoreItem::class, WishItem::class,
    ],
    version = 4,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun partnerDao(): PartnerDao
    abstract fun pointEventDao(): PointEventDao
    abstract fun blockedAppDao(): BlockedAppDao
    abstract fun reminderMessageDao(): ReminderMessageDao
    abstract fun choreItemDao(): ChoreItemDao
    abstract fun wishItemDao(): WishItemDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "unhook_database",
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
