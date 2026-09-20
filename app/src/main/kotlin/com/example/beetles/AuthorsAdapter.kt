package com.example.beetles

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView

data class Author(val name: String, val photoResId: Int)

class AuthorsAdapter(context: Context, private val authors: List<Author>) : ArrayAdapter<Author>(context, 0, authors) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_author, parent, false)
        val author = getItem(position)

        val ivPhoto = view.findViewById<ImageView>(R.id.ivAuthorPhoto)
        val tvName = view.findViewById<TextView>(R.id.tvAuthorName)

        ivPhoto.setImageResource(author?.photoResId ?: R.drawable.ic_launcher_foreground)
        tvName.text = author?.name

        return view
    }
}