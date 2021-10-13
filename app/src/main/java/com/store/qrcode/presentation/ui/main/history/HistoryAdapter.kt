package com.store.qrcode.presentation.ui.main.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.daimajia.swipe.SimpleSwipeListener
import com.daimajia.swipe.SwipeLayout
import com.daimajia.swipe.adapters.RecyclerSwipeAdapter
import com.daimajia.swipe.implments.SwipeItemRecyclerMangerImpl
import com.store.qrcode.R
import com.store.qrcode.common.UNSELECT_INDEX
import com.store.qrcode.common.utils.createShareIntent
import com.store.qrcode.databinding.ItemHistoryBinding
import com.store.qrcode.model.entity.History

class HistoryAdapter : RecyclerSwipeAdapter<HistoryAdapter.HistoryViewHolder>() {
    private val histories = ArrayList<History>()
    private var currentOpenPosition: Int = UNSELECT_INDEX
    private val itemManager = SwipeItemRecyclerMangerImpl(this)
    var itemEvent: ItemEvent? = null
    var randomQRList = arrayOf(
        R.drawable.ic_yellow_qr,
        R.drawable.ic_purple_qr,
        R.drawable.ic_pink_qr,
        R.drawable.ic_blue_qr,
        R.drawable.ic_green_qr
    )

    inner class HistoryViewHolder(private val binding: ItemHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(history: History) {
            binding.history = history
            itemManager.bindView(binding.swipeLayoutHistory, adapterPosition)
//            val randomNum = (0..4).random()
//            binding.ivHistory.setImageDrawable(
//                ContextCompat.getDrawable(
//                    binding.root.context,
//                    R.drawable.ic_qr_code
//                )
//            )
        }

        init {
            binding.btnDelete.setOnClickListener { view ->
                itemEvent?.onDelete(adapterPosition, histories[adapterPosition])
                closeItemAt(adapterPosition)
            }

            binding.btnShare.setOnClickListener { view ->
                val qrCode = histories[adapterPosition].qrCode
                closeItemAt(adapterPosition)
                view.context.createShareIntent(qrCode)
            }

            binding.swipeLayoutHistory.addSwipeListener(object : SimpleSwipeListener() {
                override fun onStartOpen(layout: SwipeLayout) {
                    itemManager.closeAllExcept(layout)
                    currentOpenPosition = adapterPosition
                }

                override fun onClose(layout: SwipeLayout) {
                    super.onClose(layout)
                    if (currentOpenPosition == adapterPosition) {
                        currentOpenPosition = UNSELECT_INDEX
                    }
                }
            })
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        return HistoryViewHolder(
            ItemHistoryBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(histories[position])
    }

    override fun getItemCount() = histories.size
    override fun getItemViewType(position: Int) = position

    override fun getItemId(position: Int) = position.toLong()

    override fun getSwipeLayoutResourceId(position: Int): Int {
        return R.id.swipeLayoutHistory
    }

    /**
     * Close all swipe item
     * すべてのスワイプアイテムを閉じる
     */
    private fun closeAllItem() {
        itemManager.closeAllItems()
        currentOpenPosition = UNSELECT_INDEX
    }

    /**
     * Close swipe item at position
     * すべてのスワイプアイテムを閉じる
     */
    private fun closeItemAt(position: Int) {
        itemManager.closeItem(position)
        currentOpenPosition = UNSELECT_INDEX
    }


    fun submitList(histories: List<History>) {
        this.histories.clear()
        this.histories.addAll(histories)
        notifyDataSetChanged()
    }

    interface ItemEvent {
        fun onDelete(position: Int, history: History?)
    }

}