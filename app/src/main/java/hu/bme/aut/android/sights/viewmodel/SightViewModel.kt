package hu.bme.aut.android.sights.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hu.bme.aut.android.sights.SightApplication
import hu.bme.aut.android.sights.model.Sight
import hu.bme.aut.android.sights.repository.Repository
import kotlinx.coroutines.launch

class SightViewModel : ViewModel() {

    private val repository: Repository

    val allSights: LiveData<List<Sight>>

    init {
        val sightDao = SightApplication.sightDatabase.sightDao()
        repository = Repository(sightDao)
        allSights = repository.getAllSights()
    }

    fun insert(sight: Sight) = viewModelScope.launch {
        repository.insert(sight)
    }

    fun delete(sight: Sight) = viewModelScope.launch {
        repository.delete(sight)
    }
}