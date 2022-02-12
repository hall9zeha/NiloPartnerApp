package com.barryzea.niloclient.track

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.barryzea.niloclient.databinding.FragmentTrackBinding
import com.barryzea.niloclient.interfaces.OrderAux
import com.barryzea.niloclient.pojo.Order

class TrackFragment:Fragment() {
    private var bind:FragmentTrackBinding?=null
    private var order: Order?=null

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

    private fun getOrder() {
        order=(activity as OrderAux)?.getOrderSelected()
        order?.let {
          updateUI(it)
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
}