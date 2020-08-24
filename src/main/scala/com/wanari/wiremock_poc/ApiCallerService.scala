package com.wanari.wiremock_poc

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.unmarshalling.Unmarshal
import com.wanari.wiremock_poc.ApiCallerService.{AdminApiResponse, ApiError, ApiErrorOr, BadRequest, ScenarioStateResponse, Unauthorized, UnitResponse, User}
import spray.json.DefaultJsonProtocol._
import spray.json.RootJsonFormat

import scala.concurrent.{ExecutionContext, Future}

class ApiCallerService(
    implicit
    as: ActorSystem,
    ec: ExecutionContext,
) {

  private val baseUrl: String = "http://localhost:8080"

  def getAllUsers(): ApiErrorOr[List[User]] = {
    val usersUrl = s"$baseUrl/api/users"

    val request: HttpRequest = HttpRequest(
      method = HttpMethods.GET,
      uri = usersUrl,
    )

    sendRequestAndParseInto[List[User]](request)(BadRequest())
  }

  def addUser(user: User): ApiErrorOr[UnitResponse] = {
    import spray.json._

    val usersUrl   = s"$baseUrl/api/users"
    val userEntity = HttpEntity(ContentTypes.`application/json`, user.toJson.compactPrint)

    val request = HttpRequest(
      method = HttpMethods.POST,
      uri = usersUrl,
      entity = userEntity,
    )

    sendRequestAndParseInto[UnitResponse](request)(BadRequest())
  }

  def getAdmin(): ApiErrorOr[AdminApiResponse] = {
    val adminUrl = s"$baseUrl/api/admin"

    val request: HttpRequest = HttpRequest(
      method = HttpMethods.GET,
      uri = adminUrl,
      headers = Seq(RawHeader("ApiKey", "secret")),
    )

    sendRequestAndParseInto[AdminApiResponse](request)(Unauthorized())
  }

  def checkScenarioState(): ApiErrorOr[ScenarioStateResponse] = {
    val scenarioStatusUrl = s"$baseUrl/api/scenario"

    val request: HttpRequest = HttpRequest(
      method = HttpMethods.GET,
      uri = scenarioStatusUrl,
    )

    sendRequestAndParseInto[ScenarioStateResponse](request)(BadRequest())
  }

  private def sendRequestAndParseInto[T](request: HttpRequest)(error: => ApiError)(implicit ev: RootJsonFormat[T]): ApiErrorOr[T] = {
    for {
      resp <- sendRequest(request)
      data <- parseResponse[T](resp)(error)
    } yield data
  }

  private def sendRequest(request: HttpRequest): Future[HttpResponse] = {
    Http().singleRequest(request)
  }

  private def parseResponse[T](response: HttpResponse)(error: => ApiError)(implicit ev: RootJsonFormat[T]): ApiErrorOr[T] = {
    import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._

    if (response.status == StatusCodes.OK) {
      Unmarshal(response).to[T].map(Right(_))
    } else {
      Future.successful(Left(error))
    }
  }

}

object ApiCallerService {
  case class User(name: String, username: String, id: Long = 0)

  case class AdminApiResponse(message: String)
  case class UnitResponse()
  case class ScenarioStateResponse(status: String)

  implicit val userJsonFormatter: RootJsonFormat[User]                                = jsonFormat3(User)
  implicit val adminResponseFormatter: RootJsonFormat[AdminApiResponse]               = jsonFormat1(AdminApiResponse)
  implicit val scenarioStatusResponseFormatter: RootJsonFormat[ScenarioStateResponse] = jsonFormat1(ScenarioStateResponse)
  implicit val unitResponseFormatter: RootJsonFormat[UnitResponse]                    = jsonFormat0(UnitResponse.apply)

  type ApiErrorOr[T] = Future[Either[ApiError, T]]

  sealed abstract class ApiError(val message: String)
  case class BadRequest()   extends ApiError("Malformed request content!")
  case class Unauthorized() extends ApiError("Unsuccessful authentication!")
}
