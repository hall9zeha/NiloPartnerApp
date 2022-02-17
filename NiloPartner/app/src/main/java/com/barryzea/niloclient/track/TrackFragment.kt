package com.barryzea.niloclient.track

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.barryzea.niloclient.R
import com.barryzea.niloclient.commons.Constants
import com.barryzea.niloclient.databinding.FragmentTrackBinding
import com.barryzea.niloclient.interfaces.OrderAux
import com.barryzea.niloclient.pojo.Order
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class TrackFragment:Fragment() {
    private var bind:FragmentTrackBinding?=null
    private var order: Order?=null
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        bind= FragmentTrackBinding.inflate(inflater, container,false)
        bind?.let{
           return  it.root
        }
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getOrder()

    }

    private fun setupActionBar() {
        (activity as? AppCompatActivity)?.let{
            it.supportActionBar?.setDisplayHomeAsUpEnabled(true)
            it.supportActionBar?.title=getString(R.string.track_title)
            setHasOptionsMenu(true)
        }
    }
    private fun setupAnalytics(){
        firebaseAnalytics=Firebase.analytics
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW){
            param(FirebaseAnalytics.Param.METHOD,"check_track")
        }
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId==android.R.id.home){
            activity?.onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun getOrder() {
        order=(activity as OrderAux)?.getOrderSelected()
        order?.let {
            updateUI(it)
            getOrderInRealTime(it.id)
            setupActionBar()

            setupAnalytics()
        }
    }

    private fun updateUI(order: Order) {
        bind?.let{
            it.progressBar.progress=order.status * (100/3) -15
            it.cbOrdered.isChecked=order.status>0
            it.cbPreparing.isChecked=order.status>1
            it.cbSent.isChecked=order.status>2
            it.cbDelivered.isChecked=order.status>4
        }
    }
    private fun getOrderInRealTime(orderId:String){
        val db=FirebaseFirestore.getInstance()
        val docOrderRef=db.collection(Constants.COLLECTION_REQUESTS).document(orderId)
        docOrderRef.addSnapshotListener { snapshot, error ->
            if(error!=null){
                Toast.makeText(activity, "Error al obtener esta orden", Toast.LENGTH_SHORT).show()
                return@addSnapshotListener
            }
            if(snapshot !=null && snapshot.exists()){
                val order=snapshot.toObject(Order::class.java)
                order?.let{
                    it.id=snapshot.id
                    updateUI(it)
                }
            }
        }
    }
    override fun onDestroy() {
        (activity as? AppCompatActivity)?.let{
            it.supportActionBar?.setDisplayHomeAsUpEnabled(false)
            it.supportActionBar?.title=getString(R.string.order_title)
            setHasOptionsMenu(false)
        }
        super.onDestroy()
    }
}