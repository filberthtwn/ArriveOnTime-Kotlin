package com.omkarsoft.arriveontimedelivery.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.omkarsoft.arriveontimedelivery.data.model.Order
import com.omkarsoft.arriveontimedelivery.data.rest.OrderDatabaseDao

@Database(
    entities = [Order::class],
    version = 1
)
abstract class AppDatabase: RoomDatabase() {
    abstract fun orderDao(): OrderDatabaseDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null
        private var LOCK = Any()

        operator fun invoke(context: Context) = instance ?: synchronized(LOCK){
            instance ?: buildDatabase(context).also {
                instance = it
            }
        }

        private fun buildDatabase(context: Context) = Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "app-database"
        ).allowMainThreadQueries().build()
    }
}