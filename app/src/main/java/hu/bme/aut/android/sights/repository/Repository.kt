package hu.bme.aut.android.sights.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.google.android.gms.maps.model.LatLng
import hu.bme.aut.android.sights.database.RoomSight
import hu.bme.aut.android.sights.database.SightDao
import hu.bme.aut.android.sights.model.Sight
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class Repository(private val sightDao: SightDao) {

    fun getAllSights(): LiveData<List<Sight>> {
        return sightDao.getAllSights()
            .map {roomSights ->
                roomSights.map {roomSight ->
                    roomSight.toDomainModel() }
            }
    }

    suspend fun insert(sight: Sight) = withContext(Dispatchers.IO) {
        sightDao.insertSight(sight.toRoomModel())
    }

    private fun RoomSight.toDomainModel(): Sight {
        return Sight(
            id = id,
            name = name,
            address = address,
            category = category,
            coordinates = LatLng(latitude, longitude)
        )
    }

    // a LatLng-ot nem tudja értelmezni a Room, ezért két double lesz belőle
    // egy TypeConverternek viszont csak egy paramétere lehet, itt viszont kettőre lenne szükség
    // ezért itt végzem el az átalakítást
    private fun Sight.toRoomModel(): RoomSight {
        return RoomSight(
            name = name,
            address = address,
            category = category,
            latitude = coordinates.latitude,
            longitude = coordinates.longitude
        )
    }

    suspend fun delete(sight: Sight) = withContext(Dispatchers.IO) {
        val roomSight = sightDao.getSightById(sight.id) ?: return@withContext
        sightDao.deleteSight(roomSight)
    }

}