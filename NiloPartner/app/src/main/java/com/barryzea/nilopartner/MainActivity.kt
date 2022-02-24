package com.barryzea.nilopartner

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.GridLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.barryzea.nilopartner.adapters.ProductAdapter
import com.barryzea.nilopartner.commons.Constants
import com.barryzea.nilopartner.commons.Constants.COLLECTION_PRODUCT
import com.barryzea.nilopartner.databinding.ActivityMainBinding
import com.barryzea.nilopartner.dialogs.AddDialogFragment
import com.barryzea.nilopartner.interfaces.MainAux
import com.barryzea.nilopartner.interfaces.OnProductListener
import com.barryzea.nilopartner.order.OrderActivity
import com.barryzea.nilopartner.pojo.Product
import com.barryzea.nilopartner.promo.PromoFragment
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.FirebaseException
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.io.IOException

class MainActivity : AppCompatActivity(), OnProductListener, MainAux {
    private lateinit var bind:ActivityMainBinding
    private lateinit var firebaseAuth:FirebaseAuth
    private lateinit var authStateListener:FirebaseAuth.AuthStateListener
    private lateinit var adapter:ProductAdapter
    private lateinit var listenerFirestore:ListenerRegistration
    private lateinit var querySnapshot: EventListener<QuerySnapshot>
    private var productSelected:Product?=null
    private lateinit var firebaseAnalytics:FirebaseAnalytics

