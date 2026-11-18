
package com.example.dnd_quedamos.core.network

import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respondError
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.Serializable

internal object FakeUsuarioServer {

    private val administradores = setOf(1)

    fun esAdmin(usuarioAutenticadoId: Int?): Boolean =
        usuarioAutenticadoId != null &&
                usuarioAutenticadoId in administradores

    suspend fun manejar(
        scope: MockRequestHandleScope,
        request: HttpRequestData,
        usuarioAutenticadoId: Int?,
    ): HttpResponseData? {

        val ruta = request.url.encodedPath
            .removePrefix("$API/")
            .trimEnd('/')

        if (ruta != "usuarios/me/permisos") return null

        return with(scope) {
            if (request.method != HttpMethod.Get)
                return@with respondError(HttpStatusCode.MethodNotAllowed)

            if (usuarioAutenticadoId == null)
                return@with respondError(HttpStatusCode.Unauthorized)

            respondJson(
                AppJson.encodeToString(
                    PermisosUsuarioDto(
                        esAdmin(usuarioAutenticadoId)
                    )
                )
            )
        }
    }
}

@Serializable
internal data class PermisosUsuarioDto(
    val esAdmin: Boolean
)
