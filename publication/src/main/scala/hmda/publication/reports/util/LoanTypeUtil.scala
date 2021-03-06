package hmda.publication.reports.util

import akka.NotUsed
import akka.stream.scaladsl.Source
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.publication.reports._
import hmda.util.SourceUtils

import scala.concurrent.Future

object LoanTypeUtil extends SourceUtils {
  def loanTypes[ec: EC, mat: MAT, as: AS](larSource: Source[LoanApplicationRegister, NotUsed]): Future[String] = {
    for {
      conv <- purposesOutput(larSource.filter(lar => lar.loan.loanType == 1))
      fha <- purposesOutput(larSource.filter(lar => lar.loan.loanType == 2))
      va <- purposesOutput(larSource.filter(lar => lar.loan.loanType == 3))
      fsa <- purposesOutput(larSource.filter(lar => lar.loan.loanType == 4))
    } yield {
      s"""
         |[
         |  {
         |    "loanType": "Conventional",
         |    "purposes": $conv
         |  },
         |  {
         |    "loanType": "FHA",
         |    "purposes": $fha
         |  },
         |  {
         |    "loanType": "VA",
         |    "purposes": $va
         |  },
         |  {
         |    "loanType": "FSA/RHS",
         |    "purposes": $fsa
         |  }
         |]
     """.stripMargin
    }
  }

  private def purposesOutput[ec: EC, mat: MAT, as: AS](larSource: Source[LoanApplicationRegister, NotUsed]): Future[String] = {
    for {
      homePurchaseFirst <- count(larSource.filter(lar => lar.lienStatus == 1 && lar.loan.purpose == 1))
      homePurchaseJunior <- count(larSource.filter(lar => lar.lienStatus == 2 && lar.loan.purpose == 1))
      refinanceFirst <- count(larSource.filter(lar => lar.lienStatus == 1 && lar.loan.purpose == 3))
      refinanceJunior <- count(larSource.filter(lar => lar.lienStatus == 2 && lar.loan.purpose == 3))
      homeImprovementFirst <- count(larSource.filter(lar => lar.lienStatus == 1 && lar.loan.purpose == 2))
      homeImprovementJunior <- count(larSource.filter(lar => lar.lienStatus == 2 && lar.loan.purpose == 2))
      homeImprovementNo <- count(larSource.filter(lar => lar.lienStatus != 1 && lar.lienStatus != 2 && lar.loan.purpose == 2))
    } yield {
      s"""
         |[
         |  {
         |    "purpose": "Home Purchase",
         |    "firstLienCount": $homePurchaseFirst,
         |    "juniorLienCount": $homePurchaseJunior
         |  },
         |  {
         |    "purpose": "Refinance",
         |    "firstLienCount": $refinanceFirst,
         |    "juniorLienCount": $refinanceJunior
         |  },
         |  {
         |    "purpose": "Home Improvement",
         |    "firstLienCount": $homeImprovementFirst,
         |    "juniorLienCount": $homeImprovementJunior,
         |    "noLienCount": $homeImprovementNo
         |  }
         |]
     """.stripMargin
    }
  }
}
