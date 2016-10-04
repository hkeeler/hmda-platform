package hmda.api.model

import akka.http.scaladsl.model.Uri

case class ErrorResponse(
  httpStatus: Int,
  message: String,
  path: Uri
)
