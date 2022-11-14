package com.mparticle.example.higgsshopsampleapp.utils

import android.content.Context
import java.io.IOException

fun Context.getJsonDataFromAsset(fileName: String): String? {
    val jsonString: String
    try {
        jsonString = this.assets.open(fileName).bufferedReader().use { it.readText() }
    } catch (ioException: IOException) {
        "File Parsing Error".log( "Could not find local $fileName")
        ioException.printStackTrace()
        return null
    }
    return jsonString
}