package am.todomanager.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Settings")
data class EntitySettings(
    @PrimaryKey val id: Int,
    val sort: Int,
    val maxId: Int
)
