package conf

object HealthCheck extends AllGoodCachedHealthCheck(
  9015,
  "/news-alert/alerts"
)
