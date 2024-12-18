package hu.bme.aut.android.sights.network

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import hu.bme.aut.android.sights.BuildConfig
import hu.bme.aut.android.sights.model.Sight
import kotlin.random.Random

// A kód forrása részben: https://developers.google.com/maps/documentation/android-sdk/current-place-tutorial

class PlaceHelper(private val context: Context, private val listener: OnResultListener) {

    private val placesClient: PlacesClient

    init {
        // Construct a PlacesClient
        Places.initialize(context, BuildConfig.MAPS_API_KEY)
        placesClient = Places.createClient(context)
    }

    // A közelünkben lévő helyek lekérése a PlacesClient FindCurrentPlace szolgáltatásával
    // Az engedélykérés a tartalmazó (Activity) objektum feladata
    @SuppressLint("MissingPermission")
    fun getNearbyPlaces() {
        // Ezeket az adatmezőket kérjük le, mert ezek felelnek meg a modellosztályunk propertyjeinek
        val placeFields = listOf(Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG, Place.Field.TYPES)

        // Use the builder to create a FindCurrentPlaceRequest.
        val request = FindCurrentPlaceRequest.newInstance(placeFields)

        val placeResult = placesClient.findCurrentPlace(request)
        placeResult.addOnCompleteListener { task ->
            try {
                if (task.isSuccessful && task.result != null) {
                    val likelyPlaces = task.result
                    val placeList = mutableListOf<Sight>()

                    for (placeLikelihood in likelyPlaces?.placeLikelihoods ?: emptyList()) {
                        // mindenféle (nem az alkalmazás profiljába vágó) helyet visszakapunk, ezért kiválogatjuk a modellünknek megfelelő kategóriájúakat
                        val sight = buildSight(placeLikelihood.place)
                        if (sight != null)
                            placeList.add(sight)
                    }
                    listener.onResultsReady(placeList)
                }
            }
            catch (e: Exception) {
                Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun getPlacesInCity(city: String) {
        val token = AutocompleteSessionToken.newInstance()

        val request =
            FindAutocompletePredictionsRequest.builder()
                .setSessionToken(token)
                .setQuery(QUERY_PREFIX + city)
                .build()

        val predictionsResult = placesClient.findAutocompletePredictions(request)
        predictionsResult.addOnCompleteListener { task ->
            try {
                if (task.isSuccessful && task.result != null) {
                    val predictions = task.result
                    for (prediction in predictions.autocompletePredictions) {
                        val fetchRequest = FetchPlaceRequest.builder(prediction.placeId,
                                listOf(Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG, Place.Field.TYPES))
                                .setSessionToken(token)
                                .build()
                        val fetchResult = placesClient.fetchPlace(fetchRequest)
                        fetchResult.addOnCompleteListener {fetchTask ->
                            try {
                                if (fetchTask.isSuccessful && fetchTask.result != null) {
                                    Log.d("map", fetchTask.result.place.name)
                                    val sight = buildSight(fetchTask.result.place)
                                    if (sight != null)
                                        listener.onOneResultReady(sight)
                                }
                            }
                            catch (e: Exception) {
                                Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
            catch (e: Exception) {
                Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Akkor hozzuk létre az modellobjektumunkat, ha a Google-ös kategóriája megfeleltethető volt a mi kategóriáink egyikének,
    // és minden más szükséges mező is megvan
    private fun buildSight(place: Place): Sight? {
        val category = getCategory(place.types)
        if (place.name != null &&
            place.address != null &&
            category != null &&
            place.latLng != null)
                return Sight(
                    id = Random.nextInt(),
                    name = place.name,
                    address = place.address,
                    category = category,
                    coordinates = place.latLng
                )
        return null
    }

    // Egy Place-nek több típusa is lehet, mely alapján több Sight.Categoryba is besorolható lehetne egyes esetekben
    // de egy Sight objektumnak csak egy kategóriája lehet
    // Ezért én felállítottam egy "erősségi" sorrendet
    // pl. egy templom lehet esetleg múzeum is, de vallási helyként veszem számításba csak
    // Other csak akkor lesz, ha TOURIST_ATTRACTION, de egyik másik "erősebb" típusú sem
    private fun getCategory(possibleTypes: List<Place.Type>?) : Sight.Category? {
        if (possibleTypes == null)
            return null
        if (possibleTypes.contains(Place.Type.CHURCH) ||
            possibleTypes.contains(Place.Type.HINDU_TEMPLE) ||
            possibleTypes.contains(Place.Type.MOSQUE) ||
            possibleTypes.contains(Place.Type.PLACE_OF_WORSHIP) ||
            possibleTypes.contains(Place.Type.SYNAGOGUE))
                return Sight.Category.RELIGIOUS
        if (possibleTypes.contains(Place.Type.AQUARIUM) ||
            possibleTypes.contains(Place.Type.ART_GALLERY) ||
            possibleTypes.contains(Place.Type.MUSEUM))
                return Sight.Category.MUSEUM
        if (possibleTypes.contains(Place.Type.PARK))
                return Sight.Category.PARK
        if (possibleTypes.contains(Place.Type.NATURAL_FEATURE))
                return Sight.Category.NATURAL
        if (possibleTypes.contains(Place.Type.TOURIST_ATTRACTION))
                return Sight.Category.OTHER
        return null
    }

    interface OnResultListener {
        fun onResultsReady(results: List<Sight>)
        fun onOneResultReady(result: Sight)
    }

    companion object {
        private const val QUERY_PREFIX = "Tourist attractions in "
    }
}