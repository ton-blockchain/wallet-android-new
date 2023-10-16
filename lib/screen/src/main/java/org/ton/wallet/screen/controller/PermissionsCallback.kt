package org.ton.wallet.screen.controller

interface PermissionsCallback {

    fun onPermissionsGranted(requestCode: Int, permissions: MutableList<String>) = Unit

    fun onPermissionsDenied(requestCode: Int, permissions: MutableList<String>) = Unit
}