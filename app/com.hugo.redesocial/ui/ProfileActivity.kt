package com.luiz.nativ.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.luiz.nativ.auth.UserAuth
import com.luiz.nativ.dao.UserDAO
import com.luiz.nativ.model.User
import com.luiz.nativ.utils.Base64Converter
import com.luiz.nativ.databinding.ActivityProfileBinding

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    private val userAuth = UserAuth()
    private val userDAO = UserDAO()

    private val galeria = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) binding.profilePicture.setImageURI(uri)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val email = userAuth.getEmailUsuarioLogado() ?: ""
        userDAO.buscarPerfil(email) { user ->
            if (user != null) {
                runOnUiThread {
                    binding.edtUsername.setText(user.username)
                    binding.edtFullName.setText(user.nomeCompleto)
                    if (user.fotoPerfil.isNotEmpty()) {
                        runCatching {
                            binding.profilePicture.setImageBitmap(Base64Converter.stringToBitmap(user.fotoPerfil))
                        }
                    }
                }
            }
        }

        binding.btnAlterarFoto.setOnClickListener {
            galeria.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.btnSalvarPerfil.setOnClickListener {
            val novaSenha = binding.edtNovaSenha.text.toString().trim()

            if (novaSenha.isNotEmpty()) {
                if (novaSenha.length < 6) {
                    Toast.makeText(this, "A senha deve ter pelo menos 6 caracteres", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                userAuth.atualizarSenha(novaSenha) { sucesso, erro ->
                    if (!sucesso) {
                        Toast.makeText(this, "Erro ao atualizar senha: $erro", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            val fotoString = runCatching {
                Base64Converter.drawableToString(binding.profilePicture.drawable)
            }.getOrDefault("")

            val user = User(
                email = email,
                username = binding.edtUsername.text.toString().trim(),
                nomeCompleto = binding.edtFullName.text.toString().trim(),
                fotoPerfil = fotoString
            )

            userDAO.salvarPerfil(user) { sucesso ->
                if (sucesso) {
                    Toast.makeText(this, "Perfil atualizado!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Erro ao salvar perfil", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}