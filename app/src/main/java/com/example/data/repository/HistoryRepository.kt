package com.example.data.repository

import com.example.data.local.HistoryDao
import com.example.data.model.CalculationHistory
import kotlinx.coroutines.flow.Flow

class HistoryRepository(private val historyDao: HistoryDao) {

    val allHistory: Flow<List<CalculationHistory>> = historyDao.getAllHistory()
    val favoriteHistory: Flow<List<CalculationHistory>> = historyDao.getFavoriteHistory()

    suspend fun addHistory(expression: String, result: String, angleMode: String): Long {
        val entry = CalculationHistory(
            expression = expression,
            result = result,
            angleMode = angleMode
        )
        return historyDao.insertHistory(entry)
    }

    suspend fun deleteHistory(id: Long) {
        historyDao.deleteHistoryById(id)
    }

    suspend fun toggleFavorite(id: Long, currentFavorite: Boolean) {
        historyDao.setFavorite(id, !currentFavorite)
    }

    suspend fun clearAll() {
        historyDao.clearAllHistory()
    }
}
