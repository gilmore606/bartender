package com.dlfsystems.bartender.nav

import android.util.Log
import androidx.fragment.app.Fragment
import com.zhuinden.simplestack.StateChange


class FragmentStateChanger(
    private val fragmentManager: androidx.fragment.app.FragmentManager,
    private val containerId: Int
) {

    class FragWithTag(val fragment: Fragment, val tag: String)

    fun handleStateChange(stateChange: StateChange) {
        val removeList = ArrayList<Fragment>(0)
        val addList = ArrayList<FragWithTag>(0)
        val showList = ArrayList<Fragment>(0)
        val hideList = ArrayList<Fragment>(0)

        val previousState = stateChange.getPreviousState<BaseKey>()
        val newState = stateChange.getNewState<BaseKey>()

        val animationSet = if (stateChange.direction == StateChange.FORWARD)
            stateChange.topNewState<BaseKey>().getAnimation()
        else
            stateChange.topPreviousState<BaseKey>()?.getBackAnimation() ?: FragAnimPair(0, 0)

        for (oldKey in previousState) {
            val fragment = fragmentManager.findFragmentByTag(oldKey.fragmentTag)
            if (fragment != null) {
                if (!newState.contains(oldKey)) {
                    removeList.add(fragment)
                } else if (!fragment.isHidden) {
                    hideList.add(fragment)
                }
            }
        }
        for (newKey in newState) {
            var fragment: androidx.fragment.app.Fragment? = fragmentManager.findFragmentByTag(newKey.fragmentTag)
            if (newKey == stateChange.topNewState<Any>()) {
                if (fragment != null) {
                    if (fragment.isHidden) {
                        showList.add(fragment)
                    }
                } else {
                    fragment = newKey.newFragment()
                    addList.add(FragWithTag(fragment, newKey.fragmentTag))
                }
            } else {
                if (fragment != null && !fragment.isHidden) {
                    hideList.add(fragment)
                }
            }
        }


        fragmentManager.beginTransaction().disallowAddToBackStack().apply {
            setCustomAnimations(animationSet.animIn, animationSet.animOut)
            removeList.forEach { remove(it) }
            addList.forEach{ add(containerId, it.fragment, it.tag) }
            showList.forEach{ show(it) }
            hideList.forEach{ hide(it) }
        }.commitNow()
    }
}