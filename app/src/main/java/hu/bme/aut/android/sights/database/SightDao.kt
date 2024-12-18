package hu.bme.aut.android.sights.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface SightDao {

    @Insert
    fun insertSight(sight: RoomSight)

    @Query("SELECT * FROM sight")
    fun getAllSights(): LiveData<List<RoomSight>>

    @Update
    fun updateSight(sight: RoomSight): Int

    @Delete
    fun deleteSight(sight: RoomSight)

    @Query("SELECT * FROM sight WHERE id == :id")
    fun getSightById(id: Int?): RoomSight?


}