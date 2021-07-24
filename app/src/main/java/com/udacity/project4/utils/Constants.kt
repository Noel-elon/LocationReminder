package com.udacity.project4.utils

import android.content.Context
import android.widget.Toast
import com.udacity.project4.BuildConfig

const val ACTION_GEOFENCE_EVENT =
    "com.udacity.project4.locationreminders.geofence.ACTION_GEOFENCE_EVENT"
const val LOCATION_PERMISSION_CODE = 3
const val FOREGROUND_AND_BACKGROUND_PERMISSION_CODE = 25
const val FOREGROUND_PERMISSION_CODE = 22
const val LOCATION_INDEX = 27
const val BACKGROUND_LOCATION_INDEX = 1
const val TURN_DEVICE_LOCATION_ON_CODE = 70
 const val CHANNEL_ID = "Channel_Id"
 const val NOTIFICATION_ID = 37

fun Context.showToast(message : String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}