package com.example.dnd_quedamos.features.evento.domain.model

/** Tipo de quedada. OTRO también se usa si la API manda un tipo que la app no conoce. */
enum class TipoActividad {
    ROL,
    JUEGOS_DE_MESA,
    CARTAS,
    WARGAME,
    CONVENCION,
    OTRO,
}
