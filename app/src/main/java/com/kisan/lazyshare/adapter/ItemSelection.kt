package com.kisan.lazyshare.adapter

import android.view.MotionEvent
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.ItemKeyProvider
import androidx.recyclerview.widget.RecyclerView

class SelectionItemDetailLookup(private val recyclerView: RecyclerView) : ItemDetailsLookup<String>() {
    override fun getItemDetails(event: MotionEvent): ItemDetails<String>? {
        val view = recyclerView.findChildViewUnder(event.x, event.y)
        if (view != null) {
            return (recyclerView.getChildViewHolder(view) as ShareItemAdapter.BaseViewHolder).getItemDetails()
        }
        return null
    }
}

class SelectionKeyProvider(private val adapter: ShareItemAdapter) : ItemKeyProvider<String>(SCOPE_CACHED) {
    override fun getKey(position: Int): String {
        return adapter.currentList[position].name
    }

    override fun getPosition(key: String): Int {
        return adapter.currentList.indexOfFirst {
            it.name == key
        }
    }

}