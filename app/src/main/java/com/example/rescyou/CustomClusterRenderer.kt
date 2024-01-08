package com.example.rescyou

import android.content.Context
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer

class CustomClusterRenderer(
    context: Context,
    map: GoogleMap,
    clusterManager: ClusterManager<ClusteredPins>
) : DefaultClusterRenderer<ClusteredPins>(context, map, clusterManager) {

    override fun onBeforeClusterItemRendered(item: ClusteredPins, markerOptions: MarkerOptions) {
        // Customize the marker here
    }

    override fun onBeforeClusterRendered(cluster: Cluster<ClusteredPins>, markerOptions: MarkerOptions) {
        // Customize the cluster marker here
    }

    override fun shouldRenderAsCluster(cluster: Cluster<ClusteredPins>): Boolean {
        // Define the conditions under which the markers should be clustered
        return cluster.size > 1
    }
}