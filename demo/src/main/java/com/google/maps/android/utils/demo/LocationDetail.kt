package com.google.maps.android.utils.demo

import LocationDetailViewModel
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.text.TextUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.maps.android.utils.demo.utility.combineLatLng
import kotlinx.android.synthetic.main.location_detail_fragment.*

class LocationDetail : Fragment() {

    private lateinit var viewModel: LocationDetailViewModel
    private val args: LocationDetailArgs by navArgs()
    private lateinit var well_data: WellData
    private lateinit var bottom_sheet_top_content: ConstraintLayout
    private lateinit var bottom_sheet_rv: RecyclerView
    private lateinit var bottom_sheets_done: TextView

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.location_detail_fragment, container, false)
        bottom_sheet_top_content = container!!.rootView.findViewById(R.id.bottom_sheet_top_content)
        bottom_sheet_rv = container.rootView.findViewById(R.id.bottom_sheet_rv)
        bottom_sheets_done = container.rootView.findViewById(R.id.bottom_sheets_done)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel =
                ViewModelProviders.of(requireActivity()).get(LocationDetailViewModel::class.java)
        val list_items = arrayListOf<WellData>()

        viewModel.fetchStaticWellData(args.sensorName.formatWellId())
        well_data = WellData(
                underground_water = if (TextUtils.isEmpty(edit_text_water_level.text)) 0.0 else edit_text_water_level.text.toString()
                        .toDouble(),
                precipitation = if (TextUtils.isEmpty(edit_text_precipitation.text)) 0 else edit_text_precipitation.text.toString()
                        .toInt()
        )

        viewModel.wellData.observe(viewLifecycleOwner, Observer { data ->
            well_data.id = data.id
            well_data.latitude = data.latitude
            well_data.longitude = data.longitude
            well_data.elevation = data.elevation
            detail_well_id.text = well_data.id.toString()
            detail_well_latlng.text = combineLatLng(well_data)
            detail_well_elevation.text = well_data.elevation.toString()
        })

        well_detail_upload.setOnClickListener {
            bottom_sheets_done.showView()
            viewModel.addWellItem(well_data)
            bottom_sheet_top_content.hideView()
            bottom_sheet_rv.showView()
        }

        val wells_list_adapter = WellItemsAdapter(list_items)
        viewModel.fetchWellItems().observe(viewLifecycleOwner, Observer { list_items ->
            wells_list_adapter.updateList(list_items)
        })
        bottom_sheet_rv.adapter = wells_list_adapter
        bottom_sheet_rv.layoutManager = LinearLayoutManager(activity)
    }

}