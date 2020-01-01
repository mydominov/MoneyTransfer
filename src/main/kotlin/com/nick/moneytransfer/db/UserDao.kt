package com.nick.moneytransfer.db

import com.nick.moneytransfer.db.table.UserTable
import com.nick.moneytransfer.db.table.UsersTable
import com.nick.moneytransfer.exception.InvalidInputDataException
import com.nick.moneytransfer.exception.RecordNotFoundException
import com.nick.moneytransfer.model.User
import com.nick.moneytransfer.util.UserUtil
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Main DAO for [User].
 *
 * @author Mykyta Dominov
 */
class UserDao {

    /**
     * Creates [User] in the DB.
     */
    fun create(user: User): EntityID<Int> = transaction {
        return@transaction UsersTable.insertAndGetId {
            it[iban] = UserUtil.normalizeIBAN(user.iban)
            it[fullName] = user.fullName
            it[balance] = UserUtil.normalizeBalance(user.balance)
        }
    }

    /**
     * Updates the data of the [User] in the DB.
     */
    fun update(iban: String, balance: Float) = transaction {
        val result: Int = UsersTable.update({ UsersTable.iban eq UserUtil.normalizeIBAN(iban) }) {
            it[UsersTable.balance] = balance
        }

        if (result == 0) {
            LOGGER.debug("""Update: IBAN $iban doesn't exist.""")
            throw RecordNotFoundException("""Invalid IBAN $iban!""")
        }
    }

    /**
     * Gets the [User] from the DB using the IBAN.
     */
    fun get(iban: String): User? = transaction {
        return@transaction UserTable
            .wrapRows(
                UsersTable.select { UsersTable.iban eq UserUtil.normalizeIBAN(iban) })
            .firstOrNull()
            ?.let { user ->
                User(user.iban, user.fullName, user.balance)
            }
    }

    /**
     * Gets the list of all the users.
     */
    fun getUsers(): List<User> = transaction {
        return@transaction UserTable
            .wrapRows(UsersTable.selectAll())
            .limit(10)
            .toList()
            .map {
                User(
                    it.iban,
                    it.fullName,
                    it.balance
                )
            }
    }

    /**
     * Deletes the [User]
     */
    fun delete(iban: String) = transaction {
        val result = UsersTable.deleteWhere { UsersTable.iban eq iban }
        if (result == 0) {
            LOGGER.debug("Delete: IBAN $iban doesn't exist.")
            throw InvalidInputDataException("""IBAN $iban doesn't exist!""")
        }
    }

    companion object {
        val LOGGER: Logger = LoggerFactory.getLogger(UserDao::class.java)
    }
}