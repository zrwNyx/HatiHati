package com.dicoding.picodiploma.ha.test

import com.google.android.gms.maps.model.Polyline
import com.google.gson.annotations.SerializedName

data class DirectionResponses(
    @SerializedName("geocoded_waypoints")
    var geocodedWaypoints: List<GeocodedWaypoint?>?,
    @SerializedName("routes")
    var routes: List<Route?>?,
    @SerializedName("status")
    var status: String?
)

data class GeocodedWaypoint(
    @SerializedName("geocoder_status")
    var geocoderStatus: String?,
    @SerializedName("place_id")
    var placeId: String?,
    @SerializedName("types")
    var types: List<String?>?
)
data class Route(
    @SerializedName("bounds")
    var bounds: Bounds?,
    @SerializedName("copyrights")
    var copyrights: String?,
    @SerializedName("legs")
    var legs: List<Leg?>?,
    @SerializedName("overview_polyline")
    var overviewPolyline: OverviewPolyline?,
    @SerializedName("summary")
    var summary: String?,
    @SerializedName("warnings")
    var warnings: List<Any?>?,
    @SerializedName("waypoint_order")
    var waypointOrder: List<Any?>?
)


data class Bounds(
    @SerializedName("northeast")
    var northeast: Northeast?,
    @SerializedName("southwest")
    var southwest: Southwest?
)

data class Leg(
    @SerializedName("distance")
    var distance: Distance?,
    @SerializedName("duration")
    var duration: Duration?,
    @SerializedName("end_address")
    var endAddress: String?,
    @SerializedName("end_location")
    var endLocation: EndLocation?,
    @SerializedName("start_address")
    var startAddress: String?,
    @SerializedName("start_location")
    var startLocation: StartLocation?,
    @SerializedName("steps")
    var steps: List<Step?>?,
    @SerializedName("traffic_speed_entry")
    var trafficSpeedEntry: List<Any?>?,
    @SerializedName("via_waypoint")
    var viaWaypoint: List<Any?>?
)

data class Duration(
    @SerializedName("text")
    var text: String?,
    @SerializedName("value")
    var value: Int?
)

data class EndLocation(
    @SerializedName("lat")
    var lat: Double?,
    @SerializedName("lng")
    var lng: Double?
)

data class Distance(
    @SerializedName("text")
    var text: String?,
    @SerializedName("value")
    var value: Int?
)


data class OverviewPolyline(
    @SerializedName("points")
    var points: String?
)

data class Southwest(
    @SerializedName("lat")
    var lat: Double?,
    @SerializedName("lng")
    var lng: Double?
)

data class Northeast(
    @SerializedName("lat")
    var lat: Double?,
    @SerializedName("lng")
    var lng: Double?
)

data class StartLocation(
    @SerializedName("lat")
    var lat: Double?,
    @SerializedName("lng")
    var lng: Double?
)

data class Step(
    @SerializedName("distance")
    var distance: Distance?,
    @SerializedName("duration")
    var duration: Duration?,
    @SerializedName("end_location")
    var endLocation: EndLocation?,
    @SerializedName("html_instructions")
    var htmlInstructions: String?,
    @SerializedName("maneuver")
    var maneuver: String?,
    @SerializedName("polyline")
    var polyline: Polyline?,
    @SerializedName("start_location")
    var startLocation: StartLocation?,
    @SerializedName("travel_mode")
    var travelMode: String?
)