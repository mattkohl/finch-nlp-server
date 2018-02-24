package com.mattkohl.nlp

import java.util.UUID
import scala.collection.mutable

case class Sentence(id: UUID, text: String, tokens: Option[List[Token]])

object Sentence {

  private[this] val db: mutable.Map[UUID, Sentence] = mutable.Map.empty[UUID, Sentence]

  def get(id: UUID): Option[Sentence] = synchronized { db.get(id) }
  def list(): List[Sentence] = synchronized { db.values.toList }
  def save(t: Sentence): Unit = synchronized { db += (t.id -> t) }
  def delete(id: UUID): Unit = synchronized { db -= id }

}

case class SentenceNotFound(id: UUID) extends Exception {
  override def getMessage: String = s"Sentence(${id.toString}) not found."
}


