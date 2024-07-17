package com.sojants.userinsights.adapters

import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView
import com.sojants.userinsights.MainActivity
import com.sojants.userinsights.R
import com.sojants.userinsights.models.User
import java.util.*

class UserAdapter(private val userList: ArrayList<User>, private val context: Context) :
    RecyclerView.Adapter<UserAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val userPhoto: ShapeableImageView = view.findViewById(R.id.userPhoto)
        val userName: TextView = view.findViewById(R.id.userName)
        val userEmail: TextView = view.findViewById(R.id.userEmail)
        val editButton: MaterialButton = view.findViewById(R.id.editButton)
        val deleteButton: MaterialButton = view.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = userList[position]
        holder.userName.text = user.name
        holder.userEmail.text = user.email

        user.photo?.let {
            val bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
            holder.userPhoto.setImageBitmap(bitmap)
        }

        holder.editButton.setOnClickListener {
            if (context is MainActivity) {
                context.editUser(user)
            }
        }

        holder.deleteButton.setOnClickListener {
            if (context is MainActivity) {
                context.deleteUser(user)
            }
        }
        holder.userPhoto.setOnClickListener {
            if (context is MainActivity) {
                context.updateUserPhoto(user.id)
            }
        }
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    fun updateUsers(updatedList: ArrayList<User>) {
        userList.clear()
        userList.addAll(updatedList)
        notifyDataSetChanged()
    }

}
