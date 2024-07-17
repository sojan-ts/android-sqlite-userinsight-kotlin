package com.sojants.userinsights

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.sojants.userinsights.activities.BarChartActivity
import com.sojants.userinsights.activities.ManageUserActivity
import com.sojants.userinsights.adapters.UserAdapter
import com.sojants.userinsights.database.SqliteDatabase
import com.sojants.userinsights.databinding.ActivityMainBinding
import com.sojants.userinsights.models.User
import java.io.InputStream
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var database: SqliteDatabase
    private lateinit var userAdapter: UserAdapter
    private val originalUserList: ArrayList<User> = ArrayList()
    private val PICK_IMAGE_REQUEST = 1
    private var selectedUserId: Int? = null

    private lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        onBackPressedDispatcher.addCallback(this,onBackPressedCallback)
        window.setStatusBarColor(ContextCompat.getColor(this,R.color.darkblue));

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = "Users"
        }

        database = SqliteDatabase(this)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        loadUsers()

        binding.chartBtn.setOnClickListener {
            val intent = Intent(this, BarChartActivity::class.java)
            startActivity(intent)
        }

        binding.newBtn.setOnClickListener {
            val intent = Intent(this, ManageUserActivity::class.java)
            startActivity(intent)
        }



        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }




    }


    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            onBackPressedMethod()
        }
    }

    private fun onBackPressedMethod() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Exit")
            .setMessage("Are you sure you want to exit?")
            .setCancelable(false)
            .setPositiveButton("Yes"){ _,_ ->
                moveTaskToBack(true)
                android.os.Process.killProcess(android.os.Process.myPid())
                exitProcess(1)
            }
            .setNegativeButton("No",null)
            .create()
            .show()
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.search_menu, menu)

        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView
        searchView.queryHint = "Search users..."

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                filterUsers(newText)
                return true
            }
        })

        return true
    }

    private fun loadUsers() {
        val users: ArrayList<User> = database.listUser()
        originalUserList.clear()
        originalUserList.addAll(users)
        userAdapter = UserAdapter(users, this)
        binding.recyclerView.adapter = userAdapter

        if (users.isEmpty()) {
            binding.noUsersTextView.visibility = View.VISIBLE
        } else {
            binding.noUsersTextView.visibility = View.GONE
        }
    }

    private fun filterUsers(query: String?) {
        if (query.isNullOrBlank()) {
            userAdapter.updateUsers(originalUserList)
            binding.noUsersTextView.visibility = View.GONE
        } else {
            val filteredList = originalUserList.filter {
                it.name.contains(query, ignoreCase = true) || it.email.contains(query, ignoreCase = true)
            }
            if (filteredList.isEmpty()) {
                binding.noUsersTextView.visibility = View.VISIBLE
            } else {
                binding.noUsersTextView.visibility = View.GONE
            }
            userAdapter.updateUsers(ArrayList(filteredList))
        }
    }




    override fun onResume() {
        super.onResume()
        loadUsers()
    }

    fun editUser(user: User) {
        val intent = Intent(this, ManageUserActivity::class.java)
        intent.putExtra("user", user)
        startActivity(intent)
    }

    fun deleteUser(user: User) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Delete User")
        builder.setMessage("Are you sure you want to delete this user?")
        builder.setPositiveButton("Yes") { _, _ ->
            database.deleteContact(user.id)
            loadUsers()
        }
        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }
//
//    fun updateUserPhoto(userId: Int) {
//        selectedUserId = userId
//        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
//        startActivityForResult(intent, PICK_IMAGE_REQUEST)
//    }
//
//    @Deprecated("This method has been deprecated in favor of using the Activity Result API\n      which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n      contracts for common intents available in\n      {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n      testing, and allow receiving results in separate, testable classes independent from your\n      activity. Use\n      {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)}\n      with the appropriate {@link ActivityResultContract} and handling the result in the\n      {@link ActivityResultCallback#onActivityResult(Object) callback}.")
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
//            val imageUri: Uri? = data?.data
//            imageUri?.let {
//                val inputStream: InputStream? = contentResolver.openInputStream(it)
//                val bitmap = BitmapFactory.decodeStream(inputStream)
//                val byteArrayOutputStream = ByteArrayOutputStream()
//                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
//                val byteArray = byteArrayOutputStream.toByteArray()
//
//                selectedUserId?.let { id ->
//                    database.updateUserPhoto(id, byteArray)
//                    loadUsers()
//                }
//            }
//        }
//    }
//

    fun updateUserPhoto(userId: Int) {
        selectedUserId = userId
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    @Deprecated("This method has been deprecated in favor of using the Activity Result API\n      which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n      contracts for common intents available in\n      {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n      testing, and allow receiving results in separate, testable classes independent from your\n      activity. Use\n      {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)}\n      with the appropriate {@link ActivityResultContract} and handling the result in the\n      {@link ActivityResultCallback#onActivityResult(Object) callback}.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            val imageUri: Uri? = data?.data
            imageUri?.let {
                val inputStream: InputStream? = contentResolver.openInputStream(it)
                val byteArray = inputStream?.use { stream ->
                    val bytes = stream.readBytes()
                    if (bytes.size > 100 * 1024) {
                        // File size exceeds 100KB, show dialog
                        showFileSizeExceedDialog()
                        return
                    }
                    bytes
                }

                byteArray?.let { bytes ->
                    selectedUserId?.let { id ->
                        database.updateUserPhoto(id, bytes)
                        loadUsers()
                    }
                }
            }
        }
    }

    private fun showFileSizeExceedDialog() {
        AlertDialog.Builder(this)
            .setTitle("File Size Exceeds Limit")
            .setMessage("Please select an image that is less than 100KB.")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }


}
