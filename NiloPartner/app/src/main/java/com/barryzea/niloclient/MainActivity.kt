package com.barryzea.niloclient

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.barryzea.niloclient.adapters.ProductAdapter
import com.barryzea.niloclient.cart.CartFragment
import com.barryzea.niloclient.commons.Constants.COLLECTION_PRODUCT
import com.barryzea.niloclient.databinding.ActivityMainBinding
import com.barryzea.niloclient.detail.DetailFragment

import com.barryzea.niloclient.interfaces.MainAux
import com.barryzea.niloclient.interfaces.OnProductListener
import com.barryzea.niloclient.pojo.Product


import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*

class MainActivity : AppCompatActivity(), OnProductListener, MainAux {
    private lateinit var bind: ActivityMainBinding
    private lateinit var firebaseAuth:FirebaseAuth
    private lateinit var authStateListener:FirebaseAuth.AuthStateListener
    private lateinit var adapter:ProductAdapter
    private lateinit var listenerFirestore:ListenerRegistration
    private lateinit var querySnapshot: EventListener<QuerySnapshot>
    private var productSelected:Product?=null
    private var productCartList:MutableList<Product> = mutableListOf()

    private val resultLauncher=registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ it ->
        var response=IdpResponse.fromResultIntent(it.data)
        if(it.resultCode== RESULT_OK){
            val user=FirebaseAuth.getInstance().currentUser
            if(user !=null){
                bind.nsvProducts.visibility= View.VISIBLE
                bind.lnLoading.visibility=View.GONE

                Toast.makeText(this, "Bienvenido", Toast.LENGTH_SHORT).show()
            }
        }
        else{
            if(response==null){
                Toast.makeText(this, "Hasta pronto", Toast.LENGTH_SHORT).show()
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
                    }
                    .addOnCompleteListener {
                        if(it.isSuccessful){
                            bind.nsvProducts.visibility=View.GONE

                            bind.lnLoading.visibility=View.VISIBLE
                        }
                    }
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
        bind.btnViewCar.setOnClickListener {
            val fragment=CartFragment()
            fragment.show(supportFragmentManager.beginTransaction(), CartFragment::class.java.simpleName)
        }
    }
    override fun onClick(product: Product) {
        var index=productCartList.indexOf(product)
        if(index != -1)
        {
            productSelected=productCartList[index]
        }
        else {
            productSelected=product
        }
        productSelected=product
        val fragment=DetailFragment()
        supportFragmentManager.beginTransaction()
            .add(R.id.containerMain, fragment)
            .addToBackStack(null)
            .commit()
        showButton(false)
    }


    override fun getProductCart(): MutableList<Product> = productCartList

    override fun getProductSelected(): Product?= productSelected
    override fun showButton(isVisible: Boolean) {
        bind.btnViewCar.visibility=if(isVisible) View.VISIBLE else View.GONE
    }

    override fun addProductToCart(product: Product) {
        var index=productCartList.indexOf(product)
        if(index != -1)
        {
            productCartList[index] = product
        }
        else {
            productCartList.add(product)
        }
        updateTotal()
    }

    override fun updateTotal() {
        var total=0.0
        productCartList.forEach { product->
            total += product.totalPrice()
        }
        if(total==0.0){
            bind.tvTotal.text=getString(R.string.car_empty)
        }
        else{
            bind.tvTotal.text=getString(R.string.car_full,total)
        }
    }
}