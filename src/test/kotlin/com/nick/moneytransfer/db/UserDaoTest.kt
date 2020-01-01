package com.nick.moneytransfer.db

import com.nick.moneytransfer.exception.InvalidInputDataException
import com.nick.moneytransfer.model.User
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.greaterThanOrEqualTo
import org.jetbrains.exposed.dao.EntityID
import org.testng.annotations.BeforeTest
import org.testng.annotations.Test
import java.sql.SQLException

class UserDaoTest {

    @BeforeTest(groups = [TestGroup.UNIT])
    fun testSetup() {
        // Should be replaced by the test DB connection in the production.
        InitDB.initEnvironment()
        userDao = UserDao()
    }

    @Test(groups = [TestGroup.UNIT])
    fun `Get all users test`() {
        val users: List<User> = userDao.getUsers()
        assertThat(users.size, greaterThanOrEqualTo(4))
    }

    @Test(groups = [TestGroup.UNIT])
    fun `simple create test`() {
        val user = User("TEST-RANDOM-IBAN", "Test Name", 8.9f)

        var receivedUser: User? = userDao.get(user.iban)

        assertThat(receivedUser, `is`(nullValue()))

        val entityId: EntityID<Int> = userDao.create(user)
        assertThat(entityId, `is`(notNullValue()))
        receivedUser = userDao.get(user.iban)

        assertThat(receivedUser, `is`(notNullValue()))
        assertThat(receivedUser, `is`(user))

        userDao.delete(user.iban)
    }

    @Test(groups = [TestGroup.UNIT], expectedExceptions = [InvalidInputDataException::class])
    fun `Delete unexisting user`() {
        userDao.delete("RANDOM-IBAN")
    }

    @Test(groups = [TestGroup.UNIT], expectedExceptions = [SQLException::class])
    fun `IBAN duplication test`() {
        val user = User("RANDOM/IBAN", "TestName", 9.0f)

        val receivedUser: User? = userDao.get(user.iban)
        assertThat(receivedUser, `is`(nullValue()))
        userDao.create(user)
        userDao.create(user)

        userDao.delete(user.iban)
    }

    companion object {
        private lateinit var userDao: UserDao
    }
}