// UnHook — Repository for reminder messages shown on intervention screen
package com.unhook.app.data.repository

import com.unhook.app.data.db.ReminderMessageDao
import com.unhook.app.data.model.ReminderMessage
import kotlinx.coroutines.flow.Flow

class MessageRepository(
    private val dao: ReminderMessageDao,
) {
    fun getAll(): Flow<List<ReminderMessage>> = dao.getAll()

    suspend fun getRandomMessage(): ReminderMessage? = dao.getRandom()

    suspend fun addCustomMessage(text: String): Long {
        return dao.insert(ReminderMessage(text = text, isBuiltIn = false))
    }

    suspend fun deleteMessage(message: ReminderMessage) = dao.delete(message)

    suspend fun count(): Int = dao.count()
}
