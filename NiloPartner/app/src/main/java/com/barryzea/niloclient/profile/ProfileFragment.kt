package com.barryzea.niloclient.profile

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.barryzea.niloclient.R
import com.barryzea.niloclient.commons.Constants
import com.barryzea.niloclient.databinding.FragmentProfileBinding
import com.barryzea.niloclient.interfaces.MainAux
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream

class ProfileFragment:Fragment() {
    private var bind:FragmentProfileBinding?=null
    private var photoSelectedUri:Uri?=null
    private val resultLauncher=registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if(it.resultCode== Activity.RESULT_OK){
            photoSelectedUri=it.data?.data
            //bind?.imgProductPreview?.setImageURI(photoSelectedUri)
            bind?.let{bind->
                Glide.with(this)
                    .load(photoSelectedUri)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.ic_image_search)
                    .error(R.drawable.ic_broken_image)
                    .circleCrop()
                    .centerCrop()
                    .into(bind.ibProfile)
            }
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        bind= FragmentProfileBinding.inflate(inflater,container, false)
        bind?.let{
            (activity as? MainAux)?.showButton(false)
            return it.root
        }
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getUser()
        configButtons()
        configToolbar()
    }


    private fun configToolbar() {
      (activity as? AppCompatActivity)?.apply{
          supportActionBar?.setDisplayHomeAsUpEnabled(true)
          supportActionBar?.title = getString(R.string.profile)
          setHasOptionsMenu(true)


      }
    }

    private fun getUser() {
        bind?.let {
            FirebaseAuth.getInstance().currentUser?.let { user ->
                bind?.edtFullName?.setText(user.displayName)
               // bind?.edtUrlPhoto?.setText(user.photoUrl.toString())



                Glide.with(this)
                    .load(user.photoUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.ic_access_time)
                    .error(R.drawable.ic_broken_image)
                    .circleCrop()
                    .centerCrop()
                    .into(bind!!.ibProfile)
            }
        }
    }

    private fun configButtons() {
        bind?.let{bind->
            bind.ibProfile.setOnClickListener {
                openGallery()
            }
            bind.btnUpdate.setOnClickListener {
                //bind.edtUrlPhoto.clearFocus()
                bind.edtFullName.clearFocus()
                FirebaseAuth.getInstance().currentUser?.let{user->
                    if(photoSelectedUri==null){
                        updateProfile(bind,user, Uri.parse(""))
                    }
                    else{
                        uploadReducedImage(user)
                    }
                }
            }

        }
    }
    private fun openGallery(){
        val intent= Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        resultLauncher.launch(intent)
    }
    private fun uploadReducedImage(user: FirebaseUser){


        //creamos una carpeta para cada usuario que agrege productos con su id como nombre de directorio uid/product_images/imagen

        val profileRef = FirebaseStorage.getInstance().reference.child(user.uid)
            .child(Constants.PROFILE_IMAGES).child(Constants.MY_PHOTO)
        photoSelectedUri?.let { uri ->
            bind?.let { bind ->
                getBitmapFromUri(uri)?.let { bitmap ->
                    bind.progressBar.visibility= View.VISIBLE
                    //comprimimos el tamaño de la imagen
                    val baos = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos)


                    //en lugar de putFile
                    //photoRef.putFile(uri)
                    //le pasamos en byteArray
                    profileRef.putBytes(baos.toByteArray())
                        .addOnProgressListener {
                            val progress = (100 * it.bytesTransferred / it.totalByteCount).toInt()
                            it.run {
                                bind.progressBar.progress=progress
                                 bind.tvProgress.text=String.format( "%s%%",progress)
                            }
                        }
                        .addOnSuccessListener {
                            it.storage.downloadUrl.addOnSuccessListener { downloadUrl ->

                                updateProfile(bind,user, downloadUrl)
                            }
                        }
                        .addOnCompleteListener {
                            bind.progressBar.visibility=View.INVISIBLE
                            bind.tvProgress.text = ""
                        }
                        .addOnFailureListener {

                            Toast.makeText(
                                requireActivity(),
                                "Error al subir imagen",
                                Toast.LENGTH_SHORT
                            ).show()


                        }
                }


            }
        }


    }
    //retornar el mapa de bits desde la uri
    private fun getBitmapFromUri(uri: Uri): Bitmap?{
        //ya que getBitmap está obsoleto en versiones recientes de android, usamos esta sentencia if
        activity?.let{
            val bitmap=if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.P){
                val source= ImageDecoder.createSource(it.contentResolver, uri)
                ImageDecoder.decodeBitmap(source)
            }
            else{
                MediaStore.Images.Media.getBitmap(it.contentResolver, uri)
            }
            return getResizedImage(bitmap, 320)
        }
        return null
    }
    //retornamos el bitmap redimensionado
    private fun getResizedImage(image: Bitmap, maxSize:Int): Bitmap {
        var width=image.width
        var height=image.height
        if(width<=maxSize && height<=maxSize) return image
        val bitmapRatio=width.toFloat() / height.toFloat()
        if(bitmapRatio>1){
            width=maxSize
            height=(width/bitmapRatio).toInt()
        }
        else{
            height=maxSize
            width=(height/bitmapRatio).toInt()
        }
        return Bitmap.createScaledBitmap(image, width,height,true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId==android.R.id.home){
            activity?.onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun updateProfile(bind: FragmentProfileBinding, user:FirebaseUser, uri:Uri) {

            val profileUpdate=UserProfileChangeRequest.Builder()
                .setDisplayName(bind.edtFullName.text.toString().trim())
                .setPhotoUri(uri)
                .build()
            user.updateProfile(profileUpdate)
                .addOnSuccessListener {
                    Toast.makeText(activity, "Usuario actualizado", Toast.LENGTH_SHORT).show()
                    (activity as? MainAux)?.updateTitle(user)
                    activity?.onBackPressed()
                }
                .addOnFailureListener {
                    Toast.makeText(activity, "No se pudo actualizar al usuario", Toast.LENGTH_SHORT).show()
                }


    }

    override fun onDestroy() {
        super.onDestroy()

        (activity as? MainAux)?.showButton(true)
        (activity as? AppCompatActivity)?.apply{
            supportActionBar?.setDisplayHomeAsUpEnabled(false)

            setHasOptionsMenu(false)


        }
        bind=null
    }

}