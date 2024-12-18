package hu.bme.aut.android.sights

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import hu.bme.aut.android.sights.adapter.SimpleItemRecyclerViewAdapter
import hu.bme.aut.android.sights.databinding.ActivityListBinding
import hu.bme.aut.android.sights.model.Sight
import hu.bme.aut.android.sights.viewmodel.SightViewModel

class ListActivity : AppCompatActivity(), SimpleItemRecyclerViewAdapter.SightItemClickListener {
    private lateinit var simpleItemRecyclerViewAdapter: SimpleItemRecyclerViewAdapter
    private lateinit var binding: ActivityListBinding
    private lateinit var sightViewModel: SightViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        binding.toolbar.title = title

        setupRecyclerView()
        sightViewModel = ViewModelProvider(this)[SightViewModel::class.java]
        sightViewModel.allSights.observe(this, { sights ->
            simpleItemRecyclerViewAdapter.submitList(sights)
        })

        showIntroDialog()
    }

    private fun showIntroDialog() {
        AlertDialog.Builder(this)
                .setMessage(R.string.list_intro)
                .setPositiveButton(R.string.got_it) { dialog, which ->
                }
                .show()
    }

    private fun setupRecyclerView() {
        simpleItemRecyclerViewAdapter = SimpleItemRecyclerViewAdapter()
        simpleItemRecyclerViewAdapter.itemClickListener = this
        binding.root.findViewById<RecyclerView>(R.id.sight_list).adapter = simpleItemRecyclerViewAdapter
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_list, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.itemAddSight) {
            val i = Intent(this, SearchActivity::class.java)
            startActivity(i)
        }
        if (item.itemId == R.id.itemToMap) {
            val i = Intent(this, MapActivity::class.java)
            startActivity(i)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onItemClick(sight: Sight) {
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

    private fun goToNavigation(sight: Sight) {
        val gmmIntentUri =
                Uri.parse("google.navigation:q=${sight.coordinates.latitude}, ${sight.coordinates.longitude}")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        try {
            startActivity(mapIntent)
        }
        // ilyen kivétel jön, ha az implicit Intent nem talál megfelelő Activityt
        catch (exception: ActivityNotFoundException) {
            Toast.makeText(applicationContext, getString(R.string.no_activity), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onItemLongClick(position: Int, view: View, sight: Sight): Boolean {
        val popup = PopupMenu(this, view)
        popup.inflate(R.menu.menu_sight)
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.delete -> {
                    sightViewModel.delete(sight)
                    return@setOnMenuItemClickListener true
                }
            }
            false
        }
        popup.show()
        return false
    }

}