package com.barryzea.nilopartner.fcm

import android.util.Log
import com.android.volley.AuthFailureError
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.barryzea.nilopartner.NiloPartnerApplication
import com.barryzea.nilopartner.commons.Constants
import org.json.JSONException
import org.json.JSONObject
import kotlin.jvm.Throws

class NotificationRS {

    fun sendNotification(title:String, message:String, tokens:String){
        val params=JSONObject()
        params.put(Constants.PARAM_METHOD, Constants.SEND_NOTIFICATION)
        params.put(Constants.PARAM_TITLE, title)
        params.put(Constants.PARAM_MESSAGE, message)
        params.put(Constants.PARAM_TOKENS, tokens)

        val jsonObjectRequest:JsonObjectRequest=object:JsonObjectRequest(Method.POST, Constants.NILO_RS, params,
            Response.Listener{response ->
                try{
                    val success=response.getInt(Constants.PARAM_SUCCESS)
                }
                catch(e:JSONException){
                    e.printStackTrace()
                    Log.e("Volley exception", e.localizedMessage )
                }
        }, Response.ErrorListener {error ->
                if(error.localizedMessage!=null){
                    Log.e("Voley error", error.localizedMessage )
                }
        }){
            @Throws(AuthFailureError::class)
            override fun getHeaders(): MutableMap<String, String> {
                val paramsHeader=HashMap<String, String>()
                paramsHeader["Content-Type"]="application/json; charset=utf-8"
                return super.getHeaders()
            }
        }
        NiloPartnerApplication.volleyHelper.addToRequestQueue(jsonObjectRequest)
    }
}