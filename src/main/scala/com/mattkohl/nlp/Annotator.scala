package com.mattkohl.nlp

import java.util.Properties

import edu.stanford.nlp.ling.CoreAnnotations.{PartOfSpeechAnnotation, SentencesAnnotation, TextAnnotation, TokensAnnotation}
import edu.stanford.nlp.pipeline.{Annotation, StanfordCoreNLP}
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation
import edu.stanford.nlp.trees.Tree
import edu.stanford.nlp.util.CoreMap

import scala.collection.JavaConverters._

case class Token(token: String, partOfSpeech: String)

object Annotator {
  private val props = new Properties()
  props.setProperty("annotators", "tokenize, ssplit, parse")
  private val pipeline: StanfordCoreNLP = new StanfordCoreNLP(props)

  def annotate(text: String): Annotation = pipeline.process(text)

  def sentencesAnnotations(annotation: Annotation): List[CoreMap] = annotation.get(classOf[SentencesAnnotation]).asScala.toList

  def parseTree(sentencesAnnotations: List[CoreMap]): List[Tree] = sentencesAnnotations.map(_.get(classOf[TreeAnnotation]))

  def tokenizeAndTag(annotation: Annotation): List[Token] = {
    val tokens = annotation.get(classOf[TokensAnnotation]).asScala.toList
    tokens.map { token =>
      val word = token.get(classOf[TextAnnotation])
      val pos = token.get(classOf[PartOfSpeechAnnotation])
      Token(word, pos)
    }
  }

}
