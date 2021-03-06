package hmda.api.http.admin

import akka.event.{ LoggingAdapter, NoLogging }
import akka.http.scaladsl.model.headers.{ HttpEncodings, `Accept-Encoding` }
import akka.http.scaladsl.model.headers.HttpEncodings._
import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit._
import akka.util.{ ByteString, Timeout }
import hmda.api.model.{ ModelGenerators, Status }
import org.scalatest.{ BeforeAndAfterAll, MustMatchers, WordSpec }
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.server.MalformedRequestContentRejection
import hmda.api.http.BaseHttpApi
import hmda.model.institution.Institution
import hmda.model.institution.InstitutionGenerators._
import hmda.persistence.HmdaSupervisor
import hmda.validation.stats.ValidationStats

import scala.concurrent.duration._
import spray.json._

class InstitutionAdminHttpApiSpec
    extends WordSpec
    with MustMatchers
    with ScalatestRouteTest
    with BaseHttpApi
    with InstitutionAdminHttpApi
    with ModelGenerators
    with BeforeAndAfterAll {

  override implicit val timeout: Timeout = Timeout(10.seconds)
  override val log: LoggingAdapter = NoLogging

  val validationStats = ValidationStats.createValidationStats(system)
  val supervisor = HmdaSupervisor.createSupervisor(system, validationStats)

  override def afterAll(): Unit = {
    super.afterAll()
    system.terminate()
  }

  "Institution Admin Paths" must {

    val newInstitution = sampleInstitution

    "return OK for GET requests to the root path" in {
      Get() ~> routes("hmda-admin-api") ~> check {
        responseAs[Status].status mustBe "OK"
        responseAs[Status].service mustBe "hmda-admin-api"
      }
    }
    "use requested encoding for root path" in {
      Get().addHeader(`Accept-Encoding`(gzip)) ~> routes("hmda-admin-api") ~> check {
        response.encoding mustBe HttpEncodings.gzip
      }
    }

    "create a new institution and get its details" in {
      val jsonRequest = ByteString(newInstitution.toJson.toString)
      val postRequest = createRequest(jsonRequest, HttpMethods.POST)
      postRequest ~> institutionAdminRoutes(supervisor) ~> check {
        status mustBe StatusCodes.Created
        responseAs[Institution] mustBe newInstitution
      }
      Get(s"/institutions/${newInstitution.id}") ~> institutionAdminRoutes(supervisor) ~> check {
        status mustBe StatusCodes.OK
        responseAs[Institution] mustBe newInstitution
      }
    }
    "use requested encoding for institution create/update path" in {
      val jsonRequest = ByteString(sampleInstitution.toJson.toString)
      val postRequest = createRequest(jsonRequest, HttpMethods.POST)
      postRequest.addHeader(`Accept-Encoding`(deflate)) ~> institutionAdminRoutes(supervisor) ~> check {
        response.encoding mustBe HttpEncodings.deflate
      }
    }
    "return error when trying to upload bad entity" in {
      val badJson = "bad payload".toJson
      val jsonRequest = ByteString(badJson.toString)
      val postRequest = createRequest(jsonRequest, HttpMethods.POST)
      postRequest ~> institutionAdminRoutes(supervisor) ~> check {
        rejection mustBe a[MalformedRequestContentRejection]
      }
    }
    "return conflict when trying to upload existing entity" in {
      val jsonRequest = ByteString(newInstitution.toJson.toString)
      val postRequest = createRequest(jsonRequest, HttpMethods.POST)
      postRequest ~> institutionAdminRoutes(supervisor) ~> check {
        status mustBe StatusCodes.Conflict
      }
    }
    "modify existing institution" in {
      val updatedInstitutionRespondent = newInstitution.respondent.copy(name = "new name")
      val updatedInstitution = newInstitution.copy(cra = true, respondent = updatedInstitutionRespondent)
      val jsonRequest = ByteString(updatedInstitution.toJson.toString)
      val putRequest = createRequest(jsonRequest, HttpMethods.PUT)
      putRequest ~> institutionAdminRoutes(supervisor) ~> check {
        status mustBe StatusCodes.Accepted
        responseAs[Institution] mustBe updatedInstitution
      }
    }
    "return not found when modifying non existing institution" in {
      val i1 = sampleInstitution
      val jsonRequest = ByteString(i1.toJson.toString)
      val putRequest = createRequest(jsonRequest, HttpMethods.PUT)
      putRequest ~> institutionAdminRoutes(supervisor) ~> check {
        status mustBe StatusCodes.NotFound
      }
    }

    "delete institution" in {
      val jsonRequest = ByteString(newInstitution.toJson.toString)
      val deleteRequest = createRequest(jsonRequest, HttpMethods.DELETE)
      deleteRequest ~> institutionAdminRoutes(supervisor) ~> check {
        status mustBe StatusCodes.Accepted
        responseAs[String] mustBe newInstitution.id
      }

      Get(s"/institutions/${newInstitution.id}") ~> institutionAdminRoutes(supervisor) ~> check {
        status mustBe StatusCodes.NotFound
      }
    }
  }

  private def createRequest(jsonRequest: ByteString, method: HttpMethod): HttpRequest = {
    HttpRequest(
      method,
      uri = "/institutions",
      entity = HttpEntity(MediaTypes.`application/json`, jsonRequest)
    )
  }
}
