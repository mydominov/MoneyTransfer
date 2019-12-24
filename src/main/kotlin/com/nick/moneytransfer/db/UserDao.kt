package com.nick.moneytransfer.db

import com.nick.moneytransfer.util.UserUtil
import com.nick.moneytransfer.db.table.UserTable
import com.nick.moneytransfer.db.table.UsersTable
import com.nick.moneytransfer.model.User
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

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
            it[balance] = user.balance
        }
    }

    /**
     * Updates the data of the [User] in the DB.
     */
    fun update(iban: String, balance: Float) = transaction {
        UsersTable.update({ UsersTable.iban eq UserUtil.normalizeIBAN(iban) }) {
            it[UsersTable.balance] = balance
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
    fun delete(iban: String): Boolean = transaction {
        val result = UsersTable.deleteWhere { UsersTable.iban eq iban }

        return@transaction result != 0
    }
}