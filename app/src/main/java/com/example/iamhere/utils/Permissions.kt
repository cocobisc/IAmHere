package com.example.iamhere.utils

import android.Manifest

object Permissions {

    val bluetooth = arrayOf(
        Manifest.permission.BLUETOOTH,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.BLUETOOTH_ADMIN,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.BLUETOOTH_SCAN
    )
}