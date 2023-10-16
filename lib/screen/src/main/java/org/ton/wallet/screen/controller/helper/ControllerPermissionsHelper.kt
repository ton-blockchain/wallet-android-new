package org.ton.wallet.screen.controller.helper

import org.ton.wallet.screen.controller.PermissionsCallback
import pub.devrel.easypermissions.EasyPermissions

internal class ControllerPermissionsHelper : EasyPermissions.PermissionCallbacks {

    private val callbacks = mutableListOf<PermissionsCallback>()

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) = Unit

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        callbacks.forEach { it.onPermissionsGranted(requestCode, perms) }
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        callbacks.forEach { it.onPermissionsDenied(requestCode, perms) }
    }

    fun addCallback(callback: PermissionsCallback) {
        callbacks.add(callback)
    }

    fun removeCallback(callback: PermissionsCallback) {
        callbacks.remove(callback)
    }
}