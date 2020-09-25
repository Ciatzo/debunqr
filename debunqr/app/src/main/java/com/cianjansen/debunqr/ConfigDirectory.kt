package com.cianjansen.debunqr

import android.content.Context

class ConfigDirectory{

    companion object{
        /**
         * Return the proper location for saving apiContext config file
         * @param context The application context as passed from the activity
         * @return The config file location
         */
        fun getDirectory(context : Context) : String{
            return context.getExternalFilesDir("conf").toString() + "bunq.conf"
        }
    }

}