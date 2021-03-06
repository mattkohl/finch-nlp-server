package com.mattkohl.nlp

import java.util.UUID
import scala.collection.mutable

case class Job(id: UUID, text: String, tokens: Option[List[Token]], parseTrees: Option[List[String]], status: Option[String])

object Job {

  private[this] val db: mutable.Map[UUID, Job] = mutable.Map.empty[UUID, Job]

  def get(id: UUID): Option[Job] = synchronized { db.get(id) }
  def list(): List[Job] = synchronized { db.values.toList }
  def save(t: Job): Unit = synchronized { db += (t.id -> t) }
  def delete(id: UUID): Unit = synchronized { db -= id }

}

case class JobNotFound(id: UUID) extends Exception


