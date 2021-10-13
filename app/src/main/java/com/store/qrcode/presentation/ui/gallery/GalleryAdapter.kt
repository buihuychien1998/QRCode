package com.store.qrcode.presentation.ui.gallery

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.NonNull
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.store.qrcode.R
import com.store.qrcode.common.DEFAULT_INDEX
import com.store.qrcode.databinding.ItemGalleryBinding
import com.store.qrcode.databinding.ItemPhotoNumberBinding
import com.store.qrcode.model.GalleryItem
import java.util.*

class GalleryAdapter(var onItemClick: (galleryItem: GalleryItem) -> Unit) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val galleries: ArrayList<GalleryItem> = ArrayList<GalleryItem>()
    var selectedIndex = DEFAULT_INDEX

    @NonNull
    override fun onCreateViewHolder(
        @NonNull parent: ViewGroup,
        viewType: Int
    ) = if (viewType == ITEM_TYPE_COUNT) PhotoNumberVH(
        DataBindingUtil.inflate(
            LayoutInflater.from(parent.context), R.layout.item_photo_number, parent, false
        )
    ) else GalleryVH(
        DataBindingUtil.inflate(
            LayoutInflater.from(parent.context), R.layout.item_gallery, parent, false
        )
    )


    override fun onBindViewHolder(@NonNull holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is GalleryVH) {
            holder.bind(galleries[position])
        } else if (holder is PhotoNumberVH) {
            holder.binding.total = galleries.size
        }
    }

    override fun getItemCount() = if (galleries.size > 0) galleries.size + 1 else 0

    override fun getItemViewType(position: Int) =
        if (isLastItem(position)) ITEM_TYPE_COUNT else ITEM_TYPE_PHOTO


    fun setData(galleryPaths: List<GalleryItem>?) {
        galleryPaths?.let {
            galleries.clear()
            galleries.addAll(it)
            notifyDataSetChanged()
        }
    }

    inner class GalleryVH(private var binding: ItemGalleryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(galleryItem: GalleryItem) {
            binding.executePendingBindings()
            //Loading image from path into image view
            Glide.with(binding.root.context)
                .load(galleryItem.uri)
                .placeholder(ColorDrawable(Color.LTGRAY))
                .into(binding.imgView)
            binding.layoutBg.visibility =
                if (selectedIndex == adapterPosition) View.VISIBLE else View.GONE

            //Handle when image item is clicked
            binding.imgView.setOnClickListener {
                val previousIndex = selectedIndex
                selectedIndex = adapterPosition
                if (previousIndex == selectedIndex) {
                    selectedIndex = DEFAULT_INDEX
                }
                onItemClick(galleries[adapterPosition])
                notifyItemChanged(previousIndex)
                notifyItemChanged(selectedIndex)
            }
        }

    }

    class PhotoNumberVH(var binding: ItemPhotoNumberBinding) :
        RecyclerView.ViewHolder(binding.root)

    fun isLastItem(position: Int): Boolean {
        return position >= galleries.size
    }

    fun getSelectedItem() = if (selectedIndex == DEFAULT_INDEX) null else galleries[selectedIndex]

    companion object {
        private const val ITEM_TYPE_PHOTO = 0
        private const val ITEM_TYPE_COUNT = 1
    }

}