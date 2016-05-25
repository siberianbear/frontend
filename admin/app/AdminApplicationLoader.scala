import common.{LifecycleComponents, CloudWatchMetricsLifecycle}
import common.Logback.LogstashLifecycle
import common.dfp.DfpAgentLifecycle
import conf.{CachedHealthCheckLifeCycle, CommonGzipFilter}
import controllers.HealthCheck
import dfp.DfpDataCacheLifecycle
import model.{ApplicationIdentity, AdminLifecycle}
import ophan.SurgingContentAgentLifecycle
import play.api.ApplicationLoader.Context
import play.api.http.HttpErrorHandler
import play.api.mvc.EssentialFilter
import play.api.routing.Router
import play.api.{BuiltInComponents, Application, ApplicationLoader, BuiltInComponentsFromContext}
import purge.SoftPurgeLifecycle
import services.ConfigAgentLifecycle
import router.Routes

import scala.concurrent.ExecutionContext

class AdminApplicationLoader extends ApplicationLoader {

  override def load(context: Context): Application = {
    val components = new BuiltInComponentsFromContext(context) with RoutingComponents
    components.startLifecycleComponents()
    components.application
  }
}

trait AppComponents extends BuiltInComponents {
  lazy val appIdentity = ApplicationIdentity("frontend-admin")
  implicit lazy val executionContext: ExecutionContext = actorSystem.dispatcher
}

trait Controllers {
  lazy val healthCheck = HealthCheck
}

trait AdminLifecycleComponents extends LifecycleComponents {
  self: AppComponents with Controllers =>
  lazy val adminLifecycle = new AdminLifecycle(applicationLifecycle)
  lazy val configAgentLifecycle = new ConfigAgentLifecycle(applicationLifecycle)
  lazy val cloudWatchLifecycle = new CloudWatchMetricsLifecycle(applicationLifecycle, appIdentity)
  lazy val surgingContentAgentLifecycle = new SurgingContentAgentLifecycle(applicationLifecycle)
  lazy val dfpAgentLifecycle = new DfpAgentLifecycle(applicationLifecycle)
  lazy val dfpDataCacheLifecycle = new DfpDataCacheLifecycle(applicationLifecycle)
  lazy val softPurgeLifecycle = new SoftPurgeLifecycle(applicationLifecycle)
  lazy val logstashLifecycle = new LogstashLifecycle
  lazy val cachedHealthCheckLifecycle = new CachedHealthCheckLifeCycle(healthCheck)

  override lazy val lifecycleComponents = List(
    adminLifecycle,
    configAgentLifecycle,
    cloudWatchLifecycle,
    surgingContentAgentLifecycle,
    dfpAgentLifecycle,
    dfpDataCacheLifecycle,
    softPurgeLifecycle,
    logstashLifecycle,
    cachedHealthCheckLifecycle
  )
}

trait RoutingComponents extends AdminLifecycleComponents with AppComponents with Controllers {
  lazy val prefix = "/"
  lazy val router: Router = Routes
  override lazy val httpErrorHandler: HttpErrorHandler = new AdminHttpErrorHandler(environment, configuration, sourceMapper, router)
  override lazy val httpFilters: Seq[EssentialFilter] = CommonGzipFilter.filters
}
