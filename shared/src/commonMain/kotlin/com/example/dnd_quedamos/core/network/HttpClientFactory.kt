package com.example.dnd_quedamos.core.network

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.SIMPLE
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * URL base de la API. Debe terminar en "/" para que las rutas relativas
 * ("eventos", "usuarios/3"...) se añadan detrás en lugar de reemplazar el último segmento.
 *
 * TODO: sustituir por la URL real de la API cuando exista.
 * Mientras tanto es un marcador: el dominio ".invalid" está reservado y NUNCA existe,
 * así que cualquier petición falla con un error claro ("no se puede resolver la dirección")
 * en vez de acabar en una web real de otra persona.
 */
const val BASE_URL = "https://aqui-iria-el-link-a-la-api.invalid/api/"

/**
 * Configuración de JSON compartida por toda la app.
 * Es pública por si en algún momento hace falta (de)serializar fuera de Ktor.
 */
val AppJson: Json = Json {
    ignoreUnknownKeys = true   // si la API añade campos nuevos, no rompemos
    isLenient = true           // tolera JSON algo "sucio" (comillas, etc.)
    explicitNulls = false      // los null no se envían; los campos ausentes usan su valor por defecto
}

/**
 * Crea el único HttpClient de la app.
 *
 * @param engine motor HTTP a usar. Si es null, Ktor usa el que haya en el classpath
 *               de cada plataforma (OkHttp en Android/escritorio, fetch en web).
 *               Poder pasarle un egnine sirve principalmente para pasar un MockEngine y asi probar sin backend.
 */
fun createHttpClient(engine: HttpClientEngine? = null): HttpClient {

    val config: HttpClientConfig<*>.() -> Unit = {

        // Lanza ClientRequestException (4xx) / ServerResponseException (5xx)
        // en vez de devolver la respuesta de error como si fuera válida.
        expectSuccess = true

        // JSON <-> clases @Serializable automáticamente en .body() y setBody()
        install(ContentNegotiation) {
            json(AppJson)
        }

        // Muestra peticiones/respuestas por consola mientras desarrollamos
        install(Logging) {
            logger = Logger.SIMPLE
            level = LogLevel.INFO
            sanitizeHeader { header -> header == HttpHeaders.Authorization }
        }

        // Si el servidor no responde, fallamos en vez de esperar para siempre
        install(HttpTimeout) {
            connectTimeoutMillis = 10_000
            requestTimeoutMillis = 15_000
            socketTimeoutMillis = 15_000
        }

        // Valores por defecto de TODAS las peticiones
        defaultRequest {
            url(BASE_URL)
        }
    }

    return if (engine != null) HttpClient(engine, config) else HttpClient(config)
}
