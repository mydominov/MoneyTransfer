package com.nick.moneytransfer.db

import com.nick.moneytransfer.model.User
import com.nick.moneytransfer.util.TestGroup
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.jetbrains.exposed.dao.EntityID
import org.testng.annotations.BeforeTest
import org.testng.annotations.Test

class UserBOTest {

    @BeforeTest(groups = [TestGroup.UNIT])
    fun testSetup() {
        // Should be replaced by the test DB in the production.
        InitDB.initEnvironment()
    }

    @Test(groups = [TestGroup.UNIT])
    fun `Usual transfer`() {
        // Init data
        val sender = User("DE321", "Fritz", 1500f)
        val receiver = User("DE123", "Hans", 300f)

        val expectedOutcome = true
        val senderExpected = User("DE321", "Fritz", 1300f)
        val receiverExpected = User("DE123", "Hans", 500f)

        // Create users in the DB
        val senderEntityId = UserBO.create(sender)
        val receiverEntityId = UserBO.create(receiver)

        assertThat(senderEntityId, `is`(notNullValue()))
        assertThat(receiverEntityId, `is`(notNullValue()))

        // Send transfer
        val transferResult: Boolean = UserBO.moneyTransfer(sender.iban, receiver.iban, 200f)

        assertThat(transferResult, `is`(expectedOutcome))

        val senderResult = UserBO.get(sender.iban)
        val receiverResult = UserBO.get(receiver.iban)

        assertThat(senderResult, `is`(notNullValue()))
        assertThat(receiverResult, `is`(notNullValue()))
        assertThat(senderResult, `is`(senderExpected))
        assertThat(receiverResult, `is`(receiverExpected))

        UserBO.delete(sender.iban)
        UserBO.delete(receiver.iban)
    }

    @Test(groups = [TestGroup.UNIT])
    fun `Negative transfer test`() {
        val expectedOutcome = false
        val sender = User("123", "Fritz Zimmermann", 1500f)
        val receiver = User("321", "Hans Mueller", 300f)

        val senderEntityId = UserBO.create(sender)
        val receiverEntityId = UserBO.create(receiver)

        assertThat(senderEntityId, `is`(notNullValue()))
        assertThat(receiverEntityId, `is`(notNullValue()))

        val actualOutcome: Boolean = UserBO.moneyTransfer(sender.iban, receiver.iban, -200f)

        assertThat(actualOutcome, `is`(expectedOutcome))

        UserBO.delete(sender.iban)
        UserBO.delete(receiver.iban)
    }

    @Test(groups = [TestGroup.UNIT])
    fun `Transfer with negative balance test`() {
        val sender = User("678", "Fritz Zimmermann", -30f)
        val receiver = User("867", "Hans Mueller", 300f)

        val expectedOutcome = false
        val senderEntityId = UserBO.create(sender)
        val receiverEntityId = UserBO.create(receiver)

        assertThat(senderEntityId, `is`(notNullValue()))
        assertThat(receiverEntityId, `is`(notNullValue()))

        val actualOutcome = UserBO.moneyTransfer(sender.iban, receiver.iban, 3f)

        assertThat(actualOutcome, `is`(expectedOutcome))

        UserBO.delete(sender.iban)
        UserBO.delete(receiver.iban)
    }

    @Test(groups = [TestGroup.UNIT])
    fun `Transfer from the invalid account test`() {
        val sender = User("098", "Fritz Zimmermann", 1000f)
        val receiver = User("890", "Hans Mueller", 300f)

        val expectedOutcome = false
        val receiverEntityId = UserBO.create(receiver)

        assertThat(receiverEntityId, `is`(notNullValue()))

        val actualOutcome = UserBO.moneyTransfer(sender.iban, receiver.iban, 3f)

        assertThat(actualOutcome, `is`(expectedOutcome))

        val receiverResult = UserBO.get(receiver.iban)
        assertThat(receiverResult, `is`(notNullValue()))
        assertThat(receiverResult, `is`(receiver))

        UserBO.delete(receiver.iban)
    }

    @Test(groups = [TestGroup.UNIT])
    fun `Transfer to the invalid account test`() {
        val expectedOutcome = false
        val sender = User("285", "Fritz Zimmermann", 1203f)
        val receiver = User("582", "Hans Mueller", 300f)

        val senderEntityId = UserBO.create(sender)

        assertThat(senderEntityId, `is`(notNullValue()))

        val actualOutcome = UserBO.moneyTransfer(sender.iban, receiver.iban, 5f)

        assertThat(actualOutcome, `is`(expectedOutcome))

        val senderResult = UserBO.get(sender.iban)
        assertThat(senderResult, `is`(notNullValue()))
        assertThat(senderResult, `is`(sender))

        UserBO.delete(sender.iban)
    }

    @Test(groups = [TestGroup.UNIT])
    fun `Create user test`() {
        val user = User("TEST-IBAN", "Test Name", 8.9f)

        var receivedUser: User? = UserBO.get(user.iban)

        assertThat(receivedUser, `is`(CoreMatchers.nullValue()))

        val entityId: EntityID<Int>? = UserBO.create(user)
        assertThat(entityId, `is`(notNullValue()))
        receivedUser = UserBO.get(user.iban)

        assertThat(receivedUser, `is`(notNullValue()))
        assertThat(receivedUser, `is`(user))

        val wasDeleted: Boolean = UserBO.delete(user.iban)
        assertThat(wasDeleted, `is`(true))
    }

    @Test(groups = [TestGroup.UNIT])
    fun `Delete unexisting user`() {
        val wasDeleted: Boolean = UserBO.delete("RANDOM-IBAN")
        assertThat(wasDeleted, `is`(false))
    }

    @Test(groups = [TestGroup.UNIT])
    fun `IBAN duplication test`() {
        val user = User("Simple iban", "TestName", 9.0f)

        val receivedUser: User? = UserBO.get(user.iban)
        assertThat(receivedUser, `is`(nullValue()))
        var entityId: EntityID<Int>? = UserBO.create(user)
        assertThat(entityId, `is`(notNullValue()))
        entityId = UserBO.create(user)
        assertThat(entityId, `is`(nullValue()))

        UserBO.delete(user.iban)
    }
}