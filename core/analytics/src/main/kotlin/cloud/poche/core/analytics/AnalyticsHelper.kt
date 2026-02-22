package cloud.poche.core.analytics

interface AnalyticsHelper {
    fun logEvent(event: AnalyticsEvent)
}

data class AnalyticsEvent(
    val type: String,
    val extras: List<Param> = emptyList(),
) {
    data class Param(val key: String, val value: String)
}
