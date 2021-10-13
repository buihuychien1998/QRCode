package com.store.qrcode.presentation.ui.gallery

import android.content.ContentResolver
import android.content.ContentUris
import android.provider.MediaStore.Images
import android.text.TextUtils
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.store.qrcode.common.ResultWrapper
import com.store.qrcode.common.utils.FileManager
import com.store.qrcode.model.GalleryItem
import com.store.qrcode.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class GalleryViewModel @Inject constructor(var fileManager: FileManager): BaseViewModel() {

    private val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        imagesLiveData.postValue(ResultWrapper.error(exception.localizedMessage))
        Log.d("TAG", ": $exception")
    }
    var imagesLiveData = MutableLiveData<ResultWrapper<List<GalleryItem>>>()

    private fun queryFromGallery(contentResolver: ContentResolver?): List<GalleryItem> {
        val listOfAllImages: MutableList<GalleryItem> = ArrayList<GalleryItem>()
        val columns = arrayOf(
            Images.Media._ID,
            Images.Media.BUCKET_DISPLAY_NAME,
            Images.Media.DATA,
            Images.Media.DATE_TAKEN
        )
//        val whereArgs = arrayOf("image/jpeg", "image/jpg", "image/png")
//        val orderBy = Images.Media.DATE_TAKEN + " DESC"
//        val mimiType = (Images.Media.MIME_TYPE + "=? or "
//                + Images.Media.MIME_TYPE + "=? or "
//                + Images.Media.MIME_TYPE + "=?")
//        val where =
//            Images.Media.BUCKET_DISPLAY_NAME + "='" + bucketName + "' and (" + mimiType + ")"
//        val cursor = contentResolver?.query(
//            Images.Media.EXTERNAL_CONTENT_URI,
//            columns,
//            where,
//            whereArgs,
//            orderBy
//        )
        val cursor = contentResolver?.query(
            Images.Media.EXTERNAL_CONTENT_URI,
            columns,
            null,
            null,
            null
        )
        cursor?.let {
            val idColumn = cursor.getColumnIndexOrThrow(Images.Media._ID)
            while (cursor.moveToNext()) {
                // Here we'll use the column indexs that we found above.
                val id = cursor.getLong(idColumn)
                val imageUriIndex = ContentUris.withAppendedId(
                    Images.Media.EXTERNAL_CONTENT_URI,
                    id
                )
//                val dataColumnIndex = cursor.getColumnIndex(Images.Media.DATA)
                val pathOfImage = fileManager.getRealPath(imageUriIndex)
                //            String pathOfImage = cursor.getString(dataColumnIndex);
                val bucketNameIndex = cursor.getColumnIndexOrThrow(Images.Media.BUCKET_DISPLAY_NAME)
//                val name = cursor.getString(bucketNameIndex)
                if (!TextUtils.isEmpty(pathOfImage) && fileManager.isExistsFile(pathOfImage)
//                    && name == bucketName
                ) {
                    listOfAllImages.add(GalleryItem(imageUriIndex, pathOfImage))
                }
            }
            cursor.close()
        }

        return listOfAllImages
    }

    fun getImages(contentResolver: ContentResolver?) {
        viewModelScope.launch(exceptionHandler + Dispatchers.IO) {
            imagesLiveData.postValue(ResultWrapper.loading())
            val data = queryFromGallery(contentResolver)
            imagesLiveData.postValue(ResultWrapper.success(data))
        }
    }
}