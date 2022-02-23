package com.barryzea.niloclient.settings

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import com.barryzea.niloclient.R

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        supportActionBar?.let{
            it.setDisplayHomeAsUpEnabled(true)
            it.title=getString(R.string.settings)
        }
        val fragment=SettingsFragment()

        supportFragmentManager.beginTransaction()
            .add(R.id.containerMainSettings, fragment)
            .commit()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId==android.R.id.home){
            onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }
}