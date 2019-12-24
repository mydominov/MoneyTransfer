package com.nick.moneytransfer.db.table

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass

/**
 * Representation of the [User] from the exposed framework.
 */
class UserTable(id: EntityID<Int>): IntEntity(id) {
    var iban by UsersTable.iban
    var fullName by UsersTable.fullName
    var balance by UsersTable.balance

    companion object : IntEntityClass<UserTable>(UsersTable)
}