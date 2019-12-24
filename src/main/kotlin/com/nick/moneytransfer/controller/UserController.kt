package com.nick.moneytransfer.controller

import com.nick.moneytransfer.db.UserBO
import com.nick.moneytransfer.model.Transfer
import com.nick.moneytransfer.model.User
import io.javalin.http.Context
import org.jetbrains.exposed.dao.EntityID

/**
 * Main controller for operations with [User].
 *
 * @author Mykyta Dominov
 */
object UserController {

    /**
     * Returns the list of users.
     */
    fun getUsers(ctx: Context) {
        val users = UserBO.getUsers()

        if (users.isEmpty()) {
            ctx.result("No users are present in the database.")
        } else {
            ctx.json(users)
        }
    }

    /**
     * Returns the [User] by IBAN.
     */
    fun getUser(ctx: Context) {
        val user: User? = UserBO.get(ctx.pathParam("iban"))

        if (user == null) {
            ctx.status(400)
            ctx.result("No user with such IBAN was found!")
        } else {
            ctx.json(user)
        }
    }

    /**
     * Removes the [User] from the DB.
     */
    fun deleteUser(ctx: Context) {
        val wasDeleted: Boolean = UserBO.delete(ctx.pathParam("iban"))

        if (wasDeleted) {
            ctx.result("User was successfully deleted!")
        } else {
            ctx.status(400)
            ctx.result("User was not deleted!")
        }
    }

    /**
     * Creates a [User] in the DB.
     */
    fun createUser(ctx: Context) {
        val entityID: EntityID<Int>? = UserBO.create(ctx.body<User>())

        if (entityID == null) {
            ctx.status(400)
            ctx.result("User was not created! Possible IBAN duplication or syntax exception!")
        } else {
            ctx.result("User was created successfully!")
        }
    }

    /**
     * Transfers money from one account to another.
     */
    fun transferMoney(ctx: Context) {
        val transfer = ctx.body<Transfer>()

        val wasTransferred: Boolean = UserBO.moneyTransfer(
            transfer.ibanOfSender,
            transfer.ibanOfReceiver,
            transfer.amount
        )

        if (!wasTransferred) {
            ctx.status(400)
            ctx.result("Invalid data was provided! Please, re-check your input.")
        } else {
            ctx.json(wasTransferred)
        }
    }
}