data class HotlinesDataClass (
    var dataName: String? = null,
    var dataPhone: String? = null,
    var key: String? = null
)

data class Hotlines (
    var dataName: String? = null,
    var dataPhone: String? = null
)
 {

    fun replaceNewlines() {
        dataName = dataName?.replace("\\n", "\n")
        dataPhone = dataPhone?.replace("\\'", "\'")
    }

    fun toHotlines(): Hotlines {
        // Ensure that both dataName and dataPhone are not null before creating a Hotlines object
        requireNotNull(dataName) { "dataName must not be null" }
        requireNotNull(dataPhone) { "dataPhone must not be null" }

        return Hotlines(dataName = this.dataName!!, dataPhone = this.dataPhone!!)
    }
}
