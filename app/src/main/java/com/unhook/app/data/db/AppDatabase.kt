// UnHook — Room database with DAOs for all entities
package com.unhook.app.data.db

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import com.unhook.app.data.model.Partner
import com.unhook.app.data.model.PointEvent
import com.unhook.app.data.model.User
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

    @Insert
    suspend fun insert(event: PointEvent): Long
}

@Database(
    entities = [User::class, Partner::class, PointEvent::class],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun partnerDao(): PartnerDao
    abstract fun pointEventDao(): PointEventDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "unhook_database",
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
