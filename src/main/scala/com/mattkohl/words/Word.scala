package com.mattkohl.words

import scala.collection.mutable

case class Word(form: String)

object Word {
  private[this] val db: mutable.Map[String, Word] = mutable.Map.empty[String, Word]

  def get(form: String): Option[Word] = synchronized { db.get(form) }
  def list(): List[Word] = synchronized { db.values.toList }
  def save(t: Word): Unit = synchronized { db += (t.form -> t) }
  def delete(form: String): Unit = synchronized { db -= form }
}

case class WordNotFound(form: String) extends Exception {
  override def getMessage: String = s"Word($form) not found."
}
