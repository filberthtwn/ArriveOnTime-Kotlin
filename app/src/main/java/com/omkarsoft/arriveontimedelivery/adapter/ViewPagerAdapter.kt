package com.omkarsoft.arriveontimedelivery.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class ViewPagerAdapter(manager: FragmentManager): FragmentPagerAdapter(manager) {

    private val fragmentList: MutableList<Fragment> = arrayListOf()
    private val titleList: MutableList<String> = arrayListOf()

    override fun getCount(): Int = fragmentList.count()

    override fun getItem(position: Int): Fragment = fragmentList[position]

    override fun getPageTitle(position: Int): CharSequence = titleList[position]

    fun addFragment(fragment: Fragment, title: String){
        fragmentList.add(fragment)
        titleList.add(title)
    }

    fun resetFragment(){
        fragmentList.clear()
        titleList.clear()
    }
}