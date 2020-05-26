package com.shigaga.restfuldemo.data

data class Weather(var startTime: String,
                   var endTime: String,
                   var parameter: Parameter)


data class Parameter(var parameterName: String,
                   var parameterUnit: String)
