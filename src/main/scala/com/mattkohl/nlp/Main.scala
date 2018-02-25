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
import scala.util.{Success, Failure}

/**
  * {{{
  *   $ http POST :8081/sentences text="This is a dog."
  *   $ http :8081/sentences
  *   $ http DELETE :8081/sentences/<UUID>
  *   $ http DELETE :8081/sentences
  * }}}
  */
object Main extends TwitterServer {

  def postedSentence: Endpoint[Sentence] = jsonBody[UUID => Sentence] map {
    in => in(UUID.randomUUID())
  }

  def postSentence: Endpoint[Sentence] = post("sentences" :: postedSentence) { raw: Sentence =>
    Annotator.annotate(raw) match {
      case Success(annotated) =>
        Sentence.save(annotated)
        Created(annotated)
      case Failure(_) =>
        Sentence.save(raw)
        Created(raw)
    }
  }

  def getSentences: Endpoint[List[Sentence]] = get("sentences") {
    Ok(Sentence.list())
  }

  def deleteSentence: Endpoint[Sentence] = delete("sentences" :: path[UUID]) { id: UUID =>
    Sentence.get(id) match {
      case Some(t) =>
        Sentence.delete(id)
        Ok(t)
      case None => throw SentenceNotFound(id)
    }
  }

  def deleteSentences: Endpoint[List[Sentence]] = delete("sentences") {
    val all: List[Sentence] = Sentence.list()
    all.foreach(t => Sentence.delete(t.id))
    Ok(all)
  }

  val api: Service[Request, Response] = (
    getSentences :+: postSentence :+: deleteSentence :+: deleteSentences
    ).handle({
    case e: SentenceNotFound => NotFound(e)
  }).toServiceAs[Application.Json]

  def main(): Unit = {
    val port: Flag[Int] = flag("port", 8081, "TCP port for HTTP server")

    val server = Http.server serve (s"0.0.0.0:${port()}", api)

    onExit { server.close() }

    Await.ready(adminHttpServer)
  }
}
