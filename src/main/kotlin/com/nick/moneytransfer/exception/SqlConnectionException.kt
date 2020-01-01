package com.nick.moneytransfer.exception

import java.sql.SQLException

/**
 * Exception for the invalid connection properties.
 *
 * @author Mykyta Dominov
 */
class SqlConnectionException(message: String): SQLException(message)