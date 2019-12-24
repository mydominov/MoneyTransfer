package com.nick.moneytransfer.db

import com.nick.moneytransfer.model.User
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.SQLException
import javax.swing.text.html.parser.Entity

/**
 * Business logic implementation for the [User].
 *
 * @author Mykyta Dominov
 */
object UserBO {

    // DAO instance
    private val userDao = UserDao()

    /**
     * Returns the list of users.
     */
    fun getUsers(): List<User> = userDao.getUsers()

    /**
     * Gets [User] by the IBAN.
     */
    fun get(iban: String): User? = userDao.get(iban)

    /**
     * Deletes [User] by IBAN.
     */
    fun delete(iban: String): Boolean = userDao.delete(iban)

    /**
     * Creates a new [User].
     */
    fun create(user: User): EntityID<Int>? = try {
        userDao.create(user)
    } catch (ex: SQLException) {
        null
    }

    /**
     * Transfers money to another account
     * and returns the information of the sender account.
     */
    fun moneyTransfer(ibanSender: String, ibanReceiver: String, amount: Float): Boolean = transaction {
        val sender: User = userDao.get(ibanSender) ?: return@transaction false
        val receiver: User = userDao.get(ibanReceiver) ?: return@transaction false

        if (amount <= 0 || sender.balance < amount) {
            return@transaction false
        }

        userDao.update(sender.iban, sender.balance - amount)
        userDao.update(receiver.iban, receiver.balance + amount)

        return@transaction true
    }
}