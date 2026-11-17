package com.example.kuiper.presentation.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "daily_steps")
data class DailyStepEntity(
    @PrimaryKey val date: String,
    val steps: Int,
    val goal: Int
)

@Dao
interface StepDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(day: DailyStepEntity)

    @Query("SELECT * FROM daily_steps ORDER BY date DESC LIMIT 7")
    fun getLast7Days(): Flow<List<DailyStepEntity>>
}

@Database(entities = [DailyStepEntity::class], version = 1, exportSchema = false)
abstract class StepDatabase : RoomDatabase() {
    abstract fun stepDao(): StepDao

    companion object {
        @Volatile private var INSTANCE: StepDatabase? = null
        fun getDatabase(context: Context): StepDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    StepDatabase::class.java,
                    "step_database"
                ).build().also { INSTANCE = it }
            }
        }
    }
}