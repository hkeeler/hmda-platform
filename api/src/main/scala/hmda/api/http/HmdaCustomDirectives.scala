package hmda.api.http

import akka.http.scaladsl.server._
import akka.http.scaladsl.server.Directives._
import akka.event.LoggingAdapter
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes
import hmda.api.model.ErrorResponse
import hmda.api.protocol.processing.ApiErrorProtocol
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import hmda.api.rejections.EntityNotFoundRejection

trait HmdaCustomDirectives extends ApiErrorProtocol {
  val log: LoggingAdapter

  implicit def exceptionHandler: ExceptionHandler =
    ExceptionHandler {
      case cause: Throwable =>
        extractUri { uri =>
          //FIXME: Merge uri and method
          extractMethod { method =>
            val msg = s"Error occurred while ${method.name} processing request to ${uri.toString()}"
            log.error(cause, msg)
            complete(ErrorResponse(500, "Internal Server Error", uri))
          }
        }
    }

  implicit def authRejectionHandler = {
    RejectionHandler.newBuilder()
      .handle {
        case AuthorizationFailedRejection =>
          (extractUri & extractMethod){ uri, method =>
            val errorResponse = ErrorResponse(403, "Unauthorized Access", uri)
            complete(ToResponseMarshallable(StatusCodes.Forbidden -> errorResponse))
          }
        case EntityNotFoundRejection(message) =>
          val resp = ErrorResponse(404, message, "")
          complete(ToResponseMarshallable(resp.httpStatus -> resp))
      }
      .handleNotFound {
        val errorResponse = ErrorResponse(404, "Not Found", "")
        complete(ToResponseMarshallable(StatusCodes.NotFound -> errorResponse))
      }
      .result()
  }

  def hmdaAuthorize: Directive0 =
    authorize(ctx =>
      hasHeader("CFPB-HMDA-Username", ctx) &&
        hasHeader("CFPB-HMDA-Institutions", ctx))

  private def hasHeader(headerName: String, ctx: RequestContext): Boolean = {
    ctx.request.getHeader(headerName).isPresent
  }

  //FIXME: Replace this with logRequestResult
  def time: Directive0 = {
    val startTime = System.currentTimeMillis()

    mapResponse { response =>
      val endTime = System.currentTimeMillis()
      val responseTime = endTime - startTime
      log.debug(s"Request took $responseTime ms")
      response
    }

  }

}
