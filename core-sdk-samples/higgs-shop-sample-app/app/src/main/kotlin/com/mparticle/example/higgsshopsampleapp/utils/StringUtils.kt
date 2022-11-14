package com.mparticle.example.higgsshopsampleapp.utils

import android.util.Log

fun String.logSuccess(log: String = "", logLevel: Int = Log.DEBUG) {
    this.log("Publish Success $log", logLevel)
}

fun String.logFailure(log: String = "", logLevel: Int = Log.DEBUG) {
    this.log("Publish Fail $log", logLevel)
}

fun String.log(log: String, logLevel: Int = Log.DEBUG) {
    when (logLevel) {
        Log.VERBOSE -> Log.v(this, log)
        Log.INFO -> Log.i(this, log)
        Log.WARN -> Log.w(this, log)
        Log.DEBUG -> Log.d(this, log)
        else -> Log.d(this, log)
    }
}