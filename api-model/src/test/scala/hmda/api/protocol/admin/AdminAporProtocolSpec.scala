package hmda.api.protocol.admin

import hmda.api.model.admin.AdminAporRequests.{ CreateAporRequest, ModifyAporRequest }
import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatest.prop.PropertyChecks
import hmda.model.apor.APORGenerator._
import spray.json._
import hmda.api.protocol.admin.AdminAporProtocol._

class AdminAporProtocolSpec extends PropSpec with PropertyChecks with MustMatchers {

  property("Create APOR Request must convert to and from json") {
    forAll(rateTypeGen, APORGen) { (rateType, apor) =>
      val request = CreateAporRequest(apor, rateType)
      request.toJson.convertTo[CreateAporRequest] mustBe request
    }
  }

  property("Modify APOR Request must convert to and from json") {
    forAll(rateTypeGen, APORGen) { (rateType, apor) =>
      val request = ModifyAporRequest(apor, rateType)
      request.toJson.convertTo[ModifyAporRequest] mustBe request
    }
  }
}
