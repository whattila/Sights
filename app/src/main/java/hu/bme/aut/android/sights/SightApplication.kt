package hu.bme.aut.android.sights

import android.app.Application
import androidx.room.Room
import hu.bme.aut.android.sights.database.SightDatabase

class SightApplication : Application() {

	companion object {
		lateinit var sightDatabase: SightDatabase
	  		private set
	}
	
	override fun onCreate() {
		super.onCreate()

		sightDatabase = Room.databaseBuilder(
                    applicationContext,
                    SightDatabase::class.java,
                    "sight_database"
                ).fallbackToDestructiveMigration().build()
	}
	
}