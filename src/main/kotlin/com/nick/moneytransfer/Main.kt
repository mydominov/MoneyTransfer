@file:JvmName("Main")
package com.nick.moneytransfer

import com.nick.moneytransfer.controller.UserController
import com.nick.moneytransfer.db.InitDB
import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder.*

/**
 * Main entry point of the project.
 */
fun main() {
    // Create the DB connection and tables with some data in the H2.
    InitDB.initEnvironment()

    // Set up controllers and routing.
    val app = Javalin.create().apply {
        exception(Exception::class.java) { e, _ -> e.printStackTrace() }
        error(404) { it.json("not found") }
    }.start(7000)

    app.routes {
        path("user") {
            get(UserController::getUsers)
            path("/create") {
                post(UserController::createUser)
            }
            path("/get") {
                get("/:iban", UserController::getUser)
            }
            path("/delete") {
                delete("/:iban", UserController::deleteUser)
            }
            path("/send") {
                post(UserController::transferMoney)
            }
        }
    }
}
