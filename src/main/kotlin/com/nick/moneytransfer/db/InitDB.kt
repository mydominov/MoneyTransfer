package com.nick.moneytransfer.db

import com.nick.moneytransfer.db.table.UsersTable
import com.nick.moneytransfer.model.User
import org.h2.jdbcx.JdbcDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * Initialize the DB with tables and data.
 *
 * @author Mykyta Dominov
 */
object InitDB {

    /**
     * Connects to H2, creates tables and fills them with data.
     */
    fun initEnvironment() {
        Database.connect(createDataSource())
        createTablesWithInitialData()
    }

    /**
     * Creates the data source.
     */
    private fun createDataSource(): JdbcDataSource {
        val dataSource = JdbcDataSource()
        // Can be replaced with the property file.
        dataSource.setURL("jdbc:h2:mem:regular;DB_CLOSE_DELAY=-1;")
        return dataSource
    }

    /**
     * Creates [UsersTable] and fills it with some data.
     */
    private fun createTablesWithInitialData() = transaction {
        SchemaUtils.create(UsersTable)

        UserBO.create(User("DE46 3084 1181 6666", "Sebastian Mueller", 1500.60f))
        UserBO.create(User("US30 4321 7528 9264", "James Smith", 841.38f))
        UserBO.create(User("RU11 0638 1111 6945", "Ivan Petrov", 210.21f))
        UserBO.create(User("PL25 7001 7492 4620", "Grzegorz Brzęczyszczykiewicz", 300.44f))
    }
}