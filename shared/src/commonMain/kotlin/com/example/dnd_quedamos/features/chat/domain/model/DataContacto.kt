package com.example.dnd_quedamos.features.chat.domain.model

/**
 * Usuario con el que se puede empezar un chat.
 *
 * Es propio del chat (y no DataUsuario) a propósito: en la rama `user` DataUsuario
 * tendrá campos obligatorios (email...) que el chat no necesita, y así no chocamos al mezclar.
 */
data class DataContacto(
    val id: Int,
    val nombre: String,
)
