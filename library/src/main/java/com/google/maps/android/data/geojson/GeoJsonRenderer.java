/*
 * Copyright 2020 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.maps.android.data.geojson;

import com.google.android.gms.maps.GoogleMap;
import com.google.maps.android.collections.GroundOverlayManager;
import com.google.maps.android.collections.MarkerManager;
import com.google.maps.android.collections.PolygonManager;
import com.google.maps.android.collections.PolylineManager;
import com.google.maps.android.data.Feature;
import com.google.maps.android.data.Renderer;

import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

/**
 * Renders GeoJsonFeature objects onto the GoogleMap as Marker, Polyline and Polygon objects. Also
 * removes GeoJsonFeature objects and redraws features when updated.
 */
public class GeoJsonRenderer extends Renderer implements Observer {

    private final static Object FEATURE_NOT_ON_MAP = null;

    /**
     * Creates a new GeoJsonRender object
     *
     * @param map      activity_maps to place GeoJsonFeature objects on
     * @param features contains a hashmap of features and objects that will go on the activity_maps
     * @param markerManager marker manager to create marker collection from
     * @param polygonManager polygon manager to create polygon collection from
     * @param polylineManager polyline manager to create polyline collection from
     * @param groundOverlayManager ground overlay manager to create ground overlay collection from
     */
    /* package */ GeoJsonRenderer(GoogleMap map, HashMap<GeoJsonFeature, Object> features, MarkerManager markerManager, PolygonManager polygonManager, PolylineManager polylineManager, GroundOverlayManager groundOverlayManager) {
        super(map, features, markerManager, polygonManager, polylineManager, groundOverlayManager);
    }

    /**
     * Changes the activity_maps that GeoJsonFeature objects are being drawn onto. Existing objects are
     * removed from the previous activity_maps and drawn onto the new activity_maps.
     *
     * @param map GoogleMap to place GeoJsonFeature objects on
     */
    public void setMap(GoogleMap map) {
        super.setMap(map);
        for (Feature feature : super.getFeatures()) {
            redrawFeatureToMap((GeoJsonFeature) feature, map);
        }
    }

    /**
     * Adds all of the stored features in the layer onto the activity_maps if the layer is
     * not already on the activity_maps.
     */
    public void addLayerToMap() {
        if (!isLayerOnMap()) {
            setLayerVisibility(true);
            for (Feature feature : super.getFeatures()) {
                addFeature((GeoJsonFeature) feature);
            }
        }
    }

    /**
     * Adds a new GeoJsonFeature to the activity_maps if its geometry property is not null.
     *
     * @param feature feature to add to the activity_maps
     */
    public void addFeature(GeoJsonFeature feature) {
        super.addFeature(feature);
        if (isLayerOnMap()) {
            feature.addObserver(this);
        }
    }

    /**
     * Removes all GeoJsonFeature objects stored in the mFeatures hashmap from the activity_maps
     */
    public void removeLayerFromMap() {
        if (isLayerOnMap()) {
            for (Feature feature : super.getFeatures()) {
                removeFromMap(super.getAllFeatures().get(feature));
                feature.deleteObserver(this);
            }
            setLayerVisibility(false);
        }
    }

    /**
     * Removes a GeoJsonFeature from the activity_maps if its geometry property is not null
     *
     * @param feature feature to remove from activity_maps
     */
    public void removeFeature(GeoJsonFeature feature) {
        // Check if given feature is stored
        super.removeFeature(feature);
        if (super.getFeatures().contains(feature)) {
            feature.deleteObserver(this);
        }
    }

    /**
     * Redraws a given GeoJsonFeature onto the activity_maps. The activity_maps object is obtained from the mFeatures
     * hashmap and it is removed and added.
     *
     * @param feature feature to redraw onto the activity_maps
     */
    private void redrawFeatureToMap(GeoJsonFeature feature) {
        redrawFeatureToMap(feature, getMap());
    }

    private void redrawFeatureToMap(GeoJsonFeature feature, GoogleMap map) {
        removeFromMap(getAllFeatures().get(feature));
        putFeatures(feature, FEATURE_NOT_ON_MAP);
        if (map != null && feature.hasGeometry()) {
            putFeatures(feature, addGeoJsonFeatureToMap(feature, feature.getGeometry()));
        }
    }

    /**
     * Update is called if the developer sets a style or geometry in a GeoJsonFeature object
     *
     * @param observable GeoJsonFeature object
     * @param data       null, no extra argument is passed through the notifyObservers method
     */
    public void update(Observable observable, Object data) {
        if (observable instanceof GeoJsonFeature) {
            GeoJsonFeature feature = ((GeoJsonFeature) observable);
            boolean featureIsOnMap = getAllFeatures().get(feature) != FEATURE_NOT_ON_MAP;
            if (featureIsOnMap && feature.hasGeometry()) {
                // Checks if the feature has been added to the activity_maps and its geometry is not null
                // TODO: change this so that we don't add and remove
                redrawFeatureToMap(feature);
            } else if (featureIsOnMap && !feature.hasGeometry()) {
                // Checks if feature is on activity_maps and geometry is null
                removeFromMap(getAllFeatures().get(feature));
                putFeatures(feature, FEATURE_NOT_ON_MAP);
            } else if (!featureIsOnMap && feature.hasGeometry()) {
                // Checks if the feature isn't on the activity_maps and geometry is not null
                addFeature(feature);
            }
        }
    }
}
