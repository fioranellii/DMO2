package com.hugo.redesocial.utils

import android.Manifest
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import androidx.annotation.RequiresPermission
import com.google.android.gms.location.*
import java.io.IOException
import java.util.Locale

class LocalizacaoHelper(
    private val context: Context,
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)
) {

    interface Callback {
        fun onLocalizacaoRecebida(endereco: Address, latitude: Double, longitude: Double)
        fun onErro(mensagem: String)
    }

    @RequiresPermission(anyOf = [
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ])
    fun obterLocalizacaoAtual(callback: Callback) {

        val request = CurrentLocationRequest.Builder()
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .build()

        fusedLocationClient.getCurrentLocation(request, null)
            .addOnSuccessListener { location ->
                if (location != null) {
                    obterEndereco(location.latitude, location.longitude, callback)
                } else {
                    callback.onErro("Localização indisponível")
                }
            }
            .addOnFailureListener {
                callback.onErro("Erro: ${it.message}")
            }
    }

    private fun obterEndereco(
        latitude: Double,
        longitude: Double,
        callback: Callback
    ) {
        val geocoder = Geocoder(context, Locale.getDefault())

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            geocoder.getFromLocation(
                latitude,
                longitude,
                1,
                object : Geocoder.GeocodeListener {
                    override fun onGeocode(addresses: MutableList<Address>) {
                        if (addresses.isNotEmpty()) {
                            callback.onLocalizacaoRecebida(addresses[0], latitude, longitude)
                        } else {
                            callback.onErro("Endereço não encontrado")
                        }
                    }

                    override fun onError(errorMessage: String?) {
                        callback.onErro(errorMessage ?: "Erro no Geocoder")
                    }
                }
            )
        } else {
            try {
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)

                if (!addresses.isNullOrEmpty()) {
                    callback.onLocalizacaoRecebida(addresses[0], latitude, longitude)
                } else {
                    callback.onErro("Endereço não encontrado")
                }
            } catch (e: IOException) {
                callback.onErro("Erro: ${e.message}")
            }
        }
    }
}