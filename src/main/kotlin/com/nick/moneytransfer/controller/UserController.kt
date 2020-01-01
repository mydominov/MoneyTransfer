package com.nick.moneytransfer.controller

import com.nick.moneytransfer.db.UserBo
import com.nick.moneytransfer.exception.IbanDuplicationException
import com.nick.moneytransfer.exception.InvalidInputDataException
import com.nick.moneytransfer.exception.RecordNotFoundException
import com.nick.moneytransfer.model.Transfer
import com.nick.moneytransfer.model.User
import io.javalin.http.Context

/**
 * Main controller for operations with [User].
 *
 * @author Mykyta Dominov
 */
object UserController {

    private val USER_BO: UserBo = UserBo()

    /**
     * Returns the list of users.
     */
    fun getUsers(ctx: Context) {
        val users = USER_BO.getUsers()
        ctx.json(users)
    }

    /**
     * Returns the [User] by IBAN.
     */
    fun getUser(ctx: Context) {
        try {
            ctx.json(USER_BO.get(ctx.pathParam("iban")))
        } catch (exception: RecordNotFoundException) {
            ctx.status(404)
            ctx.result("No user with such IBAN was found!")
        }
    }

    /**
     * Removes the [User] from the DB.
     */
    fun deleteUser(ctx: Context) {
        try {
            USER_BO.delete(ctx.pathParam("iban"))
            ctx.result("User was successfully deleted!")
        } catch (exception: InvalidInputDataException) {
            ctx.status(400)
            ctx.result(exception.message ?: "User was not deleted!")
        }
    }

    /**
     * Creates a [User] in the DB.
     */
    fun createUser(ctx: Context) {
        try {
            USER_BO.create(ctx.body<User>())
            ctx.result("User was created successfully!")
        } catch (exception: IbanDuplicationException) {
            ctx.status(400)
            ctx.result(
                exception.message
                    ?: "User was not created! Possible IBAN duplication or syntax exception!"
            )
        }
    }

    /**
     * Transfers money from one account to another.
     */
    fun transferMoney(ctx: Context) {
        try {
            val transfer = ctx.body<Transfer>()
            USER_BO.moneyTransfer(
                transfer.ibanOfSender,
                transfer.ibanOfReceiver,
                transfer.amount
            )
            ctx.json("The money was successfully transferred!")
        } catch (exception: InvalidInputDataException) {
            ctx.status(400)
            ctx.result(exception.message ?: "Invalid amount was provided! Please, re-check your input.")
        } catch (exception: RecordNotFoundException) {
            ctx.status(404)
            ctx.result(exception.message ?: "Invalid IBAN was provided! Please, re-check your input.")
        }
    }
}