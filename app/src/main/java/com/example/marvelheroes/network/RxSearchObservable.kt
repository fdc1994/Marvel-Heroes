package com.example.marvelheroes.network

import android.widget.SearchView
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

/**RxSearchObservable Api Call
 *This object is a singleton by nature and will allows us to set the listeners
 * for anonymous searchviews which can then be assigned onCreateOptionsMenu
 * thus allowing for a cleaner code
 *
 * It is a simple publish subject which will emmit the text changes
 * */

object RxSearchObservable  {
    fun fromView(searchView: androidx.appcompat.widget.SearchView?): Observable<String> {
        val subject: PublishSubject<String> = PublishSubject.create()
        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(s: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(text: String?): Boolean {
                text?.let { subject.onNext(it) }
                return true
            }
        })
        return subject
    }
}