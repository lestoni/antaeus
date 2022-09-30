## Antaeus

Antaeus (/ænˈtiːəs/), in Greek mythology, a giant of Libya, the son of the sea god Poseidon and the Earth goddess Gaia. He compelled all strangers who were passing through the country to wrestle with him. Whenever Antaeus touched the Earth (his mother), his strength was renewed, so that even if thrown to the ground, he was invincible. Heracles, in combat with him, discovered the source of his strength and, lifting him up from Earth, crushed him to death.

Welcome to our challenge.

## The challenge

As most "Software as a Service" (SaaS) companies, Pleo needs to charge a subscription fee every month. Our database contains a few invoices for the different markets in which we operate. Your task is to build the logic that will schedule payment of those invoices on the first of the month. While this may seem simple, there is space for some decisions to be taken and you will be expected to justify them.

## Instructions

Fork this repo with your solution. Ideally, we'd like to see your progression through commits, and don't forget to update the README.md to explain your thought process.

Please let us know how long the challenge takes you. We're not looking for how speedy or lengthy you are. It's just really to give us a clearer idea of what you've produced in the time you decided to take. Feel free to go as big or as small as you want.

## Developing

Requirements:
- \>= Java 11 environment

Open the project using your favorite text editor. If you are using IntelliJ, you can open the `build.gradle.kts` file and it is gonna setup the project in the IDE for you.

### Building

```
./gradlew build
```

### Running

There are 2 options for running Anteus. You either need libsqlite3 or docker. Docker is easier but requires some docker knowledge. We do recommend docker though.

*Running Natively*

Native java with sqlite (requires libsqlite3):

If you use homebrew on MacOS `brew install sqlite`.

```
./gradlew run
```

*Running through docker*

Install docker for your platform

```
docker build -t antaeus
docker run antaeus
```

### App Structure
The code given is structured as follows. Feel free however to modify the structure to fit your needs.
```
├── buildSrc
|  | gradle build scripts and project wide dependency declarations
|  └ src/main/kotlin/utils.kt 
|      Dependencies
|
├── pleo-antaeus-app
|       main() & initialization
|
├── pleo-antaeus-core
|       This is probably where you will introduce most of your new code.
|       Pay attention to the PaymentProvider and BillingService class.
|
├── pleo-antaeus-data
|       Module interfacing with the database. Contains the database 
|       models, mappings and access layer.
|
├── pleo-antaeus-models
|       Definition of the Internal and API models used throughout the
|       application.
|
└── pleo-antaeus-rest
        Entry point for HTTP REST API. This is where the routes are defined.
```

### Main Libraries and dependencies
* [Exposed](https://github.com/JetBrains/Exposed) - DSL for type-safe SQL
* [Javalin](https://javalin.io/) - Simple web framework (for REST)
* [kotlin-logging](https://github.com/MicroUtils/kotlin-logging) - Simple logging framework for Kotlin
* [JUnit 5](https://junit.org/junit5/) - Testing framework
* [Mockk](https://mockk.io/) - Mocking library
* [Sqlite3](https://sqlite.org/index.html) - Database storage engine

Happy hacking 😁!


## Hacking Thoughts 

Aloha! This challenge has taken me 5 days. My background is with Javascript/Typescript/Nodejs/Go and with Kotlin i have just knowhow. I spent 3 days getting a refresher on Kotlin and 2 days getting into the project.

At look at the challenge, it is clear we are developing a recurring payment charging scheduler. In order to design the proper architecture approach i thought of the following solutions:

- Create an endpoint `POST /rest/v1/billing/start` that will be triggered to start processing invoices. This offers an  This is rather a trivial solution however it falls short as it will require manual triggering and additionally more endpoints or config payload maybe required for a fine grained controlling the scheduling.
- Incorporating a cron or concurrent timer mechanism that runs on specified run time. 
- Use of a queuing or caching mechanism i.e ZeroMQ, Redis, Memcache etc. This is a more scalable solution as customers grow(implying growth of invoices too). As soon as an invoice is created, it's required data is stored in queue and can be processed when recurring clock approaches. This however requires additional tool resources for infrastructure to achieve it.


Out of the three approaches, i opted to use concurrent timers or cron job for this solution to keep it simple; as the needs for improvement grows, last solution can be used.

### Product/Technical Questions

- How to track failed payment charging and what to do with them ?
        - Added as part of general improvement
- How to handle different timezones for each customer ?
        - This is out of scope for this challenge
- How to handle growing large amounts of invoices, to do batch processsing or queueing mechanism ?
        - I query for invoices in batches of 100 but configurable as such how pagination works
- Is the scheduler manually triggered or rans as such as a cron job ?
        - Solution uses concurrent timer that runs periodically i.e every month
- How often to retry the failed invoices ?
        - The scheduler retries up-to 2 times at which and endpoint is provided(part of general improvements)
- How to handle payment Provider failures, do we retry and stop jobs then report errors ?
        - See General Improvements
- How to report concurrent timer errors ?

### Billing Service

The billing service will maintain the following functionalities:
- Retrieving Pending Invoices for processing recurring monthly ones
- Retrieving Failed Invoices to retry charging
- Charging a customer via payment Provider for an invoice
- Updating status of Invoices to either PAID or FAILED


### Scheduler Service

This is a configurable general service whose functionality is to run services that need to be scheduled for specific times aka job(s) schedule.

For the purpose of this challenge, we will have only billing service as a job running in the scheduler service


### General Improvements

- An endpoint to be triggered for a failed invoice payment once the threshold; this is useful for the api consumers to trigger payment once it can be done again.
- Best to keep billing logs for auditing.
- To handle failures/errors from payment providers we could use a circuit breaker
- Dispatch notification to the customer upon charging for an invoice either succeeded or failed. It could be an email or push notification or SMS e.t.c
