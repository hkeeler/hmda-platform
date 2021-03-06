package hmda.publication.reports.disclosure

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import hmda.model.fi.lar.{ LarGenerators, LoanApplicationRegister }
import hmda.model.institution.ExternalIdType.RssdId
import hmda.model.institution.{ ExternalId, Institution, Respondent }
import org.scalacheck.Gen
import org.scalatest.{ AsyncWordSpec, BeforeAndAfterAll, MustMatchers }
import spray.json._

class D4XSpec extends AsyncWordSpec with MustMatchers
    with LarGenerators with BeforeAndAfterAll {

  implicit val system = ActorSystem()
  implicit val ec = system.dispatcher
  implicit val materializer = ActorMaterializer()

  override def afterAll(): Unit = {
    super.afterAll()
    system.terminate()
  }

  def propType = Gen.oneOf(1, 2).sample.get

  val respId = "10101"
  val fips = 24300 // Grand Junction, CO
  val resp = Respondent(ExternalId(respId, RssdId), "Grand Junction Mortgage Co.", "", "", "")
  val inst = Institution.empty.copy(respondent = resp)
  val lars = lar100ListGen.sample.get.map { lar: LoanApplicationRegister =>
    val geo = lar.geography.copy(msa = fips.toString)
    val loan = lar.loan.copy(loanType = 1, propertyType = propType, purpose = 1)
    lar.copy(respondentId = respId, geography = geo, loan = loan)
  }

  val source: Source[LoanApplicationRegister, NotUsed] = Source
    .fromIterator(() => lars.toIterator)

  val description = "Disposition of applications for FHA, FSA/RHS, and VA home-purchase loans, 1- to 4- family and manufactured home dwellings, by race, ethnicity, gender and income of applicant"

  "Generate a Disclosure 4-1 report" in {
    D41.generate(source, fips, inst).map { result =>
      result.report.parseJson.asJsObject.getFields("table", "description", "msa", "respondentId", "institutionName") match {
        case Seq(JsString(table), JsString(desc), msa, JsString(resp), JsString(instName)) =>
          table mustBe "4-1"
          desc mustBe description
          resp mustBe "10101"
          instName mustBe "Grand Junction Mortgage Co."
          msa.asJsObject.getFields("name") match {
            case Seq(JsString(msaName)) => msaName mustBe "Grand Junction, CO"
          }
      }
    }
  }

  "Include correct demographics for dispositions" in {
    D42.generate(source, fips, inst).map { result =>
      result.report.parseJson.asJsObject.getFields("races", "minorityStatuses", "ethnicities", "incomes", "total") match {
        case Seq(JsArray(races), JsArray(ms), JsArray(ethnicities), JsArray(incomes), JsArray(total)) =>
          races must have size 8
          ms must have size 2
          ethnicities must have size 4
          incomes must have size 6
          total must have size 6
          races.head.asJsObject.getFields("race", "dispositions", "genders") match {
            case Seq(JsString(name), JsArray(disp), JsArray(genders)) =>
              name mustBe "American Indian/Alaska Native"
              disp must have size 6
              genders must have size 3
          }
      }
    }
  }

}
