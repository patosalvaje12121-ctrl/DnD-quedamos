package com.example.dnd_quedamos.core.network

import com.example.dnd_quedamos.features.usuario.data.remote.dto.UsuarioDto
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respondError
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.client.engine.mock.toByteArray
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable

/**
 * Parte de autenticación del servidor falso (ver FakeApiEngine.kt).
 *
 * Rutas:
 *   POST /api/auth/register -> crea usuario + guarda hash de contraseña
 *   POST /api/auth/login    -> comprueba nombre + contraseña y devuelve usuario
 *
 * No gestiona sesiones: la capa LoginUserUseCase actualiza FakeCurrentUserProvider
 * cuando recibe una respuesta válida. No hay tokens en esta demo.
 *
 * SOLO PARA PRUEBAS: SHA-256 sin sal no es seguro para una API real.
 * El backend futuro debe usar un hash de contraseñas lento, con sal aleatoria.
 */
internal object FakeLoginServer {

    private val mutex = Mutex()

    /** Contraseñas de prueba para los cuatro usuarios iniciales.
     *  No se guardan en UsuarioDto ni se envían al consultar un perfil. */
    private val hashes = mutableMapOf(
        1 to Sha256Demo.hash("marco123"),
        2 to Sha256Demo.hash("ana123"),
        3 to Sha256Demo.hash("luis123"),
        4 to Sha256Demo.hash("sara123"),
    )

    /** Devuelve null si la ruta no corresponde a autenticación. */
    suspend fun manejar(scope: MockRequestHandleScope, request: HttpRequestData): HttpResponseData? {
        val ruta = request.url.encodedPath.removePrefix("$API/").trimEnd('/')
        if (ruta != "auth/register" && ruta != "auth/login") return null

        return with(scope) {
            if (request.method != HttpMethod.Post) return@with respondError(HttpStatusCode.MethodNotAllowed)

            // Parsear solo los campos de autenticación, no mezclar contraseñas con UsuarioDto.
            val cuerpo = request.body.toByteArray().decodeToString()
            when (ruta) {
                "auth/register" -> {
                    val datos = try { AppJson.decodeFromString<RegistroAuthDto>(cuerpo) }
                    catch (_: Exception) { return@with respondError(HttpStatusCode.BadRequest) }
                    if (datos.nombre.isBlank() || datos.nombre.length > 50 ||
                        datos.biografia.length > 500 || datos.password.isBlank()) {
                        return@with respondError(HttpStatusCode.BadRequest)
                    }

                    // El Mutex impide registrar dos veces el mismo hash/ID simultáneamente.
                    mutex.withLock {
                        // La comprobación del nombre único y el alta se hacen dentro
                        // del mutex de FakeUsuarioServer para evitar registros duplicados.
                        val creado = FakeUsuarioServer.crearDesdeRegistro(
                            datos.nombre.trim(), datos.biografia.trim()
                        )
                        if (creado == null) {
                            respondError(HttpStatusCode.Conflict)
                        } else {
                            hashes[creado.id] = Sha256Demo.hash(datos.password)
                            respondJson(AppJson.encodeToString(creado), HttpStatusCode.Created)
                        }
                    }
                }
                else -> {
                    val datos = try { AppJson.decodeFromString<LoginAuthDto>(cuerpo) }
                    catch (_: Exception) { return@with respondError(HttpStatusCode.BadRequest) }
                    mutex.withLock {
                        // Buscamos el perfil por nombre, ignorando mayúsculas.
                        // El ID sigue siendo la clave interna del hash: si el usuario
                        // modifica su nombre, podrá entrar con el nombre nuevo.
                        val usuario = FakeUsuarioServer.buscarDesdeAuth(datos.nombre)
                        val hashGuardado = usuario?.let { hashes[it.id] }
                        if (hashGuardado == null || hashGuardado != Sha256Demo.hash(datos.password)) {
                            respondError(HttpStatusCode.Unauthorized)
                        } else {
                            respondJson(AppJson.encodeToString(usuario))
                        }
                    }
                }
            }
        }
    }

    /** Se llama al borrar una cuenta para invalidar sus credenciales. */
    suspend fun eliminarCredenciales(id: Int) {
        mutex.withLock { hashes.remove(id) }
    }
}

@Serializable
internal data class RegistroAuthDto(val nombre: String, val biografia: String = "", val password: String)

@Serializable
internal data class LoginAuthDto(val nombre: String, val password: String)
