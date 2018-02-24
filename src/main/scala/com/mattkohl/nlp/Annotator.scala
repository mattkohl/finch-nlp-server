package com.mattkohl.nlp

import java.util.Properties

import edu.stanford.nlp.ling.CoreAnnotations.{PartOfSpeechAnnotation, TextAnnotation, TokensAnnotation}
import edu.stanford.nlp.pipeline.{Annotation, StanfordCoreNLP}
import scala.collection.JavaConverters._

object Annotator {
  private val props = new Properties()
  props.setProperty("annotators", "tokenize, ssplit, parse")
  private val pipeline: StanfordCoreNLP = new StanfordCoreNLP(props)

  def annotate(text: String): List[(String, String)] = {
    val annotation: Annotation = pipeline.process(text)
    val tokens = annotation.get(classOf[TokensAnnotation]).asScala.toList
    tokens.map { token =>
      val word = token.get(classOf[TextAnnotation])
      val pos = token.get(classOf[PartOfSpeechAnnotation])
      (word, pos)
    }
  }
}
