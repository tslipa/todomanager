package am.todomanager.database

import androidx.room.*

@Dao
interface DaoElements {
    @Insert
    suspend fun insert(element: EntityListElement)

    @Update
    suspend fun update(element: EntityListElement)

    @Delete
    suspend fun delete(element: EntityListElement)

    @Query("SELECT * FROM Elements")
    suspend fun getAll(): List<EntityListElement>

    @Query("DELETE FROM Elements")
    suspend fun clearDB()

    @Query("DELETE FROM Elements WHERE le_id = :id")
    suspend fun deleteElement(id: Int)
}