package com.dicoding.picodiploma.ha.test

import android.content.Context
import com.dicoding.picodiploma.ha.Model.MyItem
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer

class MarkerClusterRenderer(context : Context, mMap:GoogleMap, clusterManager: ClusterManager<MyItem>?, resId : Int):DefaultClusterRenderer<MyItem>(context,mMap,clusterManager) {

    private val ikon : BitmapDescriptor
    init {
        ikon = BitmapDescriptorFactory.fromResource(resId)
    }

    override fun onBeforeClusterItemRendered(item: MyItem, markerOptions: MarkerOptions) {
        super.onBeforeClusterItemRendered(item, markerOptions)

        markerOptions.icon(ikon)
        markerOptions.title(item.title)
    }
}