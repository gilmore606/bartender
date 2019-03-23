package com.dlfsystems.bartender.nav

import io.reactivex.Emitter
import io.reactivex.observables.ConnectableObservable

object Rudder {

    lateinit var navDestEmitter: Emitter<BaseKey>

    val navDest: ConnectableObservable<BaseKey> = ConnectableObservable.create<BaseKey> {
        navDestEmitter = it
    }.publish()

    init {
        navDest.connect()
    }

    fun navTo(dest: BaseKey) {
        navDestEmitter.onNext(dest)
    }
}