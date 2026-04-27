package com.hugo.redesocial.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Address
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.hugo.redesocial.R
import com.hugo.redesocial.adapter.PostAdapter
import com.hugo.redesocial.auth.UserAuth
import com.hugo.redesocial.dao.UserDAO
import com.hugo.redesocial.model.Post
import com.hugo.redesocial.utils.Base64Converter
import com.hugo.redesocial.utils.LocalizacaoHelper
import com.hugo.redesocial.databinding.ActivityHomeBinding


class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private val userAuth = UserAuth()
    private val userDAO = UserDAO()
    private val db = FirebaseFirestore.getInstance()
    private lateinit var adapter: PostAdapter

    private val PAGE_SIZE = 5L
    private var ultimoTimestamp: Timestamp? = null
    private var carregando = false

    private var imagemPostSelecionada: String? = null
    private var imagemNovoPostDialog: ImageView? = null
    private var cidadeNovoPost: String? = null
    private var latitudeNovoPost: Double? = null
    private var longitudeNovoPost: Double? = null

    private val LOCATION_PERMISSION_CODE = 1001
    private var callbackCidadePendente: (() -> Unit)? = null

    private val galeriaPost = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            imagemNovoPostDialog?.setImageURI(uri)
            imagemNovoPostDialog?.visibility = View.VISIBLE
            imagemNovoPostDialog?.drawable?.let {
                imagemPostSelecionada = Base64Converter.drawableToString(it)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = PostAdapter()
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        adapter.onPostClick = { post ->
            abrirComentariosDialog(post.id)
        }

        val email = userAuth.getEmailUsuarioLogado()
        if (email != null) {
            userDAO.buscarPerfil(email) { user ->
                if (user != null) {
                    runOnUiThread {
                        binding.txtUsername.text = user.username
                        binding.txtNomeCompleto.text = user.nomeCompleto
                        if (user.fotoPerfil.isNotEmpty()) {
                            runCatching {
                                binding.imgHomeProfile.setImageBitmap(
                                    Base64Converter.stringToBitmap(user.fotoPerfil)
                                )
                            }
                        }
                    }
                }
            }
        }

        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val layout = recyclerView.layoutManager as LinearLayoutManager
                val totalItems = layout.itemCount
                val lastVisible = layout.findLastVisibleItemPosition()
                if (!carregando && lastVisible >= totalItems - 2) {
                    carregarFeed(true)
                }
            }
        })

        binding.btnAdicionarPost.setOnClickListener { abrirDialogNovoPost() }

        binding.btnPerfil.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        binding.btnSair.setOnClickListener {
            userAuth.logout()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        carregarFeed(false)
    }

    private fun carregarFeed(paginar: Boolean) {
        if (carregando) return
        carregando = true

        if (!paginar) {
            ultimoTimestamp = null
            adapter.setPosts(emptyList())
        }

        var query = db.collection("posts")
            .orderBy("data", Query.Direction.DESCENDING)
            .limit(PAGE_SIZE)

        ultimoTimestamp?.let { query = query.startAfter(it) }

        query.get().addOnSuccessListener { result ->
            carregando = false

            val docs = result.documents
            if (docs.isEmpty()) return@addOnSuccessListener

            ultimoTimestamp = docs.last().getTimestamp("data")

            montarPosts(docs) { posts ->
                if (paginar) adapter.addPosts(posts)
                else adapter.setPosts(posts)
            }
        }
    }

    private fun montarPosts(
        docs: List<com.google.firebase.firestore.DocumentSnapshot>,
        onReady: (List<Post>) -> Unit
    ) {
        val total = docs.size
        val postsTemp = arrayOfNulls<Post>(total)
        var prontos = 0

        for ((index, doc) in docs.withIndex()) {

            val id = doc.id

            val imageString = doc.getString("imageString") ?: ""
            val descricao = doc.getString("descricao") ?: ""
            val autorEmail = doc.getString("autorEmail") ?: ""
            val cidade = doc.getString("cidade") ?: ""

            val bitmap = if (imageString.isNotEmpty()) {
                runCatching {
                    Base64Converter.stringToBitmap(imageString)
                }.getOrNull()
            } else null

            fun salvar(username: String, autorFoto: Bitmap?) {
                postsTemp[index] = Post(
                    id = id,
                    descricao = descricao,
                    imagem = bitmap,
                    autorEmail = autorEmail,
                    autorUsername = username,
                    autorFoto = autorFoto,
                    cidade = cidade
                )
                prontos++
                if (prontos == total) {
                    runOnUiThread {
                        onReady(postsTemp.filterNotNull())
                    }
                }
            }

            if (autorEmail.isNotEmpty()) {
                userDAO.buscarPerfil(autorEmail) { user ->
                    val foto = user?.fotoPerfil?.let {
                        runCatching {
                            Base64Converter.stringToBitmap(it)
                        }.getOrNull()
                    }
                    salvar(user?.username ?: autorEmail, foto)
                }
            } else {
                salvar("", null)
            }
        }
    }

    private fun abrirDialogNovoPost() {

        imagemPostSelecionada = null
        cidadeNovoPost = "Sem localização"

        val view = layoutInflater.inflate(R.layout.dialog_novo_post, null)

        val imagem = view.findViewById<ImageView>(R.id.imgNovoPost)
        val btnFoto = view.findViewById<Button>(R.id.btnSelecionarFotoPost)
        val edt = view.findViewById<EditText>(R.id.edtDescricaoPost)
        val btnLocalizacao = view.findViewById<Button>(R.id.btnLocalizacao) // botão no dialog
        val txtCidade = view.findViewById<TextView>(R.id.txtCidadeNovoPost) // label no dialog

        imagemNovoPostDialog = imagem

        btnFoto.setOnClickListener {
            galeriaPost.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }

        btnLocalizacao.setOnClickListener {
            // Verifica permissão
            if (ActivityCompat.checkSelfPermission(
                    this, Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_PERMISSION_CODE
                )
                return@setOnClickListener
            }

            txtCidade.text = "Obtendo localização..."

            val helper = LocalizacaoHelper(this)
            helper.obterLocalizacaoAtual(object : LocalizacaoHelper.Callback {
                override fun onLocalizacaoRecebida(
                    endereco: android.location.Address,
                    latitude: Double,
                    longitude: Double
                ) {
                    val cidade = endereco.subAdminArea
                        ?: endereco.adminArea
                        ?: "Localização obtida"
                    cidadeNovoPost = cidade
                    runOnUiThread { txtCidade.text = cidade }
                }

                override fun onErro(mensagem: String) {
                    runOnUiThread {
                        txtCidade.text = "Sem localização"
                        Toast.makeText(this@HomeActivity, mensagem, Toast.LENGTH_SHORT).show()
                    }
                }
            })
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle("Novo Post")
            .setView(view)
            .setPositiveButton("Postar", null)
            .setNegativeButton("Cancelar", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {

                val descricao = edt.text.toString()

                if (descricao.isEmpty() && imagemPostSelecionada == null) {
                    Toast.makeText(this, "Post vazio!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val post = hashMapOf(
                    "descricao" to descricao,
                    "imageString" to (imagemPostSelecionada ?: ""),
                    "autorEmail" to (userAuth.getEmailUsuarioLogado() ?: ""),
                    "cidade" to (cidadeNovoPost ?: "Sem localização"), // ✅ agora usa o valor real
                    "data" to Timestamp.now()
                )

                db.collection("posts").add(post).addOnSuccessListener {
                    Toast.makeText(this, "Post criado!", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                    carregarFeed(false)
                }
            }
        }

        dialog.show()
    }

    private fun abrirComentariosDialog(postId: String) {

        val view = layoutInflater.inflate(R.layout.dialog_comentarios, null)

        val txt = view.findViewById<TextView>(R.id.txtComentarios)
        val edt = view.findViewById<EditText>(R.id.edtComentario)
        val btn = view.findViewById<Button>(R.id.btnEnviarComentario)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Comentários")
            .setView(view)
            .create()

        fun carregar() {
            db.collection("posts")
                .document(postId)
                .collection("comentarios")
                .get()
                .addOnSuccessListener {
                    val sb = StringBuilder()
                    for (doc in it) {
                        sb.append("${doc.getString("autor")}: ${doc.getString("texto")}\n\n")
                    }
                    txt.text = sb.toString()
                }
        }

        btn.setOnClickListener {

            val texto = edt.text.toString()
            if (texto.isEmpty()) return@setOnClickListener

            val email = userAuth.getEmailUsuarioLogado()

            if (email != null) {
                userDAO.buscarPerfil(email) { user ->

                    val nome = user?.username ?: "Usuário"

                    val comentario = hashMapOf(
                        "texto" to texto,
                        "autor" to nome
                    )

                    db.collection("posts")
                        .document(postId)
                        .collection("comentarios")
                        .add(comentario)
                        .addOnSuccessListener {
                            edt.setText("")
                            carregar()
                        }
                }
            }
        }

        carregar()
        dialog.show()
    }
}