package com.example.data.local

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
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.AlarmEntity
import com.example.data.model.TimerEntity
import com.example.data.model.WorldCityEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AlarmDao {
    @Query("SELECT * FROM alarms ORDER BY orderIndex ASC, id ASC")
    fun getAllAlarms(): Flow<List<AlarmEntity>>

    @Query("SELECT * FROM alarms ORDER BY orderIndex ASC, id ASC")
    suspend fun getAllAlarmsList(): List<AlarmEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(alarm: AlarmEntity): Long

    @Update
    suspend fun update(alarm: AlarmEntity)

    @Update
    suspend fun updateAll(alarms: List<AlarmEntity>)

    @Delete
    suspend fun delete(alarm: AlarmEntity)

    @Query("DELETE FROM alarms WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)

    @Query("DELETE FROM alarms")
    suspend fun deleteAllAlarms()

    @Query("SELECT * FROM alarms WHERE id = :id LIMIT 1")
    suspend fun getAlarmById(id: Long): AlarmEntity?
}

@Dao
interface TimerDao {
    @Query("SELECT * FROM timers ORDER BY orderIndex ASC, id ASC")
    fun getAllTimers(): Flow<List<TimerEntity>>

    @Query("SELECT * FROM timers ORDER BY orderIndex ASC, id ASC")
    suspend fun getAllTimersList(): List<TimerEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(timer: TimerEntity): Long

    @Update
    suspend fun update(timer: TimerEntity)

    @Update
    suspend fun updateAll(timers: List<TimerEntity>)

    @Delete
    suspend fun delete(timer: TimerEntity)

    @Query("DELETE FROM timers WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)

    @Query("DELETE FROM timers")
    suspend fun deleteAllTimers()

    @Query("SELECT * FROM timers WHERE id = :id LIMIT 1")
    suspend fun getTimerById(id: Long): TimerEntity?
}

@Dao
interface WorldCityDao {
    @Query("SELECT * FROM world_cities ORDER BY orderIndex ASC, id ASC")
    fun getAllCities(): Flow<List<WorldCityEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(city: WorldCityEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(cities: List<WorldCityEntity>)

    @Update
    suspend fun updateAll(cities: List<WorldCityEntity>)

    @Delete
    suspend fun delete(city: WorldCityEntity)

    @Query("DELETE FROM world_cities WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)

    @Query("SELECT COUNT(*) FROM world_cities")
    suspend fun getCount(): Int
}

@Database(
    entities = [AlarmEntity::class, TimerEntity::class, WorldCityEntity::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun alarmDao(): AlarmDao
    abstract fun timerDao(): TimerDao
    abstract fun worldCityDao(): WorldCityDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE alarms ADD COLUMN skippedUntilMillis INTEGER NOT NULL DEFAULT 0")
            }
        }

        fun getInstance(context: Context): AppDatabase = getDatabase(context)

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "epic_alarm_database"
                )
                .addMigrations(MIGRATION_3_4)
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