    private val resultLauncher=registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ it ->
        var response=IdpResponse.fromResultIntent(it.data)
        if(it.resultCode== RESULT_OK){
            val user=FirebaseAuth.getInstance().currentUser
            if(user !=null){
                bind.nsvProducts.visibility= View.VISIBLE
                bind.lnLoading.visibility=View.GONE
                bind.extFabCreate.show()
                Toast.makeText(this, "Bienvenido", Toast.LENGTH_SHORT).show()

                firebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN){
                    param(FirebaseAnalytics.Param.SUCCESS, 100)//Donde 100 representa un suceso exitosos, puede ser cualquier número o cadena
                    param(FirebaseAnalytics.Param.METHOD, "login")
                }
            }
        }
        else{
            if(response==null){
                Toast.makeText(this, "Hasta pronto", Toast.LENGTH_SHORT).show()
                firebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN){
                    param(FirebaseAnalytics.Param.SUCCESS,200)//cancelado el inicio de sesión o cerrado
                    param(FirebaseAnalytics.Param.METHOD,"login")
                }
                finish()
            }
            else{
                response.error?.let{
                    if(it.errorCode==ErrorCodes.NO_NETWORK){
                        Toast.makeText(this, "No hay red", Toast.LENGTH_SHORT).show()
                    }
                    else{
                        Toast.makeText(this, "Código de error: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
                    firebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN){
                        param(FirebaseAnalytics.Param.SUCCESS,it.errorCode.toLong())
                        param(FirebaseAnalytics.Param.METHOD,"login")
                    }
                }
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind= ActivityMainBinding.inflate(layoutInflater)
        setContentView(bind.root)
        configAuth()
        configRecyclerView()
        //configFirestore()
        //configFirestoreRealTime()
        configButtons()
        configAnalytics()

    }
    private fun configRecyclerView(){
        adapter = ProductAdapter(mutableListOf(), this)
        bind.rvProducts.apply {
            layoutManager = GridLayoutManager(
                this@MainActivity, 3, GridLayoutManager.HORIZONTAL, false
            )
            adapter = this@MainActivity.adapter
        }
        //No poner setHasFixed in true cuando haces llamadas a datos cambiantes desde la nube o cualquier otros
        //dato dinámico
        /*
        * Solamente hardcodeo de código para probar el funcionamiento
        * */
        /*  (1..20).forEach {
              val product=Product(it.toString(),"Producto $it", "Este producto es el $it",
                  "", it, it * 1.1)
              adapter.add(product)
          }*/
    }
    private fun configAuth() {
        //Implementamos autenticacion con FirebaseUi la api que recomienda firebase contiene todos los
        //proveedores en una sola api, ya la habiamos tomado antes pero ahora es oficial

        firebaseAuth= FirebaseAuth.getInstance()
        authStateListener=FirebaseAuth.AuthStateListener {auth->
            auth.currentUser?.let{
                supportActionBar?.title= auth.currentUser?.displayName
                bind.nsvProducts.visibility= View.VISIBLE
                bind.lnLoading.visibility=View.GONE
                bind.extFabCreate.show()
            }?:run{
                val providers= arrayListOf(
                    AuthUI.IdpConfig.EmailBuilder().build(),
                AuthUI.IdpConfig.GoogleBuilder().build())

                resultLauncher.launch(AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setAvailableProviders(providers)
                    .setIsSmartLockEnabled(false)
                    .build())
            }

        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_logout, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.itemLogOut->{
                AuthUI.getInstance().signOut(this)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Sesión Cerrada", Toast.LENGTH_SHORT).show()
                        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN){
                            param(FirebaseAnalytics.Param.SUCCESS, 100)//sign_out successfully
                            param(FirebaseAnalytics.Param.METHOD, "sign_out")
                        }
                    }
                    .addOnCompleteListener {
                        if(it.isSuccessful){
                            bind.nsvProducts.visibility=View.GONE
                            bind.extFabCreate.hide()
                            bind.lnLoading.visibility=View.VISIBLE
                        }
                        else{
                            Toast.makeText(this, "No se pudo cerrar la sesión", Toast.LENGTH_SHORT).show()
                            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN){
                                param(FirebaseAnalytics.Param.SUCCESS, 201)//sign_out error
                                param(FirebaseAnalytics.Param.METHOD,"sign_out")

                            }
                        }
                    }
            }
            R.id.itemOrder->{
                startActivity(Intent(this,OrderActivity::class.java))
            }
            R.id.itemPromo->{
                PromoFragment().show(supportFragmentManager, PromoFragment::class.java.simpleName)
            }
        }
        return super.onOptionsItemSelected(item)

    }
    override fun onResume() {
        super.onResume()
        firebaseAuth.addAuthStateListener(authStateListener)
        configFirestoreRealTime()

    }

    override fun onPause() {
        super.onPause()
        firebaseAuth.removeAuthStateListener(authStateListener)
        listenerFirestore.remove()
    }
    private fun configFirestore(){
        val db=FirebaseFirestore.getInstance()


            db.collection(COLLECTION_PRODUCT)
                .get()
                .addOnSuccessListener { snapshots ->
                    for (document in snapshots) {
                        val product = document.toObject(Product::class.java)

                        adapter.add(product)
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al consultar los datos", Toast.LENGTH_SHORT).show()
                }

    }
    private fun configFirestoreRealTime(){
        val db=FirebaseFirestore.getInstance()
        val dbRef=db.collection(COLLECTION_PRODUCT)

        querySnapshot= EventListener<QuerySnapshot> { snapshots, error ->
            for(snapshot in snapshots!!.documentChanges)
            {

                var product:Product=snapshot.document.toObject(Product::class.java)!!
                product.id=snapshot.document.id
                when(snapshot.type){
                    DocumentChange.Type.ADDED->{adapter.add(product)}
                    DocumentChange.Type.MODIFIED->{adapter.update(product)}
                    DocumentChange.Type.REMOVED->{adapter.delete(product)}
                }

            }
        }
        listenerFirestore=dbRef.addSnapshotListener(querySnapshot)



    }
    private fun configButtons(){
        bind.extFabCreate.setOnClickListener {
        productSelected=null
            AddDialogFragment().show(supportFragmentManager, AddDialogFragment::class.java.simpleName)
        }
    }
    private fun configAnalytics(){
        firebaseAnalytics= Firebase.analytics
    }
    override fun onClick(product: Product) {
        productSelected=product
        AddDialogFragment().show(supportFragmentManager, AddDialogFragment::class.java.simpleName)
    }

    override fun onLongClick(product: Product) {

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.delete_dialog_title)
            .setMessage(R.string.delete_dialog_msg)
            .setPositiveButton(R.string.delete_dialog_confirm){ _,_->
                val db=FirebaseFirestore.getInstance()

                val dbRef=db.collection(COLLECTION_PRODUCT)
                product.id?.let{id->
                    product.imgUrl?.let{url->
                        val photoRef=FirebaseStorage.getInstance().getReferenceFromUrl(url)
                            photoRef
                            //FirebaseStorage.getInstance().reference.child(Constants.PRODUCT_IMAGE).child(id)
                            .delete()
                            .addOnSuccessListener {
                                dbRef.document(id)
                                    .delete()
                                    .addOnFailureListener {
                                        Toast.makeText(this, "Error al eliminar registro", Toast.LENGTH_SHORT).show()
                                    }
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Error al eliminar imagen", Toast.LENGTH_SHORT).show()
                            }


                    }


                }
            }
            .setNegativeButton(R.string.delete_dialog_cancel,null)
            .show()

       
    }

    override fun getProductSelected(): Product? =productSelected
}