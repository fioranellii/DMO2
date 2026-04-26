package com.luiz.nativ.dao

import com.google.firebase.firestore.FirebaseFirestore
import com.luiz.nativ.model.User

class UserDAO {
    private val db = FirebaseFirestore.getInstance()
    private val collection = "usuarios"

    fun salvarPerfil(user: User, callback: (Boolean) -> Unit) {
        db.collection(collection).document(user.email)
            .set(user)
            .addOnSuccessListener { callback(true) }
            .addOnFailureListener { callback(false) }
    }

    fun buscarPerfil(email: String, callback: (User?) -> Unit) {
        db.collection(collection).document(email).get()
            .addOnSuccessListener { document ->
                val user = document.toObject(User::class.java)
                callback(user)
            }
            .addOnFailureListener { callback(null) }
    }
}