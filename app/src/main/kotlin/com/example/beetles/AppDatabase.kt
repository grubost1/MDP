package com.example.beetles

import android.content.Context
import androidx.room.*

@Entity(
    tableName = "records",
    indices = [Index(value = ["playerId"], unique = true)],
    foreignKeys = [ForeignKey(
        entity = Player::class,
        parentColumns = ["id"],
        childColumns = ["playerId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class PlayerRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val playerId: Int,
    val score: Int,
    val difficultyPreset: String,
    val dateString: String
)

@Dao
interface RecordDao {
    @Query("SELECT * FROM records ORDER BY score DESC")
    fun getAllRecordsDesc(): List<PlayerRecord>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrUpdateRecord(record: PlayerRecord)

    @Query("SELECT * FROM records WHERE playerId = :playerId LIMIT 1")
    fun getRecordByPlayer(playerId: Int): PlayerRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPlayer(player: Player): Long

    @Query("SELECT * FROM players WHERE fullName = :name LIMIT 1")
    fun getPlayerByName(name: String): Player?

    @Query("SELECT * FROM players")
    fun getAllPlayers(): List<Player>
}

@Database(entities = [Player::class, PlayerRecord::class], version = 3, exportSchema = false)
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
                )
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build()
                instance = instanceBuilding
                instanceBuilding
            }
        }
    }
}