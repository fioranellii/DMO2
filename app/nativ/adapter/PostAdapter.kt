package com.luiz.nativ.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.luiz.nativ.R
import com.luiz.nativ.model.Post

class PostAdapter(private var posts: MutableList<Post> = mutableListOf()) :
    RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    class PostViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgAutorPerfil: ImageView = view.findViewById(R.id.imgAutorPerfil)
        val txtAutorUsername: TextView = view.findViewById(R.id.txtAutorUsername)
        val txtCidade: TextView = view.findViewById(R.id.txtCidade)
        val imgPost: ImageView = view.findViewById(R.id.imgPost)
        val txtDescricao: TextView = view.findViewById(R.id.txtDescricao)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.post_item, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]

        holder.txtAutorUsername.text = post.autorUsername.ifEmpty { post.autorEmail }
        if (post.autorFoto != null) {
            holder.imgAutorPerfil.setImageBitmap(post.autorFoto)
        } else {
            holder.imgAutorPerfil.setImageResource(R.drawable.empty_profile)
        }

        if (post.cidade.isNotEmpty()) {
            holder.txtCidade.text = "📍 ${post.cidade}"
            holder.txtCidade.visibility = View.VISIBLE
        } else {
            holder.txtCidade.visibility = View.GONE
        }

        if (post.imagem != null) {
            holder.imgPost.setImageBitmap(post.imagem)
            holder.imgPost.visibility = View.VISIBLE
        } else {
            holder.imgPost.visibility = View.GONE
        }

        holder.txtDescricao.text = post.descricao
    }

    override fun getItemCount(): Int = posts.size

    fun setPosts(newPosts: List<Post>) {
        posts = newPosts.toMutableList()
        notifyDataSetChanged()
    }

    fun addPosts(newPosts: List<Post>) {
        val start = posts.size
        posts.addAll(newPosts)
        notifyItemRangeInserted(start, newPosts.size)
    }
}