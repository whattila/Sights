package hu.bme.aut.android.sights.model

import com.google.android.gms.maps.model.LatLng

data class Sight(
    val id: Int,
    val name: String,
    val address: String,
    val category: Category,
    val coordinates: LatLng
) {
    enum class Category {
        MUSEUM, PARK, OTHER, RELIGIOUS, NATURAL;

        // This is for conversion between the model and the UI
        // To make it more elegant
        override fun toString(): String = when(this) {
            MUSEUM -> "Museum"
            PARK -> "Public park"
            OTHER -> "Other sight"
            RELIGIOUS -> "Religious place"
            NATURAL -> "Natural feature"
        }
    }
}
