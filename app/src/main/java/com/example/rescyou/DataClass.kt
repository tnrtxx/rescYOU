package com.example.rescyou

data class DataClass(
    var dataTitle: String? = null,
    var dataDesc: String? = null,
    var dataImage: String? = null,
    var dataImageIcon: String? = null,
    var dataImageSource: String? = null,
    var dataArticleSource: String? = null,
    var key: String? = null

) {

    // No-argument constructor to satisfy Firebase's deserialization requirements
    constructor() : this(null, null, null, null, null, null)

    // Function to replace "\\n" with "\n" in dataDesc
    fun replaceNewlines() {
        dataDesc = dataDesc?.replace("\\n", "\n")
        dataDesc = dataDesc?.replace("\\'", "\'")
        dataTitle = dataTitle?.replace("\\n", "\n")
    }
}
