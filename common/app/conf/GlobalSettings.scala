package conf

import common._
import conf.switches.Switches
import model.Cors
import play.api.Mode
import play.api.http.Status._
import play.api.http.DefaultHttpErrorHandler
import play.api.inject.ApplicationLifecycle
import play.api.routing.Router
import play.api.{Configuration => PlayConfiguration, Environment}
import play.api.mvc.{Result, RequestHeader, Results}
import play.core.SourceMapper

import scala.concurrent.{ExecutionContext, Future}

class CorsHttpErrorHandler(
  environment: Environment,
  configuration: PlayConfiguration,
  sourceMapper: Option[SourceMapper],
  router: Router
)(implicit ec: ExecutionContext) extends DefaultHttpErrorHandler(
  environment = environment,
  configuration = configuration,
  sourceMapper = sourceMapper,
  router = Some(router)
) with Results {

  private val varyFields = List("Origin", "Accept")
  private val defaultVaryFields = varyFields.mkString(",")

  override def onServerError(request: RequestHeader, ex: Throwable) = {
    // Overriding onError in Dev can hide helpful Exception messages.
    if (environment.mode == Mode.Dev) {
      super.onServerError(request, ex)
    } else {
      val headers = request.headers
      val vary = headers.get("Vary").fold(defaultVaryFields)(v => (v :: varyFields).mkString(","))

      Future.successful {
        Cors(InternalServerError.withHeaders("Vary" -> vary))(request)
      }
    }
  }

  override def onClientError(request : RequestHeader, statusCode: Int, message: String) : Future[Result] = statusCode match {
    case NOT_FOUND | BAD_REQUEST => super.onClientError(request, statusCode, message).map(Cors(_)(request))
    case _ => super.onClientError(request, statusCode, message)
  }
}

class SwitchboardLifecycle(appLifecycle: ApplicationLifecycle)(implicit ec: ExecutionContext) extends LifecycleComponent with ExecutionContexts with Logging {

  override def start(): Unit = {
    Jobs.deschedule("SwitchBoardRefreshJob")
    //run every minute, 47 seconds after the minute
    Jobs.schedule("SwitchBoardRefreshJob", "47 * * * * ?") {
      refresh()
    }

    AkkaAsync {
      refresh()
    }

    appLifecycle.addStopHook { () => Future {
      Jobs.deschedule("SwitchBoardRefreshJob")
    }
    }
  }

  def refresh(): Unit = {
    log.info("Refreshing switches")
    services.S3.get(Configuration.switches.key) foreach { response =>

      val nextState = Properties(response)

      for (switch <- Switches.all) {
        nextState.get(switch.name) match {
          case Some("on") => switch.switchOn()
          case Some("off") => switch.switchOff()
          case other => {
            log.warn(s"Badly configured switch ${switch.name}, setting to safe state.")
            switch.switchToSafeState()
          }
        }
      }
    }
  }
}
