package com.mattkohl.nlp


import java.util.UUID

import com.twitter.app.Flag
import com.twitter.finagle.{Http, Service}
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.stats.Counter
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

  val port: Flag[Int] = flag("port", 8081, "TCP port for HTTP server")

  val sentences: Counter = statsReceiver.counter("sentences")

  def postedSentence: Endpoint[Sentence] = jsonBody[UUID => Sentence].map{ x =>
    x(UUID.randomUUID())
  }

  def postSentence: Endpoint[Sentence] = post("sentences" :: postedSentence) { t: Sentence =>
    sentences.incr()
    Sentence.save(t)
    Created(t)
  }

  def getSentences: Endpoint[List[Sentence]] = get("sentences") {
    Ok(Sentence.list())
  }

  def deleteSentence: Endpoint[Sentence] = delete("sentences" :: path[UUID]) { id: UUID =>
    Sentence.get(id) match {
      case Some(t) => Sentence.delete(id); Ok(t)
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
    val server = Http.server
      .withStatsReceiver(statsReceiver)
      .serve(s":${port()}", api)

    onExit { server.close() }

    Await.ready(adminHttpServer)
  }
}
