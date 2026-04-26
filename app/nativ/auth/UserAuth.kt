package com.luiz.nativ.auth

import com.google.firebase.auth.FirebaseAuth

class UserAuth {
    private val auth = FirebaseAuth.getInstance()

    fun login(email: String, pass: String, callback: (Boolean) -> Unit) {
        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task -> callback(task.isSuccessful) }
    }

    fun cadastro(email: String, pass: String, callback: (Boolean, String?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                callback(task.isSuccessful, task.exception?.message)
            }
    }

    fun atualizarSenha(novaSenha: String, callback: (Boolean, String?) -> Unit) {
        auth.currentUser?.updatePassword(novaSenha)
            ?.addOnCompleteListener { task ->
                callback(task.isSuccessful, task.exception?.message)
            } ?: callback(false, "Usuário não autenticado")
    }

    fun getEmailUsuarioLogado(): String? = auth.currentUser?.email

    fun logout() = auth.signOut()
}