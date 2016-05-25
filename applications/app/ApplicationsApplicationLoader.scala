import common._
import common.Logback.LogstashLifecycle
import common.dfp.DfpAgentLifecycle
import conf.{CorsHttpErrorHandler, SwitchboardLifecycle, CommonFilters, CachedHealthCheckLifeCycle, HealthCheck}
import contentapi.SectionsLookUpLifecycle
import dev.DevParametersHttpRequestHandler
import jobs.SiteMapLifecycle
import model.ApplicationIdentity
import ophan.SurgingContentAgentLifecycle
import play.api.ApplicationLoader.Context
import play.api.http.{HttpErrorHandler, HttpRequestHandler}
import play.api.mvc.EssentialFilter
import play.api.routing.Router
import play.api.{BuiltInComponents, Application, ApplicationLoader, BuiltInComponentsFromContext}
import services.{IndexListingsLifecycle, ConfigAgentLifecycle}
import router.Routes

import scala.concurrent.ExecutionContext

class ApplicationsApplicationLoader extends ApplicationLoader {

  override def load(context: Context): Application = {
    val components = new BuiltInComponentsFromContext(context) with RoutingComponents
    components.startLifecycleComponents()
    components.application
  }
}

trait AppComponents extends BuiltInComponents {
  lazy val appIdentity = ApplicationIdentity("frontend-applications")
  implicit lazy val executionContext: ExecutionContext = actorSystem.dispatcher
}

trait Controllers {
  lazy val healthCheck = HealthCheck
}

trait AdminLifecycleComponents extends LifecycleComponents {
  self: AppComponents with Controllers =>

  val applicationMetrics = ApplicationMetrics(
    ContentApiMetrics.HttpTimeoutCountMetric,
    ContentApiMetrics.HttpLatencyTimingMetric,
    ContentApiMetrics.ContentApiErrorMetric,
    EmailSubsciptionMetrics.EmailSubmission,
    EmailSubsciptionMetrics.EmailFormError,
    EmailSubsciptionMetrics.NotAccepted,
    EmailSubsciptionMetrics.APIHTTPError,
    EmailSubsciptionMetrics.APINetworkError,
    EmailSubsciptionMetrics.ListIDError,
    EmailSubsciptionMetrics.AllEmailSubmission
  )

  lazy val configAgentLifecycle = new ConfigAgentLifecycle(applicationLifecycle)
  lazy val cloudWatchLifecycle = new CloudWatchMetricsLifecycle(applicationLifecycle, appIdentity)
  lazy val dfpAgentLifecycle = new DfpAgentLifecycle(applicationLifecycle)
  lazy val indexListingLifecycle = IndexListingsLifecycle
  lazy val surgingContentAgentLifecycle = new SurgingContentAgentLifecycle(applicationLifecycle)
  lazy val sectionsLookUpLifecycle = new SectionsLookUpLifecycle(applicationLifecycle)
  lazy val switchboardLifecycle = new SwitchboardLifecycle(applicationLifecycle)
  lazy val logstashLifecycle = new LogstashLifecycle
  lazy val cachedHealthCheckLifecycle = new CachedHealthCheckLifeCycle(healthCheck)
  lazy val siteMapLifecycle = new SiteMapLifecycle()

  override lazy val lifecycleComponents = List(
    configAgentLifecycle,
    cloudWatchLifecycle,
    dfpAgentLifecycle,
    indexListingLifecycle,
    surgingContentAgentLifecycle,
    sectionsLookUpLifecycle,
    switchboardLifecycle,
    logstashLifecycle,
    cachedHealthCheckLifecycle,
    siteMapLifecycle
  )
}

trait RoutingComponents extends AdminLifecycleComponents with AppComponents with Controllers {
  lazy val prefix = "/"
  lazy val router: Router = Routes
  lazy val allFilters = CommonFilters
  override lazy val httpFilters: Seq[EssentialFilter] = allFilters.filters
  override lazy val httpErrorHandler: HttpErrorHandler = new CorsHttpErrorHandler(environment, configuration, sourceMapper, router)
  override lazy val httpRequestHandler: HttpRequestHandler = new DevParametersHttpRequestHandler(router, httpErrorHandler, httpConfiguration, allFilters)
}
