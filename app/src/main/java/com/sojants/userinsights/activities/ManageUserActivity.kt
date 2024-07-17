package com.sojants.userinsights.activities

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.provider.MediaStore
import android.view.MenuItem
import android.widget.RadioButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.snackbar.Snackbar
import com.sojants.userinsights.R
import com.sojants.userinsights.database.SqliteDatabase
import com.sojants.userinsights.databinding.ActivityManageUserBinding
import com.sojants.userinsights.models.User
import java.io.ByteArrayOutputStream
import java.io.IOException

class ManageUserActivity : AppCompatActivity() {

    private lateinit var binding: ActivityManageUserBinding
    private lateinit var database: SqliteDatabase
    private var user: User? = null
    private val PICK_IMAGE = 1
    private lateinit var toolbar: Toolbar

    private val MAX_FILE_SIZE_BYTES = 100 * 1024 // 100KB in bytes
    private var fileSizeAlertDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityManageUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.setStatusBarColor(ContextCompat.getColor(this,R.color.darkblue));

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = ""
        }

        database = SqliteDatabase(this)

        user = intent.getSerializableExtra("user") as? User


        user?.let {
            binding.nameInput.setText(it.name)
            binding.emailInput.setText(it.email)
            when (it.gender) {
                "male" -> binding.radioMale.isChecked = true
                "female" -> binding.radioFemale.isChecked = true
                "other" -> binding.radioOther.isChecked = true
            }
            binding.ageInput.setText(it.age.toString())
            it.photo?.let { photo ->
                val bitmap = BitmapFactory.decodeByteArray(photo, 0, photo.size)
                binding.photoView.setImageBitmap(bitmap)
            }
        }


        binding.saveButton.setOnClickListener {
            saveUser()
        }

        binding.photoView.setOnClickListener {
            pickPhoto()
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun saveUser() {
        val name = binding.nameInput.text.toString().trim()
        val email = binding.emailInput.text.toString().trim()
        val gender = findViewById<RadioButton>(binding.genderRadioGroup.checkedRadioButtonId)?.text.toString()
        val age = binding.ageInput.text.toString().trim().toIntOrNull()
        val photo = (binding.photoView.drawable as? BitmapDrawable)?.bitmap?.let { bitmap ->
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.toByteArray()
        }

        if (!isValidName(name)) {
            showSnackbar("Please enter a valid name")
            return
        }

        if (!isValidEmail(email)) {
            showSnackbar("Please enter a valid email")
            return
        }

        if (gender == "null") {
            showSnackbar("Please select a gender")
            return
        }

        if (age == null || !isValidAge(age)) {
            showSnackbar("Please enter a valid age (0-120)")
            return
        }

        if (user == null) {
            val newUser = User(0, name, email, gender, age, photo)
            database.addUser(newUser)
        } else {
            user!!.apply {
                this.name = name
                this.email = email
                this.gender = gender
                this.age = age
                this.photo = photo
            }
            database.updateUser(user!!)
        }

       finish()
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    private fun isValidName(name: String): Boolean {
        return name.length >= 2
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isValidAge(age: Int): Boolean {
        return age in 0..120
    }



//    private fun pickPhoto() {
//        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
//        startActivityForResult(intent, PICK_IMAGE)
//    }
//
//    @Deprecated("This method has been deprecated in favor of using the Activity Result API\n      which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n      contracts for common intents available in\n      {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n      testing, and allow receiving results in separate, testable classes independent from your\n      activity. Use\n      {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)}\n      with the appropriate {@link ActivityResultContract} and handling the result in the\n      {@link ActivityResultCallback#onActivityResult(Object) callback}.")
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {
//            val selectedImage = data?.data
//            selectedImage?.let {
//                val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, it)
//                binding.photoView.setImageBitmap(bitmap)
//            }
//        }
//    }

    private fun pickPhoto() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE)
    }

    @Deprecated("This method has been deprecated in favor of using the Activity Result API\n      which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n      contracts for common intents available in\n      {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n      testing, and allow receiving results in separate, testable classes independent from your\n      activity. Use\n      {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)}\n      with the appropriate {@link ActivityResultContract} and handling the result in the\n      {@link ActivityResultCallback#onActivityResult(Object) callback}.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            val selectedImage = data?.data
            selectedImage?.let {
                try {
                    val inputStream = contentResolver.openInputStream(it)
                    val fileSize = inputStream?.available() ?: 0
                    if (fileSize > MAX_FILE_SIZE_BYTES) {
                        showFileSizeExceedDialog()
                        return
                    }
                    val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, it)
                    binding.photoView.setImageBitmap(bitmap)

                    inputStream?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun showFileSizeExceedDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("File Size Too Large")
            .setMessage("Selected file size exceeds the limit of 100KB. Please choose a smaller file.")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }

        fileSizeAlertDialog = builder.create()
        fileSizeAlertDialog?.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        fileSizeAlertDialog?.dismiss()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
