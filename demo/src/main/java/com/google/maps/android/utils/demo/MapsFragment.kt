package com.google.maps.android.utils.demo

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.TileOverlay
import com.google.android.gms.maps.model.TileOverlayOptions
import com.google.maps.android.data.kml.KmlLayer
import com.google.maps.android.heatmaps.HeatmapTileProvider
import com.google.maps.android.heatmaps.WeightedLatLng
import kotlinx.android.synthetic.main.heatmaps_demo.*
import org.json.JSONArray
import org.json.JSONException
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream
import java.util.*
import kotlin.collections.HashMap

class MapsFragment : Fragment() {

    private var mProvider: HeatmapTileProvider? = null
    private var mOverlay: TileOverlay? = null
    private lateinit var mMap: GoogleMap

    /**
     * Maps name of data set to data (list of LatLngs)
     * Also maps to the URL of the data set for attribution
     */
    private val mLists = HashMap<String, DataSet>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.heatmaps_demo, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)

        mapView.onCreate(savedInstanceState)

        mapView.onResume() // needed to get the map to display immediately

        try {
            MapsInitializer.initialize(activity?.applicationContext)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        mapView.getMapAsync { googleMap ->
            mMap = googleMap
            mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(34.659616, -77.068516761500007), 10f))
            try {
                mLists[getString(R.string.police_stations)] = DataSet(readItems(R.raw.police))
            } catch (e: JSONException) {
                Toast.makeText(activity, "Problem reading list of markers.", Toast.LENGTH_LONG).show()
            }
            // Check if need to instantiate (avoid setData etc twice)
            if (mProvider == null) {
                mProvider = HeatmapTileProvider.Builder()
                        .radius(50)
                        .opacity(1.0)
                        .maxIntensity(5.0)
                        .weightedData(mLists[getString(R.string.police_stations)]!!.data)
                        .build()
                mOverlay = mMap?.addTileOverlay(TileOverlayOptions().tileProvider(mProvider))
            } else {
                mProvider!!.setWeightedData(mLists["Well Sites"]!!.data)
                mOverlay!!.clearTileCache()
            }
            try {
                val kml_layer = KmlLayer(mMap, R.raw.kmlfile, activity)
                kml_layer.addLayerToMap()
                kml_layer.setOnFeatureClickListener {
                    feature ->
                    Log.d("MapsFragment", feature.getProperty("name").formatWellId().toString())
                    findNavController().navigate(
                            MapsFragmentDirections.actionMapsFragmentToLocationDetail(
                                    feature.getProperty("name")
                            )
                    )

                }
            } catch (e: XmlPullParserException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    @Throws(JSONException::class)
    fun readItems(resource: Int): ArrayList<WeightedLatLng?>? {
        val list = ArrayList<WeightedLatLng?>()
        val inputStream: InputStream = resources.openRawResource(resource)
        val json = Scanner(inputStream).useDelimiter("\\A").next()
        val array = JSONArray(json)
        for (i in 0 until array.length()) {
            val `object` = array.getJSONObject(i)
            val lat = `object`.getDouble("lat")
            val lng = `object`.getDouble("lng")
            val latLng = LatLng(lat, lng)
            val random = Random()
            val intensity = random.nextInt(5)
            list.add(WeightedLatLng(latLng, intensity.toDouble()))
        }
        return list
    }

    /**
     * Helper class - stores data sets and sources.
     */
    class DataSet(private val mDataset: ArrayList<WeightedLatLng?>?) {
        val data: Collection<WeightedLatLng?>?
            get() = mDataset

    }
}