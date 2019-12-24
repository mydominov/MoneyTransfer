package com.nick.moneytransfer.model

/**
 * Transfer model.
 *
 * @author Mykyta Dominov
 */
data class Transfer(val ibanOfSender: String, val ibanOfReceiver: String, val amount: Float)