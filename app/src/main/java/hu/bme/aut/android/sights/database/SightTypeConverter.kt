package hu.bme.aut.android.sights.database

import androidx.room.TypeConverter
import hu.bme.aut.android.sights.model.Sight

class SightTypeConverter {

    companion object {
        const val MUSEUM = "MUSEUM"
        const val PARK = "PARK"
        const val OTHER = "OTHER"
        const val RELIGIOUS = "RELIGIOUS"
        const val NATURAL = "NATURAL"
    }

    // A enumokat nem tudja értelmezni a Room, ezért sztringekké alakítjuk át őket
    @TypeConverter
    fun toCategory(value: String?): Sight.Category {
        return when (value) {
            MUSEUM -> Sight.Category.MUSEUM
            PARK -> Sight.Category.PARK
            OTHER -> Sight.Category.OTHER
            RELIGIOUS -> Sight.Category.RELIGIOUS
            NATURAL -> Sight.Category.NATURAL
            else -> Sight.Category.OTHER
        }
    }

    @TypeConverter
    fun toString(enumValue: Sight.Category): String {
        return enumValue.name
    }

}