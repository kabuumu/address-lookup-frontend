package pages

import controllers.routes
import itutil.IntegrationSpecBase
import itutil.config.IntegrationTestConstants._
import itutil.config.PageElementConstants.LookupPage
import play.api.http.HeaderNames
import play.api.http.Status.{OK, BAD_REQUEST}
import play.api.libs.json.Json
import play.api.test.FakeApplication
import model.JourneyConfigDefaults._

import scala.util.Random

class LookupPageSpec extends IntegrationSpecBase {

  override implicit lazy val app = FakeApplication(additionalConfiguration = fakeConfig())

  def longFilterValue = (1 to 257) map (_ => Random.alphanumeric.head) mkString

  // TODO: Make hint configurable as part of welsh translation
  val hardCodedFormHint = " For example, The Mill, 116 or Flat 37a"

  "The lookup page" when {
    "when provided with no page config" should {
      "Render the default content" in {
        stubKeystore(testJourneyId, testConfigDefaultAsJson, OK)
        stubKeystoreSave(testJourneyId, testConfigWithoutAddressAsJson, OK)

        val fResponse = buildClientLookupAddress(path = s"lookup?postcode=$testPostCode&filter=$testFilterValue")
          .withHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
          .get()
        val res = await(fResponse)
        val doc = getDocFromResponse(res)

        res.status shouldBe OK

        doc.title shouldBe LOOKUP_PAGE_TITLE
        doc.h1.text() shouldBe LOOKUP_PAGE_HEADING

        doc.input(LookupPage.postcodeId) should have(
          label(LOOKUP_PAGE_POSTCODE_LABEL),
          value(testPostCode)
        )

        doc.input(LookupPage.filterId) should have(
          label(LOOKUP_PAGE_FILTER_LABEL + hardCodedFormHint),
          value(testFilterValue)
        )

        doc.link(LookupPage.manualAddressLink) should have(
          href(routes.AddressLookupController.edit(testJourneyId).url),
          text(LOOKUP_PAGE_MANUAL_ADDRESS_LINK_TEXT)
        )

        doc.submitButton.text() shouldBe "Search for the address"
      }

      "show the default 'postcode not entered' error message" in {
        stubKeystore(testJourneyId, testConfigDefaultAsJson, OK)
        stubKeystoreSave(testJourneyId, testConfigWithoutAddressAsJson, OK)

        val fResponse = buildClientLookupAddress(path = "select")
          .withHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
          .get()
        val res = await(fResponse)
        val doc = getDocFromResponse(res)

        res.status shouldBe BAD_REQUEST

        val message = "This field is required"

        doc.errorSummary should have(
          errorSummaryMessage(LookupPage.postcodeId, message)
        )

        doc.input(LookupPage.postcodeId) should have(
          errorMessage(message),
          value("")
        )
      }

      "show the default 'invalid postcode' error message" in {
        stubKeystore(testJourneyId, testConfigDefaultAsJson, OK)
        stubKeystoreSave(testJourneyId, testConfigWithoutAddressAsJson, OK)

        val fResponse = buildClientLookupAddress(path = s"select?postcode=QQ")
          .withHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
          .get()
        val res = await(fResponse)
        val doc = getDocFromResponse(res)

        res.status shouldBe BAD_REQUEST

        val message = "The postcode you entered appears to be incomplete or invalid. Please check and try again."

        doc.errorSummary should have (
          errorSummaryMessage(LookupPage.postcodeId, message)
        )

        doc.input(LookupPage.postcodeId) should have(
          errorMessage(message),
          value("QQ")
        )
      }

      "show the default 'filter invalid' error messages" in {
        stubKeystore(testJourneyId, testConfigDefaultAsJson, OK)
        stubKeystoreSave(testJourneyId, testConfigWithoutAddressAsJson, OK)

        val filterValue = longFilterValue
        val fResponse = buildClientLookupAddress(path = s"select?filter=$filterValue")
          .withHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
          .get()
        val res = await(fResponse)
        val doc = getDocFromResponse(res)

        res.status shouldBe BAD_REQUEST

        val message = "Your house name/number needs to be fewer than 256 characters"

        doc.errorSummary should have (
          errorSummaryMessage(LookupPage.filterId, message)
        )

        doc.input(LookupPage.filterId) should have(
          errorMessage(message),
          value(filterValue)
        )
      }

      "display the back button" in {
        stubKeystore(testJourneyId, testConfigDefaultAsJson, OK)
        stubKeystoreSave(testJourneyId, testConfigDefaultAsJson, OK)

        val fResponse = buildClientLookupAddress(path = s"lookup?postcode=$testPostCode&filter=$testFilterValue")
          .withHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
          .get()
        val res = await(fResponse)
        val doc = getDocFromResponse(res)

        res.status shouldBe OK

        doc.select("a[class=back-link]") should have(
          text("Back")
        )
      }
    }

    "provided with custom content" should {
      "render the page with custom content" in {
        stubKeystore(testJourneyId, testLookupConfig, OK)
        stubKeystoreSave(testJourneyId, testLookupConfig, OK)

        val fResponse = buildClientLookupAddress(path = s"lookup?postcode=$testPostCode&filter=$testFilterValue")
          .withHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
          .get()
        val res = await(fResponse)
        val doc = getDocFromResponse(res)

        res.status shouldBe OK

        doc.title shouldBe fullLookupPageConfig.title.get
        doc.h1.text() shouldBe fullLookupPageConfig.heading.get

        doc.input(LookupPage.postcodeId) should have(
          label(fullLookupPageConfig.postcodeLabel.get),
          value(testPostCode)
        )

        doc.input(LookupPage.filterId) should have(
          label(fullLookupPageConfig.filterLabel.get + hardCodedFormHint),
          value(testFilterValue)
        )

        doc.link(LookupPage.manualAddressLink) should have(
          href(routes.AddressLookupController.edit(testJourneyId).url),
          text(fullLookupPageConfig.manualAddressLinkText.get)
        )

        doc.submitButton.text() shouldBe fullLookupPageConfig.submitLabel.get
      }

      "not display the back button if disabled" in {
        stubKeystore(testJourneyId, testLookupConfigNoBackButtons, OK)
        stubKeystoreSave(testJourneyId, testLookupConfigNoBackButtons, OK)

        val fResponse = buildClientLookupAddress(path = s"lookup?postcode=$testPostCode&filter=$testFilterValue")
          .withHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
          .get()
        val res = await(fResponse)
        val doc = getDocFromResponse(res)

        res.status shouldBe OK

        doc.select("a[class=back-link]") should not have(
          text("Back")
        )
      }
    }
  }
}