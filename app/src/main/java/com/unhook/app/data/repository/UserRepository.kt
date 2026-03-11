// UnHook — Repository for User and Partner DB operations
package com.unhook.app.data.repository

import com.unhook.app.data.db.PartnerDao
import com.unhook.app.data.db.UserDao
import com.unhook.app.data.model.Partner
import com.unhook.app.data.model.User
import kotlinx.coroutines.flow.Flow

class UserRepository(
    private val userDao: UserDao,
    private val partnerDao: PartnerDao,
) {
    fun getMe(): Flow<User?> = userDao.getMe()

    suspend fun getMeOnce(): User? = userDao.getMeOnce()

    suspend fun createUser(name: String, emojiAvatar: String): Long {
        val user = User(name = name, emojiAvatar = emojiAvatar, isMe = true)
        return userDao.insert(user)
    }

    suspend fun updateUser(user: User) = userDao.update(user)

    fun getPartner(): Flow<Partner?> = partnerDao.getPartner()

    suspend fun getPartnerOnce(): Partner? = partnerDao.getPartnerOnce()

    suspend fun createPartner(name: String, emojiAvatar: String, pairingCode: String): Long {
        val partner = Partner(name = name, emojiAvatar = emojiAvatar, pairingCode = pairingCode)
        return partnerDao.insert(partner)
    }

    suspend fun updatePartner(partner: Partner) = partnerDao.update(partner)
}
