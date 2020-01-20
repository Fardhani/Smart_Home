package com.fardhani.smarthome.Model

//data class door status for firebase
data class SecurityStatusModel(
    var isLocked: Boolean?,
    var isClosed: Boolean?,
    var isSecurityMode: Boolean?
)