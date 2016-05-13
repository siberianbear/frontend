import commercial.CommercialLifecycle
import common.Logback.Logstash
import common.dfp.FaciaDfpAgentLifecycle
import conf._
import feed.OnwardJourneyLifecycle
import rugby.conf.RugbyLifecycle
import services.ConfigAgentLifecycle

class StandaloneGlobal extends CommercialLifecycle
  with OnwardJourneyLifecycle
  with ConfigAgentLifecycle
  with FaciaDfpAgentLifecycle
  with SwitchboardLifecycle
  with FootballLifecycle
  with CricketLifecycle
  with RugbyLifecycle
  with Logstash
