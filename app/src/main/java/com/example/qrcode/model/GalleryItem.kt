package com.example.qrcode.model

import android.net.Uri
import java.io.Serializable

data class GalleryItem(
    var uri: Uri? = null,
    var path: String?
) : Serializable