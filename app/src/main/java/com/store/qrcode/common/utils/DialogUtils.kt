package com.store.qrcode.common.utils

import android.app.AlertDialog
import android.content.Context
import android.widget.TextView
import androidx.annotation.LayoutRes
import com.store.qrcode.R

object DialogUtils {
    private var progressDialog: AlertDialog? = null
    private fun buildProgressDialog(
        context: Context?,
        @LayoutRes layout: Int,
        setCancellationOnTouchOutside: Boolean
    ) {
        val builder = AlertDialog.Builder(context)
        //        View customLayout =
//            getLayoutInflater().inflate(layout, null);
        builder.setView(layout)
        progressDialog = builder.create()
        progressDialog?.setCanceledOnTouchOutside(setCancellationOnTouchOutside)
    }

    fun showProgressDialog(
        context: Context?,
        message: String?
    ) {
        dismissProgressDialog()
        buildProgressDialog(context, R.layout.dialog_progress, false)
        progressDialog?.show()
        val tvMessage = progressDialog?.findViewById<TextView>(R.id.tv_message)
        tvMessage?.text = message
    }

    fun dismissProgressDialog() {
        if (progressDialog != null && progressDialog!!.isShowing) {
            progressDialog?.dismiss()
        }
    }
}