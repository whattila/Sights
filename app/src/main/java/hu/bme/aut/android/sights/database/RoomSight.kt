package hu.bme.aut.android.sights.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import hu.bme.aut.android.sights.model.Sight

@Entity(tableName = "sight")
data class RoomSight(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val address: String,
    val category: Sight.Category,
    val latitude: Double,
    val longitude: Double
)
