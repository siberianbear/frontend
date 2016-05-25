import common.{ContentApiMetrics, ApplicationMetrics, LifecycleComponents, CloudWatchMetricsLifecycle}
import common.Logback.LogstashLifecycle
import conf.{HealthCheck, SwitchboardLifecycle, CommonFilters, CachedHealthCheckLifeCycle}
import contentapi.SectionsLookUpLifecycle
import dev.DevParametersHttpRequestHandler
import model.ApplicationIdentity
import ophan.SurgingContentAgentLifecycle
import play.api.ApplicationLoader.Context
import play.api.http.HttpRequestHandler
import play.api.mvc.EssentialFilter
import play.api.routing.Router
import play.api.{BuiltInComponents, Application, ApplicationLoader, BuiltInComponentsFromContext}
import services.ConfigAgentLifecycle
import router.Routes

import scala.concurrent.ExecutionContext

class AdminJobsApplicationLoader extends ApplicationLoader {

  override def load(context: Context): Application = {
    val components = new BuiltInComponentsFromContext(context) with RoutingComponents
    components.startLifecycleComponents()
    components.application
  }
}

trait AppComponents extends BuiltInComponents {
  lazy val appIdentity = ApplicationIdentity("frontend-admin-jobs")
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
    ContentApiMetrics.ContentApiErrorMetric
  )

  lazy val configAgentLifecycle = new ConfigAgentLifecycle(applicationLifecycle)
  lazy val cloudWatchLifecycle = new CloudWatchMetricsLifecycle(applicationLifecycle, appIdentity, applicationMetrics)
  lazy val sectionsLookupLifecycle = new SectionsLookUpLifecycle(applicationLifecycle)
  lazy val switchboardLifecycle = new SwitchboardLifecycle(applicationLifecycle)
  lazy val surgingContentAgentLifecycle = new SurgingContentAgentLifecycle(applicationLifecycle)
  lazy val logstashLifecycle = new LogstashLifecycle
  lazy val cachedHealthCheckLifecycle = new CachedHealthCheckLifeCycle(healthCheck)

  override lazy val lifecycleComponents = List(
    configAgentLifecycle,
    cloudWatchLifecycle,
    sectionsLookupLifecycle,
    switchboardLifecycle,
    surgingContentAgentLifecycle,
    logstashLifecycle,
    cachedHealthCheckLifecycle
  )
}

trait RoutingComponents extends AdminLifecycleComponents with AppComponents with Controllers {
  lazy val prefix = "/"
  lazy val router: Router = Routes
  lazy val allFilters = CommonFilters
  override lazy val httpFilters: Seq[EssentialFilter] = allFilters.filters
  override lazy val httpRequestHandler: HttpRequestHandler = new DevParametersHttpRequestHandler(router, httpErrorHandler, httpConfiguration, allFilters)
}
