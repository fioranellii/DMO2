package com.luiz.nativ.model

import android.graphics.Bitmap

data class Post(
    val descricao: String = "",
    val imagem: Bitmap? = null,
    val autorEmail: String = "",
    val autorUsername: String = "",
    val autorFoto: Bitmap? = null,
    val cidade: String = ""
)