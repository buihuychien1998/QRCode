package com.example.qrcode.common.widget

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import androidx.recyclerview.widget.RecyclerView

class SpacesItemDecoration(private val spaceBottom: Int, spaceColumn: Int) : ItemDecoration() {
    private val spaceColumn: Int = spaceColumn / 2
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        outRect.bottom = spaceBottom
        if (parent.getChildLayoutPosition(view) % 2 == 1) {
            outRect.left = spaceColumn
        } else {
            outRect.right = spaceColumn
        }
    }

}