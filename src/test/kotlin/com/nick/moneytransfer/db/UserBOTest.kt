package com.nick.moneytransfer.db

import com.nick.moneytransfer.exception.InvalidInputDataException
import com.nick.moneytransfer.exception.RecordNotFoundException
import com.nick.moneytransfer.model.User
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.testng.annotations.BeforeTest
import org.testng.annotations.Test

class UserBOTest {

    @BeforeTest(groups = [TestGroup.UNIT])
    fun testSetup() {
        // Should be replaced by the test DB in the production.
        InitDB.initEnvironment()
        userBo = UserBo()
    }

    @Test(groups = [TestGroup.UNIT])
    fun `Usual transfer`() {
        // Init data
        val sender = User("DE321", "Fritz", 1500f)
        val receiver = User("DE123", "Hans", 300f)

        val senderExpected = User("DE321", "Fritz", 1300f)
        val receiverExpected = User("DE123", "Hans", 500f)

        // Create users in the DB
        val senderEntityId = userBo.create(sender)
        val receiverEntityId = userBo.create(receiver)

        assertThat(senderEntityId, `is`(notNullValue()))
        assertThat(receiverEntityId, `is`(notNullValue()))

        // Send transfer
        userBo.moneyTransfer(sender.iban, receiver.iban, 200f)

        val senderResult = userBo.get(sender.iban)
        val receiverResult = userBo.get(receiver.iban)

        assertThat(senderResult, `is`(notNullValue()))
        assertThat(receiverResult, `is`(notNullValue()))
        assertThat(senderResult, `is`(senderExpected))
        assertThat(receiverResult, `is`(receiverExpected))

        userBo.delete(sender.iban)
        userBo.delete(receiver.iban)
    }

    @Test(groups = [TestGroup.UNIT], expectedExceptions = [InvalidInputDataException::class])
    fun `Negative transfer test`() {
        val sender = User("123", "Fritz Zimmermann", 1500f)
        val receiver = User("321", "Hans Mueller", 300f)

        val senderEntityId = userBo.create(sender)
        val receiverEntityId = userBo.create(receiver)

        assertThat(senderEntityId, `is`(notNullValue()))
        assertThat(receiverEntityId, `is`(notNullValue()))

        userBo.moneyTransfer(sender.iban, receiver.iban, -200f)

        userBo.delete(sender.iban)
        userBo.delete(receiver.iban)
    }

    @Test(groups = [TestGroup.UNIT], expectedExceptions = [InvalidInputDataException::class])
    fun `Transfer with negative balance test`() {
        val sender = User("678", "Fritz Zimmermann", -30f)
        val receiver = User("867", "Hans Mueller", 300f)

        val senderEntityId = userBo.create(sender)
        val receiverEntityId = userBo.create(receiver)

        assertThat(senderEntityId, `is`(notNullValue()))
        assertThat(receiverEntityId, `is`(notNullValue()))

        userBo.moneyTransfer(sender.iban, receiver.iban, 3f)

        userBo.delete(sender.iban)
        userBo.delete(receiver.iban)
    }

    @Test(groups = [TestGroup.UNIT], expectedExceptions = [RecordNotFoundException::class])
    fun `Transfer from the invalid account test`() {
        val sender = User("098", "Fritz Zimmermann", 1000f)
        val receiver = User("890", "Hans Mueller", 300f)

        val receiverEntityId = userBo.create(receiver)

        assertThat(receiverEntityId, `is`(notNullValue()))

        userBo.moneyTransfer(sender.iban, receiver.iban, 3f)
    }

    @Test(groups = [TestGroup.UNIT], expectedExceptions = [RecordNotFoundException::class])
    fun `Transfer to the invalid account test`() {
        val sender = User("285", "Fritz Zimmermann", 1203f)
        val receiver = User("582", "Hans Mueller", 300f)

        val senderEntityId = userBo.create(sender)

        assertThat(senderEntityId, `is`(notNullValue()))

        userBo.moneyTransfer(sender.iban, receiver.iban, 5f)

        val senderResult = userBo.get(sender.iban)
        assertThat(senderResult, `is`(notNullValue()))
        assertThat(senderResult, `is`(sender))

        userBo.delete(sender.iban)
    }

    @Test(groups = [TestGroup.UNIT])
    fun `Create user test`() {
        val user = User("TEST-IBAN", "Test Name", 8.9f)

        userBo.create(user)
        val receivedUser = userBo.get(user.iban)

        assertThat(receivedUser, `is`(notNullValue()))
        assertThat(receivedUser, `is`(user))

        userBo.delete(user.iban)
    }

    @Test(groups = [TestGroup.UNIT], expectedExceptions = [InvalidInputDataException::class])
    fun `Delete unexisting user`() {
        userBo.delete("RANDOM-IBAN")
    }

    @Test(groups = [TestGroup.UNIT], expectedExceptions = [RecordNotFoundException::class])
    fun `IBAN duplication test`() {
        val user = User("Simple iban", "TestName", 9.0f)
        val receivedUser: User? = userBo.get(user.iban)
        assertThat(receivedUser, `is`(nullValue()))
        userBo.create(user)
        userBo.create(user)
    }

    companion object {
        private lateinit var userBo: UserBo
    }
}