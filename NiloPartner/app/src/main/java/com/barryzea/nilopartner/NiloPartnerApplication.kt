package com.barryzea.nilopartner

import android.app.Application
import com.barryzea.nilopartner.fcm.VolleyHelper

class NiloPartnerApplication:Application() {

    companion object{
        lateinit var volleyHelper:VolleyHelper
    }

    override fun onCreate() {
        super.onCreate()
        volleyHelper=VolleyHelper.getInstance(this)
    }
}