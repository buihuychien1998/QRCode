package com.store.qrcode.presentation.ui.main.qrcode

import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.store.qrcode.databinding.ItemQrCodeBinding
import com.store.qrcode.model.entity.Barcode

class QRCodeAdapter : RecyclerView.Adapter<QRCodeAdapter.QRViewHolder>() {
    private val qrList = ArrayList<Barcode>()
    var itemClickListener: ItemClickListener? = null

    inner class QRViewHolder(private val binding: ItemQrCodeBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(barcode: Barcode) {
            binding.item = barcode
            if (barcode.image?.size == null) {
                binding.ivQRCode.setImageDrawable(ColorDrawable(Color.GRAY))
                return
            }
            val bitmap = BitmapFactory.decodeByteArray(barcode.image, 0, barcode.image.size)
            binding.ivQRCode.setImageBitmap(bitmap)
        }

        init {
            binding.root.setOnClickListener { view ->
                val barcode = qrList[adapterPosition]
                itemClickListener?.onItemClickListener(barcode)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QRViewHolder {
        return QRViewHolder(
            ItemQrCodeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: QRViewHolder, position: Int) {
        holder.bind(qrList[position])
    }

    override fun getItemCount() = qrList.size

    fun submitList(barList: List<Barcode>) {
        this.qrList.clear()
        this.qrList.addAll(barList)
        notifyDataSetChanged()
    }

    fun getQRCode(position: Int) = qrList[position]

    interface ItemClickListener {
        fun onItemClickListener(barcode: Barcode?)
    }
}