package com.thenewmotion.ocpi
package tokens

import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.client.RequestBuilding._
import akka.http.scaladsl.model.{HttpRequest, Uri}
import akka.stream.ActorMaterializer
import com.thenewmotion.ocpi.msgs.Ownership.Ours
import common.{ClientObjectUri, OcpiClient}
import msgs.{AuthToken, SuccessResp, SuccessWithDataResp}
import msgs.v2_1.Tokens.{Token, TokenPatch}
import cats.syntax.either._
import scala.concurrent.{ExecutionContext, Future}

class CpoTokensClient(implicit http: HttpExt) extends OcpiClient {
  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  import com.thenewmotion.ocpi.msgs.v2_1.OcpiJsonProtocol._

  def getToken(tokenUri: ClientObjectUri, authToken: AuthToken[Ours], tokenUid: String)
    (implicit ec: ExecutionContext, mat: ActorMaterializer): Future[ErrorRespOr[Token]] =
    singleRequest[SuccessWithDataResp[Token]](Get(tokenUri.value), authToken).map {
      _.bimap(err => {
        logger.error(s"Could not retrieve token from ${tokenUri.value}. Reason: $err")
        err
      }, _.data)
    }

  private def push(tokenUri: ClientObjectUri, authToken: AuthToken[Ours], rb: Uri => HttpRequest)
    (implicit ec: ExecutionContext, mat: ActorMaterializer): Future[ErrorRespOr[Unit]] =
    singleRequest[SuccessResp](rb(tokenUri.value), authToken).map {
      _.bimap(err => {
        logger.error(s"Could not upload token to ${tokenUri.value}. Reason: $err")
        err
      }, _ => ())
    }

  def uploadToken(tokenUri: ClientObjectUri, authToken: AuthToken[Ours], token: Token)
    (implicit ec: ExecutionContext, mat: ActorMaterializer): Future[ErrorRespOr[Unit]] =
    push(tokenUri, authToken, uri => Put(uri, token))


  def updateToken(tokenUri: ClientObjectUri, authToken: AuthToken[Ours], patch: TokenPatch)
    (implicit ec: ExecutionContext, mat: ActorMaterializer): Future[ErrorRespOr[Unit]] =
    push(tokenUri, authToken, uri => Patch(uri, patch))

}

