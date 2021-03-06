package hmda.query.repository.filing

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.query.model.filing.ModifiedLoanApplicationRegister

import scala.language.implicitConversions

object LarConverter {

  implicit def toModifiedLar(lar: LoanApplicationRegister): ModifiedLoanApplicationRegister = {
    ModifiedLoanApplicationRegister(
      2,
      lar.respondentId,
      lar.agencyCode,
      lar.preapprovals,
      lar.actionTakenType,
      lar.purchaserType,
      lar.rateSpread,
      lar.hoepaStatus,
      lar.lienStatus,
      lar.loan.loanType,
      lar.loan.propertyType,
      lar.loan.purpose,
      lar.loan.occupancy,
      lar.loan.amount,
      lar.geography.msa,
      lar.geography.state,
      lar.geography.county,
      lar.geography.tract,
      lar.applicant.ethnicity,
      lar.applicant.coEthnicity,
      lar.applicant.race1,
      lar.applicant.race2,
      lar.applicant.race3,
      lar.applicant.race4,
      lar.applicant.race5,
      lar.applicant.coRace1,
      lar.applicant.coRace2,
      lar.applicant.coRace3,
      lar.applicant.coRace4,
      lar.applicant.coRace5,
      lar.applicant.sex,
      lar.applicant.coSex,
      lar.applicant.income,
      lar.denial.reason1,
      lar.denial.reason2,
      lar.denial.reason3,
      lar.actionTakenDate.toString.substring(0, 4)
    )
  }
}
