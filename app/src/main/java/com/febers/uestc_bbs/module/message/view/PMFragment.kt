/*
 * Created by Febers at 18-8-14 上午1:45.
 * Copyright (c). All rights reserved.
 * Last modified 18-8-14 上午1:45.
 */

package com.febers.uestc_bbs.module.message.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.febers.uestc_bbs.R
import com.febers.uestc_bbs.base.BaseFragment
import me.yokeyword.fragmentation.SupportFragment

/**
 * 私信通知
 */
class PMFragment : SupportFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_sub_message, container, false)
        return view
    }

    override fun onLazyInitView(savedInstanceState: Bundle?) {
        super.onLazyInitView(savedInstanceState)
    }
}