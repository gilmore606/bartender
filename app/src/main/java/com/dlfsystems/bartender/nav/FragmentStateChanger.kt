package com.dlfsystems.bartender.nav

import android.util.Log
import androidx.fragment.app.Fragment
import com.dlfsystems.bartender.BaseFragment
import com.zhuinden.simplestack.StateChange


class FragmentStateChanger(
    private val fragmentManager: androidx.fragment.app.FragmentManager,
    private val containerId: Int
) {

    class FragWithTag(val fragment: BaseFragment, val tag: String)

    fun handleStateChange(stateChange: StateChange) {
        val removeList = ArrayList<BaseFragment>(0)
        val addList = ArrayList<FragWithTag>(0)
        val showList = ArrayList<BaseFragment>(0)
        val hideList = ArrayList<BaseFragment>(0)

        val previousState = stateChange.getPreviousState<BaseKey>()
        val newState = stateChange.getNewState<BaseKey>()

        val animationSet = if (stateChange.direction == StateChange.FORWARD)
            stateChange.topNewState<BaseKey>().getAnimation()
        else
            stateChange.topPreviousState<BaseKey>()?.getBackAnimation() ?: FragAnimPair(0, 0)

        previousState.forEach { oldKey ->
            fragmentManager.findFragmentByTag(oldKey.fragmentTag)?.also {
                if (!newState.contains(oldKey)) { removeList.add(it as BaseFragment) }
                else if (!it.isHidden) { hideList.add(it as BaseFragment) }
            }
        }
        newState.forEach { newKey ->
            var fragment: androidx.fragment.app.Fragment? = fragmentManager.findFragmentByTag(newKey.fragmentTag)
            if (newKey == stateChange.topNewState<Any>()) {
                if (fragment != null) {
                    if (fragment.isHidden) showList.add(fragment as BaseFragment)
                } else {
                    fragment = newKey.newFragment()
                    addList.add(FragWithTag(fragment as BaseFragment, newKey.fragmentTag))
                }
            } else {
                if (fragment != null && !fragment.isHidden) {
                    hideList.add(fragment as BaseFragment)
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

        addList.forEach { it.fragment.renderInitialState() }
    }
}