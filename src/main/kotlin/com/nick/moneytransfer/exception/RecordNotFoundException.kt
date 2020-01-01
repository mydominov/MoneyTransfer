package com.nick.moneytransfer.exception

import java.sql.SQLException

/**
 * Exception for the missing record in the database.
 *
 * @author Mykyta Dominov
 */
class RecordNotFoundException(message: String): SQLException(message)