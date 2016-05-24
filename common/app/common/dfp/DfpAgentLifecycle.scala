package common.dfp

import common.{LifecycleComponent, AkkaAsync, Jobs}
import play.api.inject.ApplicationLifecycle

import scala.concurrent.{ExecutionContext, Future}

class DfpAgentLifecycle(appLifeCycle: ApplicationLifecycle)(implicit ec: ExecutionContext) extends LifecycleComponent {

  def refreshDfpAgent(): Unit = DfpAgent.refresh()

  override def start() = {
    Jobs.deschedule("DfpDataRefreshJob")
    Jobs.scheduleEveryNMinutes("DfpDataRefreshJob", 1) {
      refreshDfpAgent()
      Future.successful(())
    }

    AkkaAsync {
      refreshDfpAgent()
    }

    appLifeCycle.addStopHook { () => Future {
      Jobs.deschedule("DfpDataRefreshJob")
    }}
  }
}

trait FaciaDfpAgentLifecycle extends DfpAgentLifecycle {

  override def refreshDfpAgent(): Unit = {
    DfpAgent.refresh()
    DfpAgent.refreshFaciaSpecificData()
  }
}
