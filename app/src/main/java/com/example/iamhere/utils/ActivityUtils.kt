package com.example.iamhere.utils

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat

fun Context.showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}

@RequiresApi(Build.VERSION_CODES.S)
fun Context.checkPermissions(permissions: Array<String>): Boolean {
    return permissions.all {
        ContextCompat.checkSelfPermission(
            this,
            it
        ) == PackageManager.PERMISSION_GRANTED
    }
}