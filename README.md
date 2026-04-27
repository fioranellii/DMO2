# 📱 RedeSocial

Aplicativo Android de rede social desenvolvido em Kotlin, com suporte a posts com imagens, comentários e localização por cidade.

---
[![Assistir demo](https://img.youtube.com/vi/u8oJtFLwYJs/0.jpg)](https://www.youtube.com/shorts/u8oJtFLwYJs)

## 📋 Funcionalidades

- **Cadastro e login** de usuários com autenticação Firebase
- **Feed de posts** com paginação (scroll infinito)
- **Criação de posts** com imagem e descrição
- **Localização** — exibe a cidade do autor no post
- **Comentários** em posts
- **Perfil de usuário** com foto, nome e username
- **Logout**

---

## 🛠️ Tecnologias utilizadas

| Tecnologia | Uso |
|---|---|
| Kotlin | Linguagem principal |
| Android SDK | Plataforma mobile |
| Firebase Authentication | Login e cadastro |
| Firebase Firestore | Banco de dados em nuvem |
| Firebase Storage | Armazenamento de imagens |
| FusedLocationProvider | Obtenção de localização GPS |
| Geocoder (Android) | Conversão de coordenadas em cidade |
| ViewBinding | Acesso às views |
| RecyclerView | Listagem de posts |

---

## 🚀 Como rodar o projeto

### Pré-requisitos

- Android Studio (Hedgehog ou superior)
- JDK 11+
- Conta no [Firebase Console](https://console.firebase.google.com/)

### Passo a passo

1. Clone o repositório:
   ```bash
   git clone https://github.com/fioranellii/redesocial.git
   ```

2. Abra o projeto no **Android Studio**

3. Configure o Firebase:
   - Crie um projeto no Firebase Console
   - Ative **Authentication** (e-mail/senha), **Firestore** e **Storage**
   - Baixe o arquivo `google-services.json` e coloque em `app/`

4. Sincronize as dependências com o Gradle

5. Execute o app em um emulador ou dispositivo físico

---

## 🔑 Permissões necessárias

```xml
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.INTERNET" />
```

---

## 📁 Estrutura do projeto

```
app/src/main/java/com/hugo/redesocial/
├── adapter/        # Adapters do RecyclerView
├── auth/           # Autenticação Firebase
├── dao/            # Acesso ao Firestore
├── model/          # Modelos de dados (Post, User...)
├── ui/             # Activities (Home, Profile, Login...)
└── utils/          # Utilitários (Base64, Localização...)
```

---

## 👨‍💻 Autor

Desenvolvido por **Hugo**
