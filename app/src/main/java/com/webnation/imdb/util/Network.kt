package com.webnation.imdb.util

import android.content.Context
import android.net.ConnectivityManager

object Network {

    fun isNetworkAvailble(context: Context?): Boolean {

        val conManager = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = conManager.activeNetworkInfo

        return activeNetworkInfo != null

    }
}