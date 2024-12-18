package hu.bme.aut.android.sights

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import hu.bme.aut.android.sights.adapter.SimpleItemRecyclerViewAdapter
import hu.bme.aut.android.sights.databinding.ActivitySearchBinding
import hu.bme.aut.android.sights.model.Sight
import hu.bme.aut.android.sights.network.PlaceHelper
import hu.bme.aut.android.sights.viewmodel.SightViewModel
import permissions.dispatcher.*

// A kód forrása részben: https://developers.google.com/maps/documentation/android-sdk/current-place-tutorial

@RuntimePermissions
class SearchActivity : AppCompatActivity(), PlaceHelper.OnResultListener {
    private lateinit var simpleItemRecyclerViewAdapter: SimpleItemRecyclerViewAdapter
    private lateinit var binding: ActivitySearchBinding
    private lateinit var sightViewModel: SightViewModel

    private lateinit var placeHelper: PlaceHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)
        setupRecyclerView()

        binding.btnCitySearch.setOnClickListener {
            Toast.makeText(applicationContext, getString(R.string.not_available), Toast.LENGTH_SHORT).show()
        }

        binding.btnNearbySearch.setOnClickListener {
            showCurrentPlaceWithPermissionCheck()
        }

        binding.btnSave.setOnClickListener {
            saveSights()
        }

        sightViewModel = ViewModelProvider(this)[SightViewModel::class.java]

        placeHelper = PlaceHelper(applicationContext, this)
    }

    private fun findSightsInCity() {
        val cityName = binding.etCity.text.toString()
        if (cityName != "")
            placeHelper.getPlacesInCity(cityName)
        else
            Toast.makeText(applicationContext, getString(R.string.no_city), Toast.LENGTH_SHORT).show()
    }

    private fun saveSights() {
        val selectedPlaces = simpleItemRecyclerViewAdapter.selectedItems

        if (selectedPlaces.isNotEmpty()) {
            for (sight in selectedPlaces)
                sight?.let { sightViewModel.insert(it) }
            finish()
        }
        else
            Toast.makeText(applicationContext, getString(R.string.no_selected), Toast.LENGTH_SHORT).show()
    }

    private fun setupRecyclerView() {
        simpleItemRecyclerViewAdapter = SimpleItemRecyclerViewAdapter()
        binding.root.findViewById<RecyclerView>(R.id.sight_list).adapter = simpleItemRecyclerViewAdapter
    }

    @NeedsPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    fun showCurrentPlace() {
        placeHelper.getNearbyPlaces()
    }

    @OnPermissionDenied(Manifest.permission.ACCESS_FINE_LOCATION)
    fun onLocationDenied() {
        Toast.makeText(this, getString(R.string.permission_denied_location), Toast.LENGTH_SHORT).show()
    }

    @OnShowRationale(Manifest.permission.ACCESS_FINE_LOCATION)
    fun showRationaleForLocation(request: PermissionRequest) {
        val alertDialog = AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(R.string.location_permission_explanation)
            .setCancelable(false)
            .setPositiveButton(R.string.proceed) { dialog, id -> request.proceed() }
            .setNegativeButton(R.string.exit) { dialog, id -> request.cancel() }
            .create()
        alertDialog.show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // NOTE: delegate the permission handling to generated method
        onRequestPermissionsResult(requestCode, grantResults)
    }

    override fun onResultsReady(results: List<Sight>) {
        if (results.isNotEmpty()) {
            simpleItemRecyclerViewAdapter.submitList(results)
            binding.btnSave.isEnabled = true
        }
        // Adok visszajelzést akkor is, ha nincsenek találatok
        else
            Toast.makeText(applicationContext, getString(R.string.no_results), Toast.LENGTH_SHORT).show()
    }

    override fun onOneResultReady(result: Sight) {
        val newList = simpleItemRecyclerViewAdapter.currentList
        newList.add(result)
        simpleItemRecyclerViewAdapter.submitList(newList)
    }
}