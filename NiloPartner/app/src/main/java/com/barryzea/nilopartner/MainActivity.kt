package com.barryzea.nilopartner

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import com.barryzea.nilopartner.databinding.ActivityMainBinding
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    private lateinit var bind:ActivityMainBinding
    private lateinit var firebaseAuth:FirebaseAuth
    private lateinit var authStateListener:FirebaseAuth.AuthStateListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind= ActivityMainBinding.inflate(layoutInflater)
        setContentView(bind.root)
        configAuth()


    }

    private fun configAuth() {
        //Implementamos autenticacion con FirebaseUi la api que recomienda firebase contiene todos los
        //proveedores en una sola api, ya la habiamos tomado antes pero ahora es oficial
        firebaseAuth= FirebaseAuth.getInstance()
        authStateListener=FirebaseAuth.AuthStateListener {auth->
            auth.currentUser?.let{
                supportActionBar?.title= auth.currentUser?.displayName
            }?:run{
                val providers= arrayListOf(AuthUI.IdpConfig.EmailBuilder().build())

                registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
                    var response=IdpResponse.fromResultIntent(it.data)
                    if(it.resultCode== RESULT_OK){
                        val user=FirebaseAuth.getInstance().currentUser
                        if(user !=null){
                            Toast.makeText(this, "Bienvenido", Toast.LENGTH_SHORT).show()
                        }
                    }
                }.launch(AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setAvailableProviders(providers)
                    .build())
            }

        }

    }

    override fun onResume() {
        super.onResume()
        firebaseAuth.addAuthStateListener(authStateListener)
    }

    override fun onPause() {
        super.onPause()
        firebaseAuth.removeAuthStateListener(authStateListener)
    }
}