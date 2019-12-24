# Money transfer
Money transfer is a lightweight RESTful API for money transfers between accounts written in Kotlin.

## Getting started
1. Install the Gradle on your PC (the instruction is provided [here](https://gradle.org/install/));
2. Clone project from the GitHub (`git clone https://github.com/mydominov/MoneyTransfer.git`);
3. Start the project using the Gradle command: `gradlew run`.

## Available APIs

### Return list of users
`GET -> http://localhost:7000/user`
### Return the user by the IBAN
`GET -> http://localhost:7000/user/get/*IBAN_NUMBER*`<br />
Where `*IBAN_NUMBER*` is replaced by the IBAN value.
### Add new User
```
POST -> http://localhost:7000/user/create
{"iban": "DE89 3704 0044 0532 0130 00", "fullName":"Sebastian Mueller", "amount": 1500}
```
### Delete the user by the IBAN
`DELETE -> http://localhost:7000/user/delete/*IBAN_NUMBER*`<br />
Where `*IBAN_NUMBER*` is replaced by the IBAN value.
### Transfer money
```
POST -> http://localhost:7000/user/send
{"ibanOfSender": "DE46 3084 1181 6666", "ibanOfReceiver": "US30 4321 7528 9264", "amount": 30}
```

## Testing
### Unit tests
In order to run the unit tests you should run `gradlew test`.
### Integration tests
In order to run integration tests you should open two tabs in the terminal.<br />
1st window: You should start the server by running the `gradlew run`.<br />
2nd window: You should start integration tests by running the `gradlew integrationTest`.

## Used software
* Kotlin
* Gradle
* H2 Database
* Javalin
* Exposed
* TestNG
* Hamcrest

## License
Money transfer is MIT Licensed<br />
Copyright © 2019 Mykyta Dominov