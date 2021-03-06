package hmda.persistence.serialization.submission

import hmda.model.fi.{ Created, Failed, Parsed, ParsedWithErrors, Parsing, Signed, Submission, SubmissionId, SubmissionStatus, Uploaded, Uploading, Validated, ValidatedWithErrors, Validating }
import hmda.persistence.messages.events.institutions.SubmissionEvents._
import hmda.persistence.model.serialization.SubmissionEvents._

object SubmissionProtobufConverter {

  def submissionCreatedToProtobuf(obj: SubmissionCreated): SubmissionCreatedMessage = {
    SubmissionCreatedMessage(
      submission = Some(submissionToProtobuf(obj.submission))
    )
  }

  def submissionCreatedFromProtobuf(msg: SubmissionCreatedMessage): SubmissionCreated = {
    SubmissionCreated(
      submission = submissionFromProtobuf(msg.submission.getOrElse(SubmissionMessage()))
    )
  }

  def submissionStatusUpdatedToProtobuf(obj: SubmissionStatusUpdated): SubmissionStatusUpdatedMessage = {
    SubmissionStatusUpdatedMessage(
      id = Some(submissionIdToProtobuf(obj.id)),
      status = Some(submissionStatusToProtobuf(obj.status))
    )
  }

  def submissionStatusUpdatedFromProtobuf(msg: SubmissionStatusUpdatedMessage): SubmissionStatusUpdated = {
    SubmissionStatusUpdated(
      id = submissionIdFromProtobuf(msg.id.getOrElse(SubmissionIdMessage())),
      status = submissionStatusFromProtobuf(msg.status.getOrElse(SubmissionStatusMessage()))
    )
  }

  def submissionStatusUpdatedV2ToProtobuf(obj: SubmissionStatusUpdatedV2): SubmissionStatusUpdatedV2Message = {
    SubmissionStatusUpdatedV2Message(
      id = Some(submissionIdToProtobuf(obj.id)),
      status = Some(submissionStatusToProtobuf(obj.status)),
      time = obj.time
    )
  }

  def submissionStatusUpdatedV2FromProtobuf(msg: SubmissionStatusUpdatedV2Message): SubmissionStatusUpdatedV2 = {
    SubmissionStatusUpdatedV2(
      id = submissionIdFromProtobuf(msg.id.getOrElse(SubmissionIdMessage())),
      status = submissionStatusFromProtobuf(msg.status.getOrElse(SubmissionStatusMessage())),
      time = msg.time
    )
  }

  def submissionFileNameAddedToProtobuf(obj: SubmissionFileNameAdded): SubmissionFileNameAddedMessage = {
    SubmissionFileNameAddedMessage(
      id = Some(submissionIdToProtobuf(obj.id)),
      fileName = obj.fileName
    )
  }

  def submissionFileNameAddedFromProtobuf(msg: SubmissionFileNameAddedMessage): SubmissionFileNameAdded = {
    SubmissionFileNameAdded(
      id = submissionIdFromProtobuf(msg.id.getOrElse(SubmissionIdMessage())),
      fileName = msg.fileName
    )
  }

  def submissionToProtobuf(obj: Submission): SubmissionMessage = {
    SubmissionMessage(
      id = Some(submissionIdToProtobuf(obj.id)),
      status = Some(submissionStatusToProtobuf(obj.status)),
      start = obj.start,
      end = obj.end,
      receipt = obj.receipt,
      fileName = obj.fileName
    )
  }

  def submissionFromProtobuf(msg: SubmissionMessage): Submission = {
    Submission(
      id = submissionIdFromProtobuf(msg.id.getOrElse(SubmissionIdMessage())),
      status = submissionStatusFromProtobuf(msg.status.getOrElse(SubmissionStatusMessage())),
      start = msg.start,
      end = msg.end,
      receipt = msg.receipt,
      fileName = msg.fileName
    )
  }

  def submissionIdToProtobuf(obj: SubmissionId): SubmissionIdMessage = {
    SubmissionIdMessage(
      institutionId = obj.institutionId,
      period = obj.period,
      sequenceNumber = obj.sequenceNumber
    )
  }

  def submissionIdFromProtobuf(msg: SubmissionIdMessage): SubmissionId = {
    SubmissionId(
      institutionId = msg.institutionId,
      period = msg.period,
      sequenceNumber = msg.sequenceNumber
    )
  }

  def submissionStatusToProtobuf(obj: SubmissionStatus): SubmissionStatusMessage = {
    SubmissionStatusMessage(
      code = obj.code,
      message = obj.message,
      description = obj.description
    )
  }

  def submissionStatusFromProtobuf(msg: SubmissionStatusMessage): SubmissionStatus = {
    msg.code match {
      case 1 => Created
      case 2 => Uploading
      case 3 => Uploaded
      case 4 => Parsing
      case 5 => ParsedWithErrors
      case 6 => Parsed
      case 7 => Validating
      case 8 => ValidatedWithErrors
      case 9 => Validated
      case 10 => Signed
      case -1 => Failed(msg.message)
    }
  }

}
