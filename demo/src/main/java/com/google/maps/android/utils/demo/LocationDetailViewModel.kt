import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.maps.android.utils.demo.WellData
import com.google.maps.android.utils.demo.WellDataClient
import com.google.maps.android.utils.demo.WellDataService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import java.util.*

class LocationDetailViewModel : ViewModel() {

    private val compositeDisposable = CompositeDisposable()
    private val wellDataClient: WellDataClient = WellDataService.service()

    private val well_data = MutableLiveData<WellData>()
    val wellData: LiveData<WellData> = well_data

    private val list_well_items = arrayListOf<WellData>()
    private val observable_well_items = MutableLiveData<ArrayList<WellData>>()

    fun fetchStaticWellData(id: Int) {
        Log.d("LocationDetailViewModel", "Fetch well data")
        compositeDisposable.add(
                wellDataClient.getWellData(id)
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(object : DisposableSingleObserver<WellData>() {
                            override fun onSuccess(data: WellData) {
                                Log.d("LocationDetailViewModel", "Well data: ${data.id}")
                                well_data.postValue(data)
                            }

                            override fun onError(e: Throwable) {
                                Log.e("LocationDetailViewModel", "Error loading static well data")
                                Log.e("LocationDetailViewModel", e.localizedMessage)
                            }

                        })
        )
    }

    fun addWellItem(well_item: WellData) {
        list_well_items.add(well_item)
        Log.d("LocationDetailViewModel", "ViewModel list size: ${list_well_items.size}")
        observable_well_items.value = list_well_items
    }

    fun fetchWellItems(): LiveData<ArrayList<WellData>> = observable_well_items

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}