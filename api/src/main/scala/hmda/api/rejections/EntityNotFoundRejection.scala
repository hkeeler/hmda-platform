package hmda.api.rejections

import akka.http.scaladsl.server.Rejection

/**
 * Created by keelerh on 8/29/16.
 */
final case class EntityNotFoundRejection(message: String) extends Rejection
