package com.barryzea.niloclient.cart

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.barryzea.niloclient.R
import com.barryzea.niloclient.adapters.ProductCartAdapter
import com.barryzea.niloclient.databinding.FragmentCartBinding
import com.barryzea.niloclient.interfaces.MainAux
import com.barryzea.niloclient.interfaces.OnCartListener
import com.barryzea.niloclient.pojo.Product
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class CartFragment : BottomSheetDialogFragment(),  OnCartListener {
    private var bind: FragmentCartBinding?=null
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<*>
    private lateinit var adapter:ProductCartAdapter
    private var totalPrice=0.0

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        bind= FragmentCartBinding.inflate(LayoutInflater.from(activity))
        bind?.let{
            val bottomSheetDialog=super.onCreateDialog(savedInstanceState) as BottomSheetDialog
            bottomSheetDialog.setContentView(it.root)

            bottomSheetBehavior= BottomSheetBehavior.from(it.root.parent as View)
            bottomSheetBehavior.state=BottomSheetBehavior.STATE_EXPANDED
            setupRecyclerView()
            setupButtons()
            getProducts()

            return bottomSheetDialog
        }
        return super.onCreateDialog(savedInstanceState)
    }
    private fun setupRecyclerView(){
        bind?.let{
            adapter= ProductCartAdapter(arrayListOf(), this)
            it.recyclerView.apply{
                layoutManager=LinearLayoutManager(context)
                adapter=this@CartFragment.adapter
            }
        }
    }
    private fun setupButtons(){
        bind?.let{
            it.ibCancel.setOnClickListener {
                bottomSheetBehavior.state=BottomSheetBehavior.STATE_HIDDEN
            }
            it.efab.setOnClickListener {
                requestOrder()
            }
        }
    }

    private fun requestOrder() {
        dismiss()
        (activity as MainAux)?.clearCart()
    }

    private fun getProducts(){
        (activity as MainAux)?.getProductCart()?.forEach {
            adapter.add(it)
        }
    }
    override fun onDestroyView() {
        (activity as MainAux)?.updateTotal()
        super.onDestroyView()
        bind=null
    }

    override fun setQuantity(product: Product) {
        adapter.update(product)
    }

    override fun showTotal(total: Double) {
       totalPrice=total
        bind?.let{
            it.tvTotal.text=getString(R.string.car_full, total)
        }
    }

}