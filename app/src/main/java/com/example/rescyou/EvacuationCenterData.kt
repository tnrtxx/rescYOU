package com.example.rescyou

import java.io.Serializable

data class EvacuationCenterData(
    var evacuationCenterId: String? = null,     // Primary key
    var placeId: String? = null,                // Foreign key to Google Places
    var name: String? = null,
    var address: String? = null,
    var latitude: Double? = 0.0,
    var longitude: Double? = 0.0,
    var status: String? = null,
    var inCharge: String? = null,
    var inChargeContactNum: String? = null,
    var occupants: String? = null,
) : Serializable

