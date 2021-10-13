package com.store.qrcode.common.utils

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 */
@Singleton
class PermissionHelper @Inject constructor(@ApplicationContext private var context: Context?) {
    /** Determines if the context calling has the required permission
     * @param permissions - The permissions to check
     * @return true if the IPC has the granted permission
     */
    fun hasPermission(permission: String): Boolean {
        return context?.let {
            ContextCompat.checkSelfPermission(
                it,
                permission
            )
        } == PackageManager.PERMISSION_GRANTED
    }

    /** Determines if the context calling has the required permissions
     * @param permissions - The permissions to check
     * @return true if the IPC has the granted permission
     */
    fun hasPermissions(vararg permissions: String): Boolean {
        var hasAllPermissions = true
        for (permission in permissions) {
            //you can return false instead of assigning, but by assigning you can log all permission values
            if (!hasPermission(permission)) {
                hasAllPermissions = false
            }
        }
        return hasAllPermissions
    }
}