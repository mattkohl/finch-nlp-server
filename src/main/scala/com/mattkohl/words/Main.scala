package com.mattkohl.words


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

object Main extends TwitterServer {

  val port: Flag[Int] = flag("port", 8081, "TCP port for HTTP server")

  val words: Counter = statsReceiver.counter("words")

  def postWord: Endpoint[Word] = post("words" :: path[String]) { form: String =>
    val w = Word(form)
    words.incr()
    Word.save(w)
    Created(w)
  }

  def getWords: Endpoint[List[Word]] = get("words") {
    Ok(Word.list())
  }

  def deleteWord: Endpoint[Word] = delete("words" :: path[String]) { form: String =>
    Word.get(form) match {
      case Some(w) =>
        Word.delete(form)
        Ok(w)
      case None => throw WordNotFound(form)
    }
  }

  def deleteWords: Endpoint[List[Word]] = delete("words") {
    val all: List[Word] = Word.list()
    all.foreach(w => Word.delete(w.form))

    Ok(all)
  }

  val api: Service[Request, Response] = (
    getWords :: postWord :: deleteWord :: deleteWords
  ).handle({
    case e: WordNotFound => NotFound(e)
  }).toServiceAs[Application.Json]

  def main(): Unit = {
    val server = Http.server.withStatsReceiver(statsReceiver).serve(s":${port()}", api)

    onExit { server.close() }

    Await.ready(adminHttpServer)
  }

}
