package com.udacity.project4.authentication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.map

class AuthViewModel : ViewModel() {


    val authState = FirebaseUserLiveData().map { user ->
        if (user != null) {
            AuthState.SUCCESS
        } else {
            AuthState.FAIL
        }
    }
}