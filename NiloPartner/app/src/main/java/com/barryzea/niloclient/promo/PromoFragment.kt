package com.barryzea.niloclient.promo

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.barryzea.niloclient.R
import com.barryzea.niloclient.databinding.FragmentPromoBinding
import com.barryzea.niloclient.interfaces.MainAux
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.badge.BadgeUtils
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import kotlin.math.roundToInt

/****
 * Project Nilo Client
 * Created by Barry Zea H. on 28/02/2022.
 * Copyright (c)  All rights reserved.
 ***/

class PromoFragment: Fragment() {
    private var bind:FragmentPromoBinding?=null
    private var title:String=""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        bind= FragmentPromoBinding.inflate(inflater, container, false)
        bind?.let{
            return it.root
        }

        return super.onCreateView(inflater, container, savedInstanceState)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configToolbar()
        configRemoteConfig()
    }
    private fun configToolbar(){
        (activity as? MainAux)?.showButton(false)
        (activity as? AppCompatActivity)?.apply {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            title=supportActionBar?.title
            supportActionBar?.title=getString(R.string.promo_fragment_title)
            setHasOptionsMenu(true)

        }
    }
    @SuppressLint("UnsafeOptInUsageError")
    private fun configRemoteConfig() {
        val remoteConfig= Firebase.remoteConfig
        remoteConfig.setDefaultsAsync(R.xml.remote_config_default)
        //solicitar y extraer datos de remoteconfig
        remoteConfig.fetchAndActivate()

            //si no hay ningún error se extraerán los datos ya sea desde el servidor o localmente
            .addOnCompleteListener {
                if(it.isSuccessful){

                    val percentage=remoteConfig.getDouble("percentage")
                    val photoUrl=remoteConfig.getString("photoUrl")
                    val message=remoteConfig.getString("message")
                   bind?.let{
                       it.tvMessage.text=message
                       it.tvPercentage.text=String.format("%s0%%", (percentage).roundToInt().toString())

                       Glide.with(this)
                           .load(photoUrl)
                           .diskCacheStrategy(DiskCacheStrategy.NONE)
                           .placeholder(R.drawable.ic_access_time)
                           .error(R.drawable.ic_offer)
                           .centerCrop()
                           .into(it.imgPromo)
                   }

                }
            }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.clear()
        super.onPrepareOptionsMenu(menu)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == android.R.id.home){
            activity?.onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }
    override fun onDestroyView() {
        super.onDestroyView()
        (activity as? MainAux)?.showButton(true)
        (activity as? AppCompatActivity)?.apply {
            supportActionBar?.setDisplayHomeAsUpEnabled(false)
            supportActionBar?.title=title
            setHasOptionsMenu(false)
            }
        bind=null
    }
}