package com.webnation.imdb.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.webnation.imdb.singleton.Constants
import com.webnation.imdb.util.Network

/**
 * class that receives network changes.  Not placed in Manifest because after API 26, they do not receive
 * intents for android.net.conn.CONNECTIVITY_CHANGE
 */
class NetworkAvailableReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val i = Intent("com.webnation.imdb.MainActivity")
        val extras = Bundle()
        if (context != null && Network.isNetworkAvailble(context)) {
            extras.putBoolean(Constants.KEY_CONNECTED,true)
        } else {
            extras.putBoolean(Constants.KEY_CONNECTED, false)
        }
        i.putExtras(extras)
        context?.sendBroadcast(i)


    }
}