package hu.bme.aut.android.sights

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import hu.bme.aut.android.sights.databinding.ActivityMapBinding
import hu.bme.aut.android.sights.model.Sight
import hu.bme.aut.android.sights.viewmodel.SightViewModel

// A kód forrása részben: https://developers.google.com/maps/documentation/android-sdk/current-place-tutorial

class MapActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var binding: ActivityMapBinding
    private lateinit var sightViewModel: SightViewModel
    private var map: GoogleMap? = null

    private val defaultLocation = LatLng(-33.8523341, 151.2106085)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        sightViewModel = ViewModelProvider(this)[SightViewModel::class.java]

        sightViewModel.allSights.observe(this, { sights ->
            addSightMarkers(sights)
        })

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        showIntroDialog()
    }

    private fun showIntroDialog() {
        AlertDialog.Builder(this)
                .setMessage(R.string.map_intro)
                .setPositiveButton(R.string.got_it) { dialog, which ->
                }
                .show()
    }

    private fun addSightMarkers(sights: List<Sight>) {
        // nehogy véletlenül valamit kétszer tegyünk ki, azért van a clear
        // csak akkor adunk hozzá markereket, ha már kész a térkép
        map?.clear()
        for (sight in sights) {
            val marker = map?.addMarker(
                    MarkerOptions()
                            .position(sight.coordinates)
                            .title(sight.name)
                            .snippet("${sight.address}, ${sight.category}")
            )
            marker?.tag = sight
        }
        map?.moveCamera(CameraUpdateFactory.newLatLngZoom(sights[sights.size - 1].coordinates,
                DEFAULT_ZOOM.toFloat()))
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_map, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.itemAddSight) {
            val i = Intent(this, SearchActivity::class.java)
            startActivity(i)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        googleMap.uiSettings.isZoomControlsEnabled = true

        // Legyen hova vinni a kamerát akkor is, ha épp nincsenek látnivalóink
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, DEFAULT_ZOOM.toFloat()))

        // Azért állítok be InfoWindowAdaptert, hogy több soros szövegek is megjelenjenek az InfoWindow-kban
        this.map?.setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter {
            // Return null here, so that getInfoContents() is called next.
            override fun getInfoWindow(arg0: Marker): View? {
                return null
            }

            override fun getInfoContents(marker: Marker): View {
                // Inflate the layouts for the info window, title and snippet.
                val infoWindow = layoutInflater.inflate(R.layout.custom_info_contents,
                        findViewById<FrameLayout>(R.id.map), false)
                val title = infoWindow.findViewById<TextView>(R.id.title)
                title.text = marker.title
                val snippet = infoWindow.findViewById<TextView>(R.id.snippet)
                snippet.text = marker.snippet

                return infoWindow
            }
        })

        map?.setOnInfoWindowClickListener { marker ->
            val sight = marker.tag as Sight
            showNavigationDialog(sight)
        }

        map?.setOnInfoWindowLongClickListener { marker ->
            val sight = marker.tag as Sight
            showDeleteDialog(sight)
        }
    }

    private fun showDeleteDialog(sight: Sight) {
        AlertDialog.Builder(this)
                .setTitle(R.string.delete_sight)
                .setPositiveButton(R.string.dialog_positive) { dialog, which ->
                    sightViewModel.delete(sight)
                }
                .setNegativeButton(R.string.dialog_negative) { dialog, which ->
                }
                .show()
    }

    private fun showNavigationDialog(sight: Sight) {
        AlertDialog.Builder(this)
                .setTitle(R.string.start_navigation)
                .setMessage(R.string.navigation_message)
                .setPositiveButton(R.string.dialog_positive) { dialog, which ->
                    goToNavigation(sight)
                }
                .setNegativeButton(R.string.dialog_negative) { dialog, which ->
                }
                .show()
    }

    // Ugyanaz, mint a ListActivityben
    private fun goToNavigation(sight: Sight) {
        val gmmIntentUri =
                Uri.parse("google.navigation:q=${sight.coordinates.latitude}, ${sight.coordinates.longitude}")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        try {
            startActivity(mapIntent)
        }
        catch (exception: ActivityNotFoundException) {
            Toast.makeText(applicationContext, getString(R.string.no_activity), Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val DEFAULT_ZOOM = 15
    }
}