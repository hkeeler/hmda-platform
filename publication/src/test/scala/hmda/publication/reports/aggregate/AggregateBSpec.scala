package hmda.publication.reports.aggregate

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import hmda.model.fi.lar.{ LarGenerators, LoanApplicationRegister }
import org.scalatest.{ AsyncWordSpec, BeforeAndAfterAll, MustMatchers }
import spray.json._

class AggregateBSpec extends AsyncWordSpec with MustMatchers
    with LarGenerators with BeforeAndAfterAll {

  implicit val system = ActorSystem()
  implicit val ec = system.dispatcher
  implicit val materializer = ActorMaterializer()

  override def afterAll(): Unit = {
    super.afterAll()
    system.terminate()
  }

  val respId = "65656"
  val fips = 24300 // Grand Junction, CO
  val lars = lar100ListGen.sample.get.map { lar: LoanApplicationRegister =>
    val geo = lar.geography.copy(msa = fips.toString)
    val loan = lar.loan.copy(loanType = 1, occupancy = 1)
    lar.copy(respondentId = respId, geography = geo, loan = loan)
  }

  val source: Source[LoanApplicationRegister, NotUsed] = Source
    .fromIterator(() => lars.toIterator)

  val description = "Loan pricing information for conventional loans by incidence and level"

  "Generate an Aggregate B report" in {
    AggregateB.generate(source, fips).map { result =>
      result.report.parseJson.asJsObject.getFields("table", "description", "msa") match {
        case Seq(JsString(table), JsString(desc), msa) =>
          table mustBe "B"
          desc mustBe description
          msa.asJsObject.getFields("name") match {
            case Seq(JsString(msaName)) => msaName mustBe "Grand Junction, CO"
          }
      }
    }
  }

  "Have correct JSON structure" in {
    AggregateB.generate(source, fips).map { result =>
      result.report.parseJson.asJsObject.getFields("singleFamily", "manufactured") match {
        case Seq(JsArray(singleFamily), JsArray(manufactured)) =>
          singleFamily must have size 2
          manufactured must have size 2

          singleFamily.head.asJsObject.getFields("characteristic", "pricingInformation") match {
            case Seq(JsString(char), JsArray(disp)) =>
              char mustBe "Incidence of Pricing"
              disp must have size 4

              disp.head.asJsObject.getFields("pricing", "purposes") match {
                case Seq(JsString(name), JsArray(cat)) =>
                  name mustBe "No pricing reported"
                  cat must have size 3

                  cat.head.asJsObject.getFields("purpose", "firstLienCount", "juniorLienCount", "noLienCount") match {
                    case Seq(JsString(purpose), JsNumber(first), JsNumber(junior), JsNumber(noLien)) =>
                      purpose mustBe "Home Purchase"
                  }
              }
          }
      }
    }
  }

  "Generate a National Aggregate B report" in {
    NationalAggregateB.generate(source, fips).map { result =>
      result.report.parseJson.asJsObject.getFields("table", "description") match {
        case Seq(JsString(table), JsString(desc)) =>
          table mustBe "B"
          desc mustBe description
      }
    }
  }

}
