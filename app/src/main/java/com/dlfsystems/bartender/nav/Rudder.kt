package com.dlfsystems.bartender.nav

import io.reactivex.Emitter
import io.reactivex.observables.ConnectableObservable
import io.reactivex.subjects.PublishSubject

object Rudder {

    lateinit var navDestEmitter: Emitter<BaseKey>

    private var loadProgress: Float = 0f

    val loadingProgress = PublishSubject.create<Int>()

    val navDest: ConnectableObservable<BaseKey> = ConnectableObservable.create<BaseKey> {
        navDestEmitter = it
    }.publish()

    init {
        navDest.connect()
    }

    fun navTo(dest: BaseKey) {
        navDestEmitter.onNext(dest)
    }

    fun addLoadProgress(added: Float) {
        loadProgress += added
        loadingProgress.onNext(loadProgress.toInt())
    }
}