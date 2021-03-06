package com.febers.uestc_bbs.io

import android.os.Looper

import com.bumptech.glide.Glide
import com.febers.uestc_bbs.MyApp
import com.febers.uestc_bbs.io.FileHelper.appApkDir
import com.febers.uestc_bbs.io.FileHelper.appCacheDir
import com.febers.uestc_bbs.io.FileHelper.appImageDir
import com.febers.uestc_bbs.io.FileHelper.appImageDir2
import com.febers.uestc_bbs.io.FileHelper.deleteFolderFile
import com.febers.uestc_bbs.io.FileHelper.getFolderSize
import com.febers.uestc_bbs.io.FileHelper.getFormatSize
import com.febers.uestc_bbs.io.FileHelper.glideCacheDir

import java.io.File

/**
 * 提供清除缓存功能
 */
object CacheManager {

    /**
     * 获取图片缓存大小
     * 包括Glide的缓存和Android/data/uestc_bbs/cache中的文件
     */
    val CacheSize: String
        get() {
            return try {
                getFormatSize(size = getFolderSize(File(glideCacheDir)).toDouble()
                        + getFolderSize(File(appCacheDir)).toDouble()
                        + getFolderSize(File(appApkDir)).toDouble()
                        + getFolderSize(File(appImageDir)).toDouble()
                        + getFolderSize(File(appImageDir2)).toDouble())
            } catch (e: Exception) {
                e.printStackTrace()
                "获取失败"
            }
        }

    // 清除磁盘缓存，自己获取缓存文件夹并删除方法
    fun clearCache(): Boolean {
        if (deleteFolderFile(glideCacheDir)
                && deleteFolderFile(appCacheDir)
                && deleteFolderFile(appApkDir)
                && deleteFolderFile(appImageDir)
                && deleteFolderFile(appImageDir2)) {
            return true
        }
        return false
    }

    // 清除图片磁盘缓存，调用Glide自带方法
    fun clearGlideCache(): Boolean {
        return try {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                Thread(Runnable {
                    Glide.get(MyApp.context()).clearDiskCache()
                }).start()
            } else {
                Glide.get(MyApp.context()).clearDiskCache()
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}