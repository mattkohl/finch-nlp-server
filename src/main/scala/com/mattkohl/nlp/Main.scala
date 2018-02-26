package com.mattkohl.nlp


import java.util.UUID

import com.twitter.app.Flag
import com.twitter.finagle.{Http, Service}
import com.twitter.finagle.http.{Request, Response}
import com.twitter.server.TwitterServer
import com.twitter.util.Await
import io.circe.generic.auto._
import io.finch._
import io.finch.circe._
import io.finch.syntax._

/**
  * {{{
  *   $ http POST :8081/sentences text="This is a dog."
  *   $ http :8081/sentences
  *   $ http DELETE :8081/sentences/<UUID>
  *   $ http DELETE :8081/sentences
  * }}}
  */
object Main extends TwitterServer {

  def postedJob: Endpoint[Job] = jsonBody[UUID => Job] map {
    in => in(UUID.randomUUID())
  }

  def postJob: Endpoint[Job] = post("jobs" :: postedJob) { raw: Job =>
    Annotator.annotate(raw) match {
      case Right(annotated) =>
        val succeeded = annotated.copy(status=Some("SUCCESS"))
        Job.save(succeeded)
        Created(succeeded)
      case Left(message) =>
        val failed = raw.copy(status=Some(s"FAILURE: $message"))
        Job.save(failed)
        Created(failed)
    }
  }

  def getJobs: Endpoint[List[Job]] = get("jobs") {
    Ok(Job.list())
  }

  def deleteJob: Endpoint[Job] = delete("jobs" :: path[UUID]) { id: UUID =>
    Job.get(id) match {
      case Some(t) =>
        Job.delete(id)
        Ok(t)
      case None => throw JobNotFound(id)
    }
  }

  def deleteJobs: Endpoint[List[Job]] = delete("jobs") {
    val all: List[Job] = Job.list()
    all.foreach(t => Job.delete(t.id))
    Ok(all)
  }

  val api: Service[Request, Response] = (
    getJobs :+: postJob :+: deleteJob :+: deleteJobs
    ).handle({
    case e: JobNotFound => NotFound(e)
  }).toServiceAs[Application.Json]

  def main(): Unit = {
    val port: Flag[Int] = flag("port", 8081, "TCP port for HTTP server")

    val server = Http.server serve (s"0.0.0.0:${port()}", api)

    onExit { server.close() }

    Await.ready(adminHttpServer)
  }
}
