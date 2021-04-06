package am.todomanager.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [EntityListElement::class, EntitySettings::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun daoElements() : DaoElements
    abstract fun daoSettings() : DaoSettings

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "todo_database"
                ).addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // insert the data on the IO Thread
                        ioThread {
                            getDatabase(context).daoSettings().insert(EntitySettings(0, 0, 0))
                        }
                    }
                })
                    .build()
                INSTANCE = instance
                // return instance
                instance
            }
        }

    }
}