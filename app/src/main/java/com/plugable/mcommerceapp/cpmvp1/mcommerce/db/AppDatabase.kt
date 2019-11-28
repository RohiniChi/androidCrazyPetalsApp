package com.plugable.mcommerceapp.cpmvp1.mcommerce.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.plugable.mcommerceapp.cpmvp1.mcommerce.models.Products

/**
 * [AppDatabase] is to instantiate database
 *
 */
@Database(entities = [(Products.Data.ProductDetails::class)], version = 3)
abstract class AppDatabase : RoomDatabase() {

    /**
     * productListDao is an abstract function
     *
     * @return dao interface
     */
    abstract fun productListDao(): FavoriteProductListDao

    companion object {

        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "wish_list"
                )
                    .allowMainThreadQueries()
                    .addMigrations(object : Migration(1, 2) {
                        override fun migrate(database: SupportSQLiteDatabase) {
                            database.execSQL("ALTER TABLE productWishList ADD COLUMN discountedPercentage INTEGER")
                        }
                    })
                    .addMigrations(object : Migration(2, 3) {
                        override fun migrate(database: SupportSQLiteDatabase) {
                            database.execSQL("ALTER TABLE productWishList ADD COLUMN isAvailable INTEGER DEFAULT 0 NOT NULL")
                        }
                    })
                    .build()
            }
            return INSTANCE as AppDatabase
        }

    }

}