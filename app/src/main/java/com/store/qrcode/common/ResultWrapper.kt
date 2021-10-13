package com.store.qrcode.common

data class ResultWrapper<out T>(val status: Status, val data: T?, val message: String?) {
    companion object {
        fun <T> success(data: T): ResultWrapper<T> =
            ResultWrapper(status = Status.SUCCESS, data = data, message = null)

        fun <T> error(data: T?, message: String?): ResultWrapper<T> =
            ResultWrapper(status = Status.ERROR, data = data, message = message)

        fun <T> error(message: String?): ResultWrapper<T> =
            ResultWrapper(status = Status.ERROR, data = null, message = message)

        fun <T> loading(data: T?): ResultWrapper<T> =
            ResultWrapper(status = Status.LOADING, data = data, message = null)

        fun <T> loading(): ResultWrapper<T> =
            ResultWrapper(status = Status.LOADING, data = null, message = null)
    }
}