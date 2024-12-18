package hu.bme.aut.android.sights.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.CompoundButton
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import hu.bme.aut.android.sights.databinding.RowSightBinding
import hu.bme.aut.android.sights.model.Sight

class SimpleItemRecyclerViewAdapter : ListAdapter<Sight, SimpleItemRecyclerViewAdapter.ViewHolder>(ItemCallback) {

    val selectedItems = mutableListOf<Sight?>()

    companion object{
        object ItemCallback : DiffUtil.ItemCallback<Sight>(){
            override fun areItemsTheSame(oldItem: Sight, newItem: Sight): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Sight, newItem: Sight): Boolean {
                return oldItem == newItem
            }
        }
    }

    var itemClickListener: SightItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        RowSightBinding.inflate(
            LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val sight = this.getItem(position)

        holder.sight = sight

        holder.binding.tvName.text = sight.name
        holder.binding.tvAddress.text = sight.address
	    holder.binding.tvCategory.text = sight.category.toString()
    }

    inner class ViewHolder(val binding: RowSightBinding) : RecyclerView.ViewHolder(binding.root) {
        var sight: Sight? = null
        val checkBox: CheckBox

        init {
            checkBox = binding.checkBox
            itemView.setOnClickListener {
                sight?.let { sight -> itemClickListener?.onItemClick(sight) }
            }

            itemView.setOnLongClickListener { view ->
                sight?.let {sight -> itemClickListener?.onItemLongClick(adapterPosition, view, sight) }
                true
            }
            checkBox.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
                run {
                    if (sight != null) {
                        if (isChecked)
                            selectedItems.add(sight)
                        else
                            selectedItems.remove(sight)
                    }
                }
            })
        }
    }

    // ezt valósítja meg a nézet, ahol megjelenik a lista, így tud reagálni a lista eseményeire
    interface SightItemClickListener {
        fun onItemClick(sight: Sight)
        fun onItemLongClick(position: Int, view: View, sight: Sight): Boolean
    }

}