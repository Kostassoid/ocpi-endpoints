package com.thenewmotion.ocpi
package common

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.ExceptionHandler
import akka.http.scaladsl.server.directives.BasicDirectives
import msgs.ErrorResp
import msgs.OcpiStatusCode._

object OcpiExceptionHandler extends BasicDirectives with SprayJsonSupport {

  protected val logger = Logger(getClass)

  import com.thenewmotion.ocpi.msgs.v2_1.OcpiJsonProtocol._

  val Default = ExceptionHandler {
    case exception => extractRequest { request =>
      logger.error(s"An error occurred processing: $request", exception)
      complete {
        ( OK,
          ErrorResp(
            GenericServerFailure,
            Some(exception.toString)))
      }
    }
  }
}
