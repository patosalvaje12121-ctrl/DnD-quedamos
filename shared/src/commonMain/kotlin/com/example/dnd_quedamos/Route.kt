package com.example.dnd_quedamos

import kotlinx.serialization.Serializable

@Serializable
sealed interface Route {

    // data object  -> Ventana estática (
    // data class  -> Ventana que cambia en funcion de variables ( puestas en la creacion de ventana )

    @Serializable
    data object Home : Route;

    @Serializable
    data class InspectEvent(val eventID: Int) : Route

    @Serializable
    data class UserProfile(val userID: Int) : Route;

    @Serializable
    data object MainMap : Route;

    @Serializable
    data object CreateEvent : Route;

    @Serializable
    data object Chats : Route;

    @Serializable
    data class ChatWith(val userID: Int) : Route

    // Abre un chat ya existente por su id (desde la lista de chats)
    @Serializable
    data class Chat(val chatId: Int) : Route

    // Abre (o crea) el chat de grupo de un evento
    @Serializable
    data class ChatEvento(val eventoId: Int) : Route

    // Elegir un usuario para empezar un chat directo
    @Serializable
    data object NuevoChat : Route

}