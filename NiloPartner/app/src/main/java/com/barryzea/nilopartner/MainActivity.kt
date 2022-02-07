package com.barryzea.nilopartner

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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
import com.barryzea.nilopartner.databinding.ActivityMainBinding
import com.barryzea.nilopartner.interfaces.OnProductListener
import com.barryzea.nilopartner.pojo.Product
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity(), OnProductListener {
    private lateinit var bind:ActivityMainBinding
    private lateinit var firebaseAuth:FirebaseAuth
    private lateinit var authStateListener:FirebaseAuth.AuthStateListener
    private lateinit var adapter:ProductAdapter
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
                        Toast.makeText(this, "Código de error: ${it.errorCode}", Toast.LENGTH_SHORT).show()
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

    }
    private fun configRecyclerView(){
        adapter= ProductAdapter(mutableListOf(),this)
        bind.rvProducts.apply {
            layoutManager=GridLayoutManager(this@MainActivity,3
                ,GridLayoutManager.HORIZONTAL,false)
            setHasFixedSize(true)
            adapter=this@MainActivity.adapter
        }

        (1..20).forEach {
            val product=Product(it.toString(),"Producto $it", "Este producto es el $it",
                "", it, it * 1.1)
            adapter.add(product)
        }
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
    }

    override fun onPause() {
        super.onPause()
        firebaseAuth.removeAuthStateListener(authStateListener)
    }

    override fun onClick(product: Product) {
        TODO("Not yet implemented")
    }

    override fun onLongClick(product: Product) {
        TODO("Not yet implemented")
    }
}