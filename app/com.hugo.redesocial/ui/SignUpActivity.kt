package com.luiz.nativ.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.luiz.nativ.auth.UserAuth
import com.luiz.nativ.databinding.ActivitySignUpBinding

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding
    private val userAuth = UserAuth()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnCriarConta.setOnClickListener {
            val email = binding.edtEmailSignUp.text.toString()
            val pass = binding.edtPasswordSignUp.text.toString()
            val confirm = binding.edtConfirmPassword.text.toString()

            if (pass == confirm && email.isNotEmpty()) {
                userAuth.cadastro(email, pass) { sucesso, erro ->
                    if (sucesso) {
                        startActivity(Intent(this, ProfileActivity::class.java))
                        finish()
                    } else Toast.makeText(this, erro, Toast.LENGTH_LONG).show()
                }
            } else Toast.makeText(this, "Senhas não conferem", Toast.LENGTH_SHORT).show()
        }
    }
}