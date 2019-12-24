package com.nick.moneytransfer.util

/**
 * Util functions for [User].
 *
 * @author Mykyta Dominov
 */
object UserUtil {
    fun normalizeIBAN(iban: String) = iban.replace(" ", "").toUpperCase()
}