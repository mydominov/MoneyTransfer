package com.nick.moneytransfer

import com.nick.moneytransfer.model.User
import com.nick.moneytransfer.util.TestGroup
import io.restassured.RestAssured
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import io.restassured.path.json.JsonPath
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.greaterThanOrEqualTo
import org.testng.annotations.BeforeTest
import org.testng.annotations.Test


class UserIT {

    @BeforeTest(groups = [TestGroup.INTEGRATION])
    fun initTest() {
        RestAssured.port = 7000
    }

    @Test(groups = [TestGroup.INTEGRATION])
    fun `get all users test`() {
        val ibans = JsonPath.from(getUsers()).getList<String>("iban")
        assertThat(ibans, `is`(notNullValue()))
        assertThat(ibans.size, greaterThanOrEqualTo(4))
    }

    @Test(groups = [TestGroup.INTEGRATION])
    fun `create and delete user test`() {
        val expectedUser = User("EN152345243561353", "Bill Grey", 500f)
        val jsonUser = "{\"iban\": \"EN152345243561353\", \"fullName\": \"Bill Grey\", \"balance\": 500}"

        // Insert [User] into the DB.
        createUser(jsonUser)

        // Check that user is in the DB.
        val jsonResponse = getUser(expectedUser.iban)
        val iban = JsonPath.from(jsonResponse).getString("iban")
        val fullName = JsonPath.from(jsonResponse).getString("fullName")
        val balance = JsonPath.from(jsonResponse).getFloat("balance")

        assertThat(iban, `is`(expectedUser.iban))
        assertThat(fullName, `is`(expectedUser.fullName))
        assertThat(balance, `is`(expectedUser.balance))

        // Removes [User] from the DB.
        deleteUser(expectedUser.iban)

        // Check that [User] was deleted.
        given()
            .get("""/user/get/${expectedUser.iban}""")
            .then()
            .statusCode(400)
            .extract()
            .response()
            .asString()
    }

    @Test(groups = [TestGroup.INTEGRATION])
    fun `simple transfer test`() {
        val senderJson = "{\"iban\": \"DE000\", \"fullName\": \"Carl Benz\", \"balance\": 50000}"
        val receiverJson = "{\"iban\": \"DE001\", \"fullName\": \"Max Planck\", \"balance\": 1000}"

        // Insert users into the DB.
        createUser(senderJson)
        createUser(receiverJson)

        // Money transfer.
        given()
            .contentType(ContentType.JSON)
            .body("{\"ibanOfSender\": \"DE000\", \"ibanOfReceiver\": \"DE001\", \"amount\": 1000}")
            .post("/user/send")
            .then()
            .statusCode(200)

        val senderBalance = JsonPath.from(getUser("DE000")).getFloat("balance")
        val receiverBalance = JsonPath.from(getUser("DE001")).getFloat("balance")

        assertThat(senderBalance, `is`(49000f))
        assertThat(receiverBalance, `is`(2000f))

        // Removes users from the DB.
        deleteUser("DE000")
        deleteUser("DE001")
    }

    @Test(groups = [TestGroup.INTEGRATION])
    fun `transfer with negative amount test`() {
        val senderJson = "{\"iban\": \"DE111\", \"fullName\": \"Otto Lilienthal\", \"balance\": 3000}"
        val receiverJson = "{\"iban\": \"DE221\", \"fullName\": \"Robert Koch\", \"balance\": 2000}"

        // Insert users into the DB.
        createUser(senderJson)
        createUser(receiverJson)

        // Money transfer.
        given()
            .contentType(ContentType.JSON)
            .body("{\"ibanOfSender\": \"DE111\", \"ibanOfReceiver\": \"DE221\", \"amount\": -30}")
            .post("/user/send")
            .then()
            .statusCode(400)

        val senderBalance = JsonPath.from(getUser("DE111")).getFloat("balance")
        val receiverBalance = JsonPath.from(getUser("DE221")).getFloat("balance")

        assertThat(senderBalance, `is`(3000f))
        assertThat(receiverBalance, `is`(2000f))

        // Removes users from the DB.
        deleteUser("DE111")
        deleteUser("DE221")
    }

    @Test(groups = [TestGroup.INTEGRATION])
    fun `transfer with zero amount test`() {
        val senderJson = "{\"iban\": \"DE100\", \"fullName\": \"Rudolf Diesel\", \"balance\": 6000}"
        val receiverJson = "{\"iban\": \"DE200\", \"fullName\": \"Werner von Siemens\", \"balance\": 8000}"

        // Insert users into the DB.
        createUser(senderJson)
        createUser(receiverJson)

        // Money transfer.
        given()
            .contentType(ContentType.JSON)
            .body("{\"ibanOfSender\": \"DE100\", \"ibanOfReceiver\": \"DE200\", \"amount\": 0}")
            .post("/user/send")
            .then()
            .statusCode(400)

        val senderBalance = JsonPath.from(getUser("DE100")).getFloat("balance")
        val receiverBalance = JsonPath.from(getUser("DE200")).getFloat("balance")

        assertThat(senderBalance, `is`(6000f))
        assertThat(receiverBalance, `is`(8000f))

        // Removes users from the DB.
        deleteUser("DE100")
        deleteUser("DE200")
    }

    @Test(groups = [TestGroup.INTEGRATION])
    fun `transfer to unknown account test`() {
        val senderJson = "{\"iban\": \"DE479\", \"fullName\": \"Friedrich Schiller\", \"balance\": 8000}"

        // Insert users into the DB.
        createUser(senderJson)

        // Money transfer.
        given()
            .contentType(ContentType.JSON)
            .body("{\"ibanOfSender\": \"DE479\", \"ibanOfReceiver\": \"DE666\", \"amount\": 5}")
            .post("/user/send")
            .then()
            .statusCode(400)

        val senderBalance = JsonPath.from(getUser("DE479")).getFloat("balance")

        assertThat(senderBalance, `is`(8000f))

        // Removes users from the DB.
        deleteUser("DE479")
    }

    @Test(groups = [TestGroup.INTEGRATION])
    fun `transfer from unknown account test`() {
        val receiverJson = "{\"iban\": \"DE202\", \"fullName\": \"Michael Schumacher\", \"balance\": 5500}"

        // Insert users into the DB.
        createUser(receiverJson)

        // Money transfer.
        given()
            .contentType(ContentType.JSON)
            .body("{\"ibanOfSender\": \"DE666\", \"ibanOfReceiver\": \"DE202\", \"amount\": 999999}")
            .post("/user/send")
            .then()
            .statusCode(400)

        val receiverBalance = JsonPath.from(getUser("DE202")).getFloat("balance")

        assertThat(receiverBalance, `is`(5500f))

        // Removes users from the DB.
        deleteUser("DE202")
    }

    companion object {
        private fun getUsers() = given()
            .get("/user")
            .then()
            .statusCode(200)
            .extract()
            .response()
            .asString()

        private fun getUser(iban: String) = given()
            .get("""/user/get/$iban""")
            .then()
            .statusCode(200)
            .extract()
            .response()
            .asString()

        private fun createUser(json: String) = given()
            .contentType(ContentType.JSON)
            .body(json)
            .post("/user/create")
            .then()
            .statusCode(200)

        private fun deleteUser(iban: String) = given()
            .delete("""/user/delete/$iban""")
            .then()
            .statusCode(200)
            .extract()
            .response()
            .asString()
    }
}