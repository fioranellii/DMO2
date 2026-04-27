package com.hugo.redesocial.model

import android.graphics.Bitmap

data class Post(
    val id: String = "",
    val descricao: String = "",
    val imagem: Bitmap? = null,
    val autorEmail: String = "",
    val autorUsername: String = "",
    val autorFoto: Bitmap? = null,
    val cidade: String = ""
)