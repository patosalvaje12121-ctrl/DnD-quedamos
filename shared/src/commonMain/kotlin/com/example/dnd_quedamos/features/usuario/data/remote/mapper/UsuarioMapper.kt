
package com.example.dnd_quedamos.features.usuario.data.remote.mapper

import com.example.dnd_quedamos.features.usuario.data.remote.dto.UsuarioDto
import com.example.dnd_quedamos.features.usuario.domain.model.DataUsuario

fun UsuarioDto.toDomain(): DataUsuario {
    return DataUsuario(
        id = id,
        name = nombre,
        imageID = imagenId,
        descBio = biografia,
        assistedEvents = eventosAsistidos
    )
}

fun DataUsuario.toDto(): UsuarioDto {
    return UsuarioDto(
        id = id,
        nombre = name,
        imagenId = imageID,
        biografia = descBio,
        eventosAsistidos = assistedEvents
    )
}

fun List<UsuarioDto>.toDomain(): List<DataUsuario> {
    return map { it.toDomain() }
}
