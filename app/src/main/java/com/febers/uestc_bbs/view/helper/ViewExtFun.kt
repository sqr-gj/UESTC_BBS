package com.febers.uestc_bbs.view.helper

import android.graphics.Color
import android.os.Build
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.febers.uestc_bbs.lib.header.MaterialHeader
import com.febers.uestc_bbs.module.theme.ThemeManager
import com.scwang.smartrefresh.layout.SmartRefreshLayout

fun SmartRefreshLayout.finishSuccess() {
    finishRefresh(true)
    finishLoadMore(true)
    setEnableLoadMore(true)
}

fun SmartRefreshLayout.finishFail() {
    finishRefresh(false)
    finishLoadMore(false)
}

fun SmartRefreshLayout.initAttrAndBehavior(subscribeOnThemeChange: Boolean = false) {
    if (subscribeOnThemeChange) {
        ThemeManager.viewInitAndSubscribe(this)
    } else {
        this.setPrimaryColors(ThemeManager.colorAccent(), ThemeManager.colorRefreshText())
        if (this.refreshHeader is MaterialHeader) {
            (this.refreshHeader as MaterialHeader).setColorSchemeColors(ThemeManager.colorAccent())
        }
    }
    setEnableOverScrollBounce(false)
    setEnableLoadMore(false)
    autoRefresh()
}

fun AppCompatActivity.hideStatusBar() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        val option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        window.decorView.systemUiVisibility = option
        window.statusBarColor = Color.TRANSPARENT
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
    }
}