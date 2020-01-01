package com.nick.moneytransfer.db

import com.nick.moneytransfer.exception.IbanDuplicationException
import com.nick.moneytransfer.exception.InvalidInputDataException
import com.nick.moneytransfer.exception.RecordNotFoundException
import com.nick.moneytransfer.model.User
import com.nick.moneytransfer.util.UserUtil
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.SQLException

/**
 * Business logic implementation for the [User].
 *
 * @author Mykyta Dominov
 */
class UserBo {

    /**
     * Returns the list of users.
     */
    fun getUsers(): List<User> = userDao.getUsers()

    /**
     * Gets [User] by the IBAN.
     */
    fun get(iban: String): User {
        val user = userDao.get(iban)
        if (user == null) {
            LOGGER.debug("""Get: User with the IBAN $iban doesn't exist!""")
            throw RecordNotFoundException("""No record with the IBAN "$iban" was found!""")
        }

        return user
    }

    /**
     * Deletes [User] by IBAN.
     */
    fun delete(iban: String) = userDao.delete(iban)

    /**
     * Creates a new [User].
     */
    fun create(user: User): EntityID<Int> = try {
        userDao.create(user)
    } catch (ex: SQLException) {
        LOGGER.debug("Create: IBAN duplication (${user.iban})!")
        throw IbanDuplicationException(
            "An account with such IBAN is already registered. Please, consider taking another value."
        )
    }

    /**
     * Transfers money to another account
     * and returns the information of the sender account.
     */
    fun moneyTransfer(ibanSender: String, ibanReceiver: String, amount: Float) = transaction {
        val sender: User? = userDao.get(ibanSender)
        if ((sender == null)) {
            LOGGER.debug("""Transfer: The sender with the IBAN $ibanSender doesn't exist!""")
            throw RecordNotFoundException("""No sender with the IBAN "$ibanSender" was found!""")
        }

        val receiver: User? = userDao.get(ibanReceiver)
        if (receiver == null) {
            LOGGER.debug("""Transfer: The sender with the IBAN $ibanReceiver doesn't exist!""")
            throw RecordNotFoundException("""No receiver with the IBAN "$ibanSender" was found!""")
        }

        if (amount <= 0) {
            LOGGER.debug("""Transfer: $ibanSender to $ibanReceiver: transaction with the negative amount ($amount) was blocked.""")
            throw InvalidInputDataException("""The transfer amount cannot be negative or zero ($amount)!""")
        }

        if (sender.balance < amount) {
            LOGGER.debug("""Transfer: Not enough money for the transfer. Balance: ${sender.balance}, Amount: $amount""")
            throw InvalidInputDataException(
                """Not enough money for a transfer! Expected more or equal to $amount, but was ${sender.balance}"""
            )
        }

        val normalizedAmount = UserUtil.normalizeBalance(amount)
        userDao.update(sender.iban, sender.balance - normalizedAmount)
        userDao.update(receiver.iban, receiver.balance + normalizedAmount)
    }

    companion object {
        val LOGGER: Logger = LoggerFactory.getLogger(UserBo::class.java)
        // DAO instance
        private val userDao = UserDao()
    }
}