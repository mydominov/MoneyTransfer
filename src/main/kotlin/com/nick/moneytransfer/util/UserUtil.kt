package com.nick.moneytransfer.util

import kotlin.math.ceil

/**
 * Util functions for [User].
 *
 * @author Mykyta Dominov
 */
object UserUtil {
    fun normalizeIBAN(iban: String) = iban.replace(" ", "").toUpperCase()
    fun normalizeBalance(value: Float) = ceil(value * 100) / 100
}