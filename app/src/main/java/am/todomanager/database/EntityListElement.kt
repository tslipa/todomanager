package am.todomanager.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Elements")
data class EntityListElement(
    @PrimaryKey val le_id: Int,
    val title: String?,
    val description: String?,
    val date: Long,
    val time: Long,
    val icon: Int,
    val rating: Float,
    val edited: Long,
    val notification: Int
)
