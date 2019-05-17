package com.example.landmarkremark.map

import android.support.v7.app.AppCompatActivity
import com.example.landmarkremark.remote.entity.MapEntity
import com.example.landmarkremark.utils.Constants
import com.google.firebase.firestore.*


class MapPresenter : AppCompatActivity(), MapContract.Presenter {

    private var mRef: ListenerRegistration? = null

    override fun setRealTimeUpdates() {
        val mDataListener = db.collection(Constants.Firebase.USER_COLLECTION)

        mRef = mDataListener.addSnapshotListener(EventListener<QuerySnapshot> { snapshot, e ->
            val userUpdatedList: ArrayList<MapEntity> = ArrayList()
            if (e != null) {
                return@EventListener
            }

            if (snapshot != null) {
                for (document in snapshot.documents){
                    val data: MapEntity = document?.toObject(MapEntity::class.java)!!
                    userUpdatedList += data
                }
                mView?.showRealTimeUpdates(userUpdatedList)
            }
        })
    }

    private var mView: MapContract.View? = null
    private val db = FirebaseFirestore.getInstance()



    override fun getMapData() {
        val userList: ArrayList<MapEntity> = ArrayList()
        db.collection(Constants.Firebase.USER_COLLECTION)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val data: MapEntity = document?.toObject(MapEntity::class.java)!!
                    userList += data
                }
                mView?.showMapData(userList)

            }
            .addOnFailureListener { exception ->
                mView?.showFailureMessage()
            }
    }

    override fun saveMark(mapDetails: MapEntity) {
        var userReference = db.collection(Constants.Firebase.USER_COLLECTION)
        userReference.add(mapDetails)
            .addOnSuccessListener { mView?.showSuccessDataSaved() }
            .addOnFailureListener { mView?.showFailureMessage() }
    }

    override fun processQuery(searchText: String, mapData : List<MapEntity>) {
        val queryResult = ArrayList(mapData.filter {
            it.userName.contains(searchText, true) ||
                    it.notes!!.contains(searchText, true)
        })

        if (queryResult.isEmpty()) {
            mView?.queriedDataNotFound()
            return
        }
        mView?.showMapDataSearch(queryResult)
    }


    override fun attachView(mvpView: MapContract.View) {
        mView = mvpView
    }

    override fun detachView() {
        mView = null
        // Stop listening to changes
        mRef?.remove()
    }
}