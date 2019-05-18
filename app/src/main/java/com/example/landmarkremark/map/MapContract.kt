package com.example.landmarkremark.map

import com.example.landmarkremark.remote.entity.MapEntity

// This is a contract that the application uses as part of the MVP Architecture
interface MapContract {

    interface View  {
        fun showMapData(mapDetails : List<MapEntity>)
        fun showMapDataSearch(mapDetails : List<MapEntity>)
        fun showSuccessDataSaved()
        fun showFailureMessage()
        fun queriedDataNotFound()
        fun showNotInternetConnectionMessage()
        fun showRealTimeUpdates(userUpdatedList: ArrayList<MapEntity>)
    }

    interface Presenter {
        fun getMapData()
        fun saveMark(mapDetails : MapEntity)
        fun processQuery(searchText : String, mapData : List<MapEntity>)
        fun attachView(mvpView: MapContract.View)
        fun detachView()
        fun setRealTimeUpdates()
    }
}