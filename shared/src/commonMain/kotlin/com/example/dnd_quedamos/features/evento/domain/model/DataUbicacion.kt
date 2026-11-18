package com.example.dnd_quedamos.features.evento.domain.model

/** Dónde es el evento: texto para mostrar + coordenadas para el mapa. */
data class DataUbicacion(
    val direccion: String,
    val latitud: Double,
    val longitud: Double,
)
