package com.example.beetles

import android.content.Context
import androidx.room.*

@Entity(tableName = "records")
data class PlayerRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val fullName: String,
    val gender: String,
    val course: String,
    val difficultyPreset: String,
    val maxScore: Int,
    val dateString: String
)

@Dao
interface RecordDao {
    @Query("SELECT * FROM records ORDER BY maxScore DESC")
    fun getAllRecordsDesc(): List<PlayerRecord>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertRecord(record: PlayerRecord)
}

@Database(entities = [PlayerRecord::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun recordDao(): RecordDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                val instanceBuilding = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "beetles_database"
                ).allowMainThreadQueries().build()
                instance = instanceBuilding
                instanceBuilding
            }
        }
    }
}
