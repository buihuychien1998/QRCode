package com.example.qrcode.common.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import timber.log.Timber

abstract class SwipeSimpleCallback(
    val context: Context,
    dragDir: Int = 0,
    swipeDir: Int = ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
) :
    ItemTouchHelper.SimpleCallback(dragDir, swipeDir) {

    //configure left swipe params
    var leftBG: Int = Color.RED
    var leftLabel: String = ""
    var leftIcon: Drawable? = null

    //configure right swipe params
    var rightBG: Int = Color.BLUE
    var rightLabel: String = ""
    var rightIcon: Drawable? = null

    private lateinit var background: Drawable

    var initiated: Boolean = false

    //Setting Swipe Text
    val paint = Paint()

    private fun initSwipeView(): Unit {
        paint.color = Color.WHITE
        paint.textSize = 48f
        paint.textAlign = Paint.Align.CENTER
        background = ColorDrawable();
        initiated = true;
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }


    override fun onChildDraw(
        c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
        dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean
    ) {

        Timber.d("onChildDraw dx: $dX")

        val itemView = viewHolder.itemView
        if (!initiated) {
            initSwipeView()
        }

        if (dX != 0.0f) {

            if (dX > 0) {
                //right swipe
                val intrinsicHeight = (rightIcon?.intrinsicWidth ?: 0)
                val xMarkTop =
                    itemView.top + ((itemView.bottom - itemView.top) - intrinsicHeight) / 2
                val xMarkBottom = xMarkTop + intrinsicHeight

                colorCanvas(
                    c,
                    rightBG,
                    itemView.left + dX.toInt(),
                    itemView.top,
                    itemView.left,
                    itemView.bottom
                )
                drawTextOnCanvas(
                    c,
                    rightLabel,
                    (itemView.left + 200).toFloat(),
                    (xMarkTop + 10).toFloat()
                )
                drawIconOnCanVas(
                    c, rightIcon, itemView.left + (rightIcon?.intrinsicWidth ?: 0) + 50,
                    xMarkTop,
                    itemView.left + 2 * (rightIcon?.intrinsicWidth ?: 0) + 50,
                    xMarkBottom
                )

            } else {
                //left swipe
                val intrinsicHeight = (leftIcon?.getIntrinsicWidth() ?: 0)
                val xMarkTop =
                    itemView.top + ((itemView.bottom - itemView.top) - intrinsicHeight) / 2
                val xMarkBottom = xMarkTop + intrinsicHeight

                colorCanvas(
                    c,
                    leftBG,
                    itemView.right + dX.toInt(),
                    itemView.top,
                    itemView.right,
                    itemView.bottom
                )
                drawTextOnCanvas(
                    c,
                    leftLabel,
                    (itemView.right - 200).toFloat(),
                    (xMarkTop + 10).toFloat()
                )
                drawIconOnCanVas(
                    c, leftIcon, itemView.right - 2 * (leftIcon?.intrinsicWidth ?: 0) - 70,
                    xMarkTop,
                    itemView.right - (leftIcon?.intrinsicWidth ?: 0) - 70,
                    xMarkBottom
                )
            }
        }

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }


    private fun colorCanvas(
        canvas: Canvas,
        canvasColor: Int,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int
    ): Unit {
        (background as ColorDrawable).color = canvasColor
        background.setBounds(left, top, right, bottom)
        background.draw(canvas)
    }

    private fun drawTextOnCanvas(canvas: Canvas, label: String, x: Float, y: Float) {
        canvas.drawText(label, x, y, paint)
    }

    private fun drawIconOnCanVas(
        canvas: Canvas, icon: Drawable?, left: Int, top: Int, right: Int, bottom: Int
    ) {
        icon?.setBounds(left, top, right, bottom)
        icon?.draw(canvas)

    }
}