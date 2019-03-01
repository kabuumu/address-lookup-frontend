package services

import javax.inject.Singleton
import com.google.inject.ImplementedBy
import com.typesafe.config.{ConfigObject, ConfigRenderOptions}
import config.AddressLookupFrontendSessionCache
import model._
import play.api.libs.json.Json
import uk.gov.hmrc.http.cache.client.HttpCaching
import uk.gov.hmrc.play.config.ServicesConfig

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}
import uk.gov.hmrc.http.HeaderCarrier

@ImplementedBy(classOf[KeystoreJourneyRepository])
trait JourneyRepository {

  def init(journeyName: String): JourneyData

  def get(id: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[JourneyData]]

  def put(id: String, data: JourneyData)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Boolean]

}

@Singleton
class KeystoreJourneyRepository extends JourneyRepository with ServicesConfig {

  val cacheId = "journey-data"

  private val cfg: Map[String, JourneyData] = config("address-lookup-frontend").getObject("journeys").map { journeys =>
    journeys.keySet().asScala.map { key =>
      (key -> journey(key, journeys))
    }.toMap
  }.getOrElse(Map.empty)

  val cache: HttpCaching = AddressLookupFrontendSessionCache

  override def init(journeyName: String): JourneyData = {
    try {
      cfg.get(journeyName).get
    } catch {
      case none: NoSuchElementException => throw new IllegalArgumentException(s"Invalid journey name: '$journeyName'", none)
    }
  }

  override def get(id: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[JourneyData]] = {
      cache.fetchAndGetEntry[JourneyData](cache.defaultSource, cacheId, id)
  }

  override def put(id: String, data: JourneyData)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Boolean] = {
    cache.cache(cache.defaultSource, cacheId, id, data).map { res =>
      true
    }
  }

  private def journey(key: String, journeys: ConfigObject): JourneyData = {
    import model.JourneyData._

    val renderOptions =
      ConfigRenderOptions.defaults()
        .setJson(true)
        .setComments(false)
        .setOriginComments(false)

    val journeyConfigJson = journeys
      .get(key)
      .render(renderOptions)

    val journeyConfig = Json.fromJson[JourneyConfig](Json.parse(journeyConfigJson)).get

    JourneyData(journeyConfig)
  }

}
