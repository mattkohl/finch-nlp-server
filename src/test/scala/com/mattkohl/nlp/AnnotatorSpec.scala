package com.mattkohl.nlp

import java.util.{Properties, UUID}

import edu.stanford.nlp.pipeline.{Annotation, StanfordCoreNLP}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FlatSpec, Matchers, TryValues, EitherValues, OptionValues}
import org.scalatest.prop.Checkers


@RunWith(classOf[JUnitRunner])
class AnnotatorSpec extends FlatSpec with Matchers with Checkers with OptionValues with TryValues with EitherValues {
  import Annotator._

  behavior of "getAnnotation"

  val text = "I'm just an MC hustlin'"

  it should "create an Annotation of a given nonEmpty String" in {
    val result = getAnnotation(text)
    val expected = createAnnotation(text)
    result.success.value shouldBe expected
  }

  it should "succeed when given an empty String" in {
    val result = getAnnotation("")
    val expected = createAnnotation("")
    result.success.value shouldBe expected
  }

  it should "fail when given a null" in {
    val result = getAnnotation(null)
    result.failure.exception shouldBe a[java.lang.NullPointerException]
  }

  behavior of "annotate"

  it should "produce a clone job with Some List of parseTrees" in {
    val job = createJob(text)
    val result = Annotator.annotate(job)
    val expectedTree = Some(List("(ROOT (S (NP (PRP I)) (VP (VBP 'm) (ADVP (RB just)) (NP (DT an) (NNP MC) (NN hustlin) ('' ')))))"))
    result.right.get.parseTrees shouldBe expectedTree
  }

  it should "update the clone job, but status should still be None" in {
    val job = createJob(text)
    val result = Annotator.annotate(job)
    result.right.get.tokens shouldBe Some(_: List[Token])
    result.right.get.status shouldBe None
  }

  it should "produce an error message when given a Job(text=null)" in {
    val job = createJob(null)
    val result = Annotator.annotate(job)
    result.left.get shouldBe "Annotation failed: java.lang.NullPointerException"
  }

  private def createAnnotation(text: String): Annotation = {
    val props = new Properties()
    props.setProperty("annotators", "tokenize, ssplit, parse")
    val pipeline: StanfordCoreNLP = new StanfordCoreNLP(props)
    pipeline.process(text)
  }

  private def createJob(text: String): Job = Job(UUID.randomUUID(), text, None, None, None)
}
