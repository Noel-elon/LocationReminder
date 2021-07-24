package com.udacity.project4.authentication

import android.app.Activity
import android.content.Context
import android.widget.Toast

enum class AuthState {
    SUCCESS, FAIL, ERROR
}

const val AUTH_RESULT = 1

fun Activity.showToast(message : String){
    Toast.makeText(this.applicationContext, message, Toast.LENGTH_SHORT).show()
}
