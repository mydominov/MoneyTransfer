package com.nick.moneytransfer.db.table

import org.jetbrains.exposed.dao.IntIdTable
import org.jetbrains.exposed.sql.Column

/**
 * Representation of users from the exposed framework.
 */
object UsersTable: IntIdTable() {
    val iban: Column<String> = varchar("iban", 34).uniqueIndex()
    val fullName: Column<String> = varchar("full_name", 54)
    val balance: Column<Float> = float("balance")
}