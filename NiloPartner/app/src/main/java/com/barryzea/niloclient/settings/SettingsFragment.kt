package com.barryzea.niloclient.settings

import android.os.Bundle
import android.widget.Toast
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.barryzea.niloclient.R
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
        val switchPref=findPreference<SwitchPreferenceCompat>(getString(R.string.prefs_offers_key))
        switchPref?.setOnPreferenceChangeListener { preference, newValue ->
            (newValue as? Boolean )?.let{isChecked->
                val topic =getString(R.string.topic_offers)
                if(isChecked){
                    //nos suscribimos al topico
                    Firebase.messaging.subscribeToTopic(topic)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Activado", Toast.LENGTH_SHORT).show()
                        }
                }
                else{
                    //nos desuscribimos
                    Firebase.messaging.unsubscribeFromTopic(topic)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Desactivado", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            true
        }
    }
}