package com.febers.uestc_bbs.io

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import java.text.SimpleDateFormat
import java.util.*
import java.io.*
import java.lang.StringBuilder
import java.math.BigDecimal


object FileHelper {

    /**
     * 获取Assets文件夹中文件的String值
     * 原先在其中放置了开源相关的html文件用于展示
     * 后来由于效果不好，就采用了RecyclerView
     * @param context
     * @param fileName 直接使用文件名(包含后缀)
     */
    fun getAssetsString(context: Context, fileName: String): String {
        try {
            val inputReader = InputStreamReader(
                    context.assets.open(fileName))
            val bufReader = BufferedReader(inputReader)
            val result = StringBuilder()
            var line: String? = null
            do {
                line?.let {
                    result.append(it)
                }
                line = bufReader.readLine()
            } while (line != null)
            return result.toString()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return "error"
    }

    /**
     * 将Uri路径转换成文件
     * @param uri
     * @param context
     */
    fun uriToFile(uri: Uri, context: Context): File? {
        var path: String? = null
        if ("file" == uri.scheme) {
            path = uri.encodedPath
            if (path != null) {
                path = Uri.decode(path)
                val cr = context.contentResolver
                val buff = StringBuffer()
                buff.append("(").append(MediaStore.Images.ImageColumns.DATA).append("=").append("'$path'").append(")")
                val cur = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        arrayOf(MediaStore.Images.ImageColumns._ID, MediaStore.Images.ImageColumns.DATA),
                        buff.toString(), null, null)
                var index = 0
                var dataIdx = 0
                cur!!.moveToFirst()
                while (!cur.isAfterLast) {
                    index = cur.getColumnIndex(MediaStore.Images.ImageColumns._ID)
                    index = cur.getInt(index)
                    dataIdx = cur.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
                    path = cur.getString(dataIdx)
                    cur.moveToNext()
                }
                cur.close()
                if (index == 0) {

                } else {
                    val u = Uri.parse("content://media/external/images/media/$index")
                    println("temp uri is :$u")
                }
            }
            if (path != null) {
                return File(path)
            }
        } else if ("content" == uri.scheme) {
            // 4.2.2以后
            val projections = arrayOf(MediaStore.Images.Media.DATA)
            val cursor = context.contentResolver.query(uri, projections, null, null, null)
            if (cursor!!.moveToFirst()) {
                val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                path = cursor.getString(columnIndex)
            }
            cursor.close()
            return File(path)
        } else {
            //i("Uri", uri.scheme)
        }
        return null
    }

    /**
     * 获取xml资源文件的Uri
     * @param context
     * @param id
     */
    fun getResourceUri(context: Context, id: Int): String = ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
            context.resources.getResourcePackageName(id) + "/" +
            context.resources.getResourceTypeName(id) + "/" +
            context.resources.getResourceEntryName(id)

    // 格式化单位
    fun getFormatSize(size: Double): String {
        val kiloByte = size / 1024
        if (kiloByte < 1) {
            return size.toString() + "Byte"
        }
        val megaByte = kiloByte / 1024
        if (megaByte < 1) {
            val result1 = BigDecimal(java.lang.Double.toString(kiloByte))
            return result1.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "KB"
        }
        val gigaByte = megaByte / 1024
        if (gigaByte < 1) {
            val result2 = BigDecimal(java.lang.Double.toString(megaByte))
            return result2.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "MB"
        }
        val teraBytes = gigaByte / 1024
        if (teraBytes < 1) {
            val result3 = BigDecimal(java.lang.Double.toString(gigaByte))
            return result3.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "GB"
        }
        val result4 = BigDecimal(teraBytes)
        return result4.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "TB"
    }

    // 按目录删除文件夹文件方法
    fun deleteFolderFile(filePath: String, deleteThisPath: Boolean): Boolean {
        try {
            val file = File(filePath)
            if (file.isDirectory) {
                val files = file.listFiles()
                for (file1 in files) {
                    deleteFolderFile(file1.absolutePath, true)
                }
            }
            if (deleteThisPath) {
                if (!file.isDirectory) {
                    file.delete()
                } else {
                    if (file.listFiles().isEmpty()) {
                        file.delete()
                    }
                }
            }
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }
}