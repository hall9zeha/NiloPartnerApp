package com.barryzea.niloclient.profile

import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.barryzea.niloclient.R
import com.barryzea.niloclient.databinding.FragmentProfileBinding
import com.barryzea.niloclient.interfaces.MainAux
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

class ProfileFragment:Fragment() {
    private var bind:FragmentProfileBinding?=null
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
                bind?.edtUrlPhoto?.setText(user.photoUrl.toString())



                Glide.with(this)
                    .load(user.photoUrl)

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
            bind.btnUpdate.setOnClickListener {
                bind.edtUrlPhoto.clearFocus()
                bind.edtFullName.clearFocus()
                updateProfile(bind)
            }

        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId==android.R.id.home){
            activity?.onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun updateProfile(bind: FragmentProfileBinding) {
        FirebaseAuth.getInstance().currentUser?.let{user->
            val profileUpdate=UserProfileChangeRequest.Builder()
                .setDisplayName(bind.edtFullName.toString().trim())
                .setPhotoUri(Uri.parse(bind.edtUrlPhoto.toString().trim()))
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