package com.example.dnd_quedamos.features.evento.domain.repository

import com.example.dnd_quedamos.features.evento.domain.model.DataEvento

interface IRepositoryEvento {
    suspend fun bbddEvento(): List<DataEvento>
}