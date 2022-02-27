package com.barryzea.nilopartner

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.GridLayoutManager
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
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException

class MainActivity : AppCompatActivity(), OnProductListener, MainAux {
    private lateinit var bind:ActivityMainBinding
    private lateinit var firebaseAuth:FirebaseAuth
    private lateinit var authStateListener:FirebaseAuth.AuthStateListener
    private lateinit var adapter:ProductAdapter
    private lateinit var listenerFirestore:ListenerRegistration
    private lateinit var querySnapshot: EventListener<QuerySnapshot>
    private var productSelected:Product?=null
    private lateinit var firebaseAnalytics:FirebaseAnalytics
    private val snackbar:Snackbar by lazy {
        Snackbar.make(bind.root , "", Snackbar.LENGTH_INDEFINITE)
    }

    private val authLauncher=registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ it ->
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
    //variables para subir mas de una imagen
    private var count=0
    private var uriList= mutableListOf<Uri>()

    private var galleryResult=registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if(it.resultCode== RESULT_OK){
            if(it.data?.clipData!=null){
                count=it.data!!.clipData!!.itemCount
                for(i in 0..count -1){
                    uriList.add(it.data!!.clipData!!.getItemAt(i).uri)
                }
                if(count >0){
                    uploadImage(0)
                }
            }
        }
    }

    private fun uploadImage(position: Int) {

       FirebaseAuth.getInstance().currentUser?.let{user->
                snackbar.apply {
                    setText("Subiendo imagen ${position + 1} de $count")
                    show()
                }
                val productRef=FirebaseStorage.getInstance().reference
                    .child(user.uid)
                    .child(Constants.PRODUCT_IMAGE)
                    .child(productSelected!!.id!!)
                    .child("image${position+1}")

               productRef.putFile(uriList[position])
                   .addOnSuccessListener {
                        if(position < count -1){
                            uploadImage(position +1)

                        }
                       else{
                           snackbar.apply {
                               setText("Imágenes subidas correctamente")
                               duration = Snackbar.LENGTH_SHORT
                               show()
                           }
                       }
                    }
                    .addOnFailureListener{
                        snackbar.apply {
                            setText("Error al subir imagen ${position +1}")
                            duration = Snackbar.LENGTH_LONG
                            show()
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

                authLauncher.launch(AuthUI.getInstance()
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
        val adapter=ArrayAdapter<String>(this, android.R.layout.select_dialog_singlechoice)
        adapter.add("Eliminar")
        adapter.add("Añadir fotos")

        MaterialAlertDialogBuilder(this)
            .setAdapter(adapter){dialogInterface:DialogInterface, position:Int->
                when(position){
                    0-> confirmDelete(product)
                    1->{
                        productSelected=product
                        //En un dispositivo huawei(mate 10p lite) no funciona la badera ACTION_PICK con la combinación de EXTRA_ALLOW_MULTIPLE
                        //así que lo cambiamos por ACTION_GET_CONTENT, no es muy amigable pero funciona
                        val intent=Intent(Intent.ACTION_GET_CONTENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                        intent.type = "image/*"
                        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                        galleryResult.launch(intent)
                    }
                }
            }
            .show()

       
    }
    private fun confirmDelete(product:Product){
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.delete_dialog_title)
            .setMessage(R.string.delete_dialog_msg)
            .setPositiveButton(R.string.delete_dialog_confirm){ _,_->

                product.id?.let{id->
                    product.imgUrl?.let{url->
                        //usamos el try catch en el caso de que la url de la imagen esté vacía
                        //ya que estamos eliminando por referencia de url
                        //si salta la excepción pasamos a eliminar el registro del producto directamente en el catch
                        try {
                            val photoRef=FirebaseStorage.getInstance().getReferenceFromUrl(url)
                            photoRef
                                //FirebaseStorage.getInstance().reference.child(Constants.PRODUCT_IMAGE).child(id)
                                .delete()
                                .addOnSuccessListener {
                                    deleteProductFromFirestore(id)
                                }
                                .addOnFailureListener {
                                    //Si la imágen ya no está en storage no nos permitirá eliminar el registro del producto
                                    //entonces tomamos la excepción desde este listener e intentamos borrar el registro
                                    //ya que la imagen no estará disponible
                                    if((it as StorageException).errorCode== StorageException.ERROR_OBJECT_NOT_FOUND)
                                    {
                                        deleteProductFromFirestore(id)
                                    }else {
                                        Toast.makeText(
                                            this,
                                            "Error al eliminar imagen",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            deleteProductFromFirestore(id)
                        }


                    }


                }
            }
            .setNegativeButton(R.string.delete_dialog_cancel,null)
            .show()

    }
    private fun deleteProductFromFirestore(productId:String){
        val db=FirebaseFirestore.getInstance()
        val dbRef=db.collection(COLLECTION_PRODUCT)
        dbRef.document(productId)
            .delete()
            .addOnFailureListener {
                Toast.makeText(this, "Error al eliminar registro", Toast.LENGTH_SHORT).show()
            }
    }

    override fun getProductSelected(): Product? =productSelected
}