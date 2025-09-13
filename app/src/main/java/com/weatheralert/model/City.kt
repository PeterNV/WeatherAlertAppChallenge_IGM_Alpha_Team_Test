package com.weatheralert.model

import com.google.android.gms.maps.model.LatLng

data class City(
    val name: String,
    var isMonitored: Boolean = false,
    var location: LatLng? = null,
    val salt: Long? = null, // usado para forçar atualizacao da UI

)