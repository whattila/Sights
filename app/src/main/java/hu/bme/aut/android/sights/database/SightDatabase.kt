package hu.bme.aut.android.sights.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    version = 1,
    exportSchema = false,
    entities = [RoomSight::class]
)
@TypeConverters(
    SightTypeConverter::class
)
abstract class SightDatabase : RoomDatabase() {

    abstract fun sightDao(): SightDao

}