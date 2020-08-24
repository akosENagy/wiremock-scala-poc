package com.wanari.wiremock_poc

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.stubbing.Scenario
import com.wanari.wiremock_poc.ApiCallerService.{AdminApiResponse, ScenarioStateResponse, Unauthorized, UnitResponse, User, userJsonFormatter}
import com.wanari.wiremock_poc.testutil.WireMockTest
import spray.json.DefaultJsonProtocol._
import spray.json._

class ApiCallerServiceSpec extends WireMockTest with ScalatestRouteTest {
  override def hostName: String = "localhost"
  override def port: Int        = 8080

  trait TestScope {
    val apiCallerService: ApiCallerService = new ApiCallerService()

    val userListUri: String      = "/api/users"
    val adminUri: String         = "/api/admin"
    val scenarioStateUri: String = "/api/scenario"

    val user1: User = User(name = "Gipsz Jakab", username = "j.gibbs")
    val user2: User = User(name = "Jeremiah Sutherford Saint-Jones the Last", username = "youlostthegame")

    val newUser: User = User(name = "John Smith", username = "the_doctor")

    val SCENARIO_NAME = "My First Scenario"
    val SECOND_STATE  = "Second state"
    val THIRD_STATE   = "Third state"

    def createScenarioStub(currentState: String, nextState: String, response: String): Unit = {
      mockServer.stubFor(
        get(scenarioStateUri)
          .inScenario(SCENARIO_NAME)
          .whenScenarioStateIs(currentState)
          .willSetStateTo(nextState)
          .willReturn(
            okJson(response),
          ),
      )
    }
  }

  "ApiCallerService" should {

    "#getUsers" should {
      "return a List of users" in new TestScope {
        private val users: List[User] = List(user1, user2)

        mockServer.stubFor(
          get(userListUri)
            .willReturn(
              okJson(users.toJson.compactPrint),
            ),
        )

        await(apiCallerService.getAllUsers()) shouldBe Right(users)

        mockServer.verify(
          getRequestedFor(urlEqualTo(userListUri)),
        )
      }
    }

    "#addUser" should {
      "post a User" in new TestScope {
        private val newUserJson = equalToJson(newUser.toJson.compactPrint)

        mockServer.stubFor(
          post(userListUri)
            .withHeader("Content-Type", equalTo("application/json"))
            .withRequestBody(newUserJson)
            .willReturn(
              okJson("{}"),
            ),
        )

        await(apiCallerService.addUser(newUser)) shouldBe Right(UnitResponse())

        mockServer.verify(
          postRequestedFor(urlEqualTo(userListUri))
            .withHeader("Content-Type", equalTo("application/json"))
            .withRequestBody(newUserJson),
        )
      }
    }

    "#getAdmin" should {
      "return OK" in new TestScope {
        private val response = AdminApiResponse("Successful auth")
        mockServer.stubFor(
          get(adminUri)
            .withHeader("ApiKey", equalTo("secret"))
            .willReturn(
              okJson(response.toJson.compactPrint),
            ),
        )

        await(apiCallerService.getAdmin()) shouldBe Right(response)

        mockServer.verify(
          getRequestedFor(urlEqualTo(adminUri))
            .withHeader("ApiKey", equalTo("secret")),
        )
      }

      "return Unauthorized" in new TestScope {
        mockServer.stubFor(
          get(adminUri)
            .willReturn(
              unauthorized(),
            ),
        )

        await(apiCallerService.getAdmin()) shouldBe Left(Unauthorized())

        mockServer.verify(
          getRequestedFor(urlEqualTo(adminUri))
            .withHeader("ApiKey", equalTo("secret")),
        )
      }
    }

    "#checkScenarioState" should {
      "return the current Scenario State" in new TestScope {
        createScenarioStub(Scenario.STARTED, SECOND_STATE, ScenarioStateResponse(Scenario.STARTED).toJson.compactPrint)
        createScenarioStub(SECOND_STATE, THIRD_STATE, ScenarioStateResponse(SECOND_STATE).toJson.compactPrint)
        createScenarioStub(THIRD_STATE, THIRD_STATE, ScenarioStateResponse(THIRD_STATE).toJson.compactPrint)

        await(apiCallerService.checkScenarioState()) shouldBe Right(ScenarioStateResponse(Scenario.STARTED))
        await(apiCallerService.checkScenarioState()) shouldBe Right(ScenarioStateResponse(SECOND_STATE))
        await(apiCallerService.checkScenarioState()) shouldBe Right(ScenarioStateResponse(THIRD_STATE))

        mockServer.verify(
          3,
          getRequestedFor(urlEqualTo(scenarioStateUri)),
        )
      }
    }
  }
}
