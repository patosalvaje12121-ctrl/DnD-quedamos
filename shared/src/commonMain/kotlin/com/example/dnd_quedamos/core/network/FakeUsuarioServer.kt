package com.example.dnd_quedamos.core.network

import com.example.dnd_quedamos.features.usuario.data.remote.dto.UsuarioDto
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.client.engine.mock.toByteArray
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable

/**
 * Usuarios de ejemplo con los que arranca el servidor falso.
 * Los ids y nombres son los mismos que usa FakeChatServer: 1 Marco, 2 Ana, 3 Luis, 4 Sara.
 *  - Usuario 1 (Marco): es el usuario de FakeCurrentUserProvider y además administrador.
 *  - Usuario 4 (Sara): sin biografía, para probar el perfil vacío.
 */
internal val usuariosFalsos: List<UsuarioDto> = listOf(
    UsuarioDto(1, "Marco", biografia = "Máster de la campaña de los jueves.", eventosAsistidos = listOf(4)),
    UsuarioDto(2, "Ana", biografia = "Me encantan los one-shots de terror.", eventosAsistidos = listOf(1)),
    UsuarioDto(3, "Luis", biografia = "Jugador de Curse of Strahd.", eventosAsistidos = listOf(4)),
    UsuarioDto(4, "Sara"),
)

/**
 * Parte de usuarios del "servidor falso" (ver FakeApiEngine.kt). Guarda los usuarios en memoria
 * mientras la app esté abierta.
 *
 * Rutas:
 *   GET    /api/usuarios/me/permisos -> permisos del usuario autenticado (401 sin sesión)
 *   GET    /api/usuarios/{id}        -> un usuario (404 si no existe)
 *   POST   /api/usuarios             -> crear / registrarse (el servidor pone el id; 400 sin nombre)
 *   PUT    /api/usuarios/{id}        -> editar (solo uno mismo: 401 sin sesión, 403 si es otro)
 *   DELETE /api/usuarios/{id}        -> borrar (solo uno mismo: 401 sin sesión, 403 si es otro)
 *
 * GET /api/usuarios (la lista) la contesta FakeChatServer, que va antes en FakeApiEngine.
 */
internal object FakeUsuarioServer {

    private val administradores = setOf(1)

    private val mutex = Mutex()
    private val usuarios = usuariosFalsos.toMutableList()
    private var nextId = (usuarios.maxOfOrNull { it.id } ?: 0) + 1

    fun esAdmin(usuarioAutenticadoId: Int?): Boolean =
        usuarioAutenticadoId != null &&
                usuarioAutenticadoId in administradores

    /** Devuelve la respuesta si la petición es de usuarios, o null para que la maneje otra parte. */
    suspend fun manejar(
        scope: MockRequestHandleScope,
        request: HttpRequestData,
        usuarioAutenticadoId: Int?,
    ): HttpResponseData? {

        val seg = request.url.encodedPath
            .removePrefix("$API/")
            .trimEnd('/')
            .split("/")

        if (seg[0] != "usuarios") return null

        val method = request.method
        val cuerpo = if (method == HttpMethod.Post || method == HttpMethod.Put) {
            request.body.toByteArray().decodeToString()
        } else ""

        return mutex.withLock {
            with(scope) {
                val id = seg.getOrNull(1)?.toIntOrNull()
                when {
                    // GET usuarios/me/permisos
                    seg == listOf("usuarios", "me", "permisos") -> when {
                        method != HttpMethod.Get -> respondError(HttpStatusCode.MethodNotAllowed)
                        usuarioAutenticadoId == null -> respondError(HttpStatusCode.Unauthorized)
                        else -> respondJson(
                            AppJson.encodeToString(PermisosUsuarioDto(esAdmin(usuarioAutenticadoId)))
                        )
                    }

                    // POST usuarios
                    method == HttpMethod.Post && seg.size == 1 -> {
                        val recibido = AppJson.decodeFromString<UsuarioDto>(cuerpo)
                        if (recibido.nombre.isBlank()) {
                            respondError(HttpStatusCode.BadRequest)
                        } else {
                            val nuevo = recibido.copy(id = nextId++)
                            usuarios += nuevo
                            respondJson(AppJson.encodeToString(nuevo), HttpStatusCode.Created)
                        }
                    }

                    // Todo lo demás es usuarios/{id} con un usuario que exista
                    seg.size != 2 || id == null -> null
                    usuarios.none { it.id == id } -> respondError(HttpStatusCode.NotFound)

                    // GET usuarios/{id}
                    method == HttpMethod.Get ->
                        respondJson(AppJson.encodeToString(usuario(id)))

                    // PUT y DELETE: solo sobre uno mismo
                    usuarioAutenticadoId == null -> respondError(HttpStatusCode.Unauthorized)
                    usuarioAutenticadoId != id -> respondError(HttpStatusCode.Forbidden)

                    // PUT usuarios/{id}
                    method == HttpMethod.Put -> {
                        val editado = AppJson.decodeFromString<UsuarioDto>(cuerpo).copy(id = id)
                        if (editado.nombre.isBlank()) {
                            respondError(HttpStatusCode.BadRequest)
                        } else {
                            usuarios[usuarios.indexOf(usuario(id))] = editado
                            respondJson(AppJson.encodeToString(editado))
                        }
                    }

                    // DELETE usuarios/{id}
                    method == HttpMethod.Delete -> {
                        usuarios.removeAll { it.id == id }
                        respond("", HttpStatusCode.NoContent)
                    }

                    else -> respondError(HttpStatusCode.MethodNotAllowed)
                }
            }
        }
    }


    /**
     * Crea un perfil desde el endpoint de registro de FakeLoginServer.
     */
    suspend fun crearDesdeRegistro(nombre: String, biografia: String): UsuarioDto = mutex.withLock {
        val nuevo = UsuarioDto(id = nextId++, nombre = nombre, biografia = biografia)
        usuarios += nuevo
        nuevo
    }

    /**
     * Busca los datos públicos del usuario para devolverlos tras validar credenciales.
     * El hash de la contraseña NUNCA se añade al UsuarioDto.
     */
    suspend fun buscarDesdeAuth(id: Int): UsuarioDto? = mutex.withLock {
        usuarios.firstOrNull { it.id == id }
    }

    // ---------- helpers (llamar siempre con el mutex cogido) ----------

    private fun usuario(id: Int): UsuarioDto = usuarios.first { it.id == id }
}

@Serializable
internal data class PermisosUsuarioDto(
    val esAdmin: Boolean
)
