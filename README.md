## This is a proof of concept repository for an introductory look at using Wiremock

Wiremock is an HTTP API mocking tool, and the following repository is meant to provide a glimpse into unit testing HTTP API integration with Wiremock.

The `com.wanari.wiremock_poc.ApiCallerService` is meant to simulate a service integrating with an HTTP API, and the `com.wanari.wiremock_poc.ApiCallerServiceSpec` contains some unit tests for the above mentioned service using Wiremock.

The `com.wanari.wiremock_poc.testutil.Test` and `com.wanari.wiremock_poc.testutil.WireMockTest` classes contain some syntactic sugar, and the boilerplate code required to set up Wiremock.

The `standalone` directory contains two things:
- a `mappings` directory with an `example_mappings.json` file containing the JSON API definition of the request-response pairs used in the Scala code
- a `curl commands.txt` file containing the cURL commands that can reproduce the HTTP requests that the `ApiCallerService` makes.

These two together can be used to explore the same functionality of Wiremock that the Scala code does, but with the JSON API for running Wiremock as a standalone service. 

For more information about Wiremock, please refer to the official documentation: http://wiremock.org/docs/