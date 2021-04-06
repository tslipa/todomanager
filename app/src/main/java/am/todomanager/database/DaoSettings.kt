package am.todomanager.database

import androidx.room.*

@Dao
interface DaoSettings {
    @Insert
    fun insert(element: EntitySettings)

    @Update
    fun update(element: EntitySettings)

    @Delete
    fun delete(element: EntitySettings)

    @Query("SELECT * FROM Settings WHERE id = 0")
    fun get() : EntitySettings

    @Query("SELECT COUNT(*) FROM Settings")
    fun count() : Int
}