package io.euruapp.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import io.euruapp.model.User

@Database(entities = [User::class], version = 1, exportSchema = false)
abstract class RoomAppDatabase : RoomDatabase() {

    abstract fun dao(): RoomAppDao

    companion object {
        @Volatile
        private var instance: RoomAppDatabase? = null

        @JvmStatic
        fun getInstance(context: Context): RoomAppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context,
                    RoomAppDatabase::class.java,
                    "euru_room_db"
                ).fallbackToDestructiveMigration().build().also { instance = it }
            }
        }
    }

}