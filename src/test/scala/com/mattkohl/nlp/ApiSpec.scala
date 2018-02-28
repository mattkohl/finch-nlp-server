package com.mattkohl.nlp


import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import com.twitter.finagle.http.Status
import io.circe.generic.auto._
import io.finch._
import io.finch.circe._
import java.nio.charset.StandardCharsets
import java.util.UUID
import org.scalacheck.{Arbitrary, Gen}
import org.scalatest.{FlatSpec, Matchers}
import org.scalatest.prop.Checkers



@RunWith(classOf[JUnitRunner])
class ApiSpec extends FlatSpec with Matchers with Checkers {
  import Main._

  behavior of "the postJob endpoint"

  case class JobWithoutId(text: String, tokens: Option[List[Token]], parseTrees: Option[List[String]], status: Option[String])

  def genToken: Gen[Token] = for {
    token <- Gen.alphaStr
    partOfSpeech <- Gen.alphaStr
  } yield Token(token, partOfSpeech)

  implicit def arbitraryToken: Arbitrary[Token] = Arbitrary(genToken)

  def genJobWithoutId: Gen[JobWithoutId] = for {
    text <- Gen.alphaStr
    tokens <- Gen.option(Gen.listOf(genToken))
    parseTrees <- Gen.option(Gen.listOf(Gen.alphaStr))
    status <- Gen.option(Gen.alphaStr)
  } yield JobWithoutId(text, tokens, parseTrees, status)

  implicit def arbitraryJobWithoutId: Arbitrary[JobWithoutId] = Arbitrary(genJobWithoutId)

  it should "create a Job" in {
    check { (jobWithoutId: JobWithoutId) =>
      val input = Input.post("/jobs")
        .withBody[Application.Json](jobWithoutId, Some(StandardCharsets.UTF_8))

      val res = postJob(input)
      res.awaitOutputUnsafe().map(_.status) === Some(Status.Created)
      res.awaitValueUnsafe().isDefined === true
      val Some(job) = res.awaitValueUnsafe()
      job.text === jobWithoutId.text
      job.tokens === jobWithoutId.tokens
      job.parseTrees === jobWithoutId.parseTrees
      job.status === jobWithoutId.status
      Job.get(job.id).isDefined === true
    }
  }

  behavior of "the getJobs endpoint"

  it should "retrieve all available Jobs" in {
    getJobs(Input.get("/jobs")).awaitValueUnsafe() shouldBe Some(Job.list())
  }

  behavior of "the deleteJob endpoint"

  it should "delete the specified job" in {
    val job = createJob()

    deleteJob(Input.delete(s"/jobs/${job.id}")).awaitValueUnsafe() shouldBe Some(job)
    Job.get(job.id) shouldBe None
  }

  it should "throw an exception if the UUID hasn't been found" in {
    val id = UUID.randomUUID()
    Job.get(id) shouldBe None
    a[JobNotFound] shouldBe thrownBy(deleteJob(Input.delete(s"/jobs/$id")).awaitValueUnsafe())
  }

  behavior of "the deleteJobs endpoint"

  it should "delete all jobs" in {
    val jobs = Job.list()
    deleteJobs(Input.delete("/jobs")).awaitValueUnsafe() shouldBe Some(jobs)
    jobs.foreach(t => Job.get(t.id) shouldBe None)
  }

  private def createJob(): Job = {
    val job = Job(UUID.randomUUID(), "LAPD can't see me, I wear a muslim beanie", None, None, None)
    Job.save(job)
    job
  }  
  

}
