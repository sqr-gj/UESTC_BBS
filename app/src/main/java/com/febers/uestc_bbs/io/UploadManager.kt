package com.febers.uestc_bbs.io

import com.febers.uestc_bbs.base.BaseCode
import com.febers.uestc_bbs.base.BaseEvent
import com.febers.uestc_bbs.base.REQUEST_SUCCESS_RS
import com.febers.uestc_bbs.entity.UploadResultBean
import com.febers.uestc_bbs.http.client.TokenClient
import com.febers.uestc_bbs.utils.ApiUtils
import okhttp3.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.io.File


/**
 * 提供文件上传功能
 */
class FileUploader {

    /**
     * 将图片上传至河畔服务器
     * 如果上传成功，应该返回包含两个值的json bean
     * 第一个 aid 供发帖时调用
     * 第二个 infor 同样用以发帖
     *
     * @param imageFile 图片文件
     */
    fun uploadPostImageToBBS(imageFile: File, listener: OnFileUploadListener) {
        Thread {
            listener.onUploading("正在上传")
            val imageBody: RequestBody = RequestBody.create(MediaType.parse("image/png"), imageFile)
            val imagePart: MultipartBody.Part = MultipartBody.Part.createFormData("uploadFile[]", imageFile.name, imageBody)

            getRetrofit().create(UploadToBBSInterface::class.java)
                    .uploadPostImage(image = imagePart)
                    .enqueue(object : Callback<UploadResultBean> {
                        override fun onFailure(call: Call<UploadResultBean>, t: Throwable) {
                            listener.onUploadFail("Upload Image Fail:$t")
                        }

                        override fun onResponse(call: Call<UploadResultBean>, response: Response<UploadResultBean>) {
                            val resultBean = response.body()
                            if (resultBean == null || resultBean.rs != REQUEST_SUCCESS_RS || resultBean.body?.attachment?.size!! == 0) {
                                listener.onUploadFail("Upload Image Fail:" + resultBean?.head?.errInfo)
                                return
                            }
                            listener.onUploadSuccess(BaseEvent(BaseCode.SUCCESS, resultBean))
                        }
                    })
        }.start()
    }

    /**
     * 批量上传图片到服务器
     * 待实现
     */
    fun uploadPostImagesToBBS(imageFiles: List<File>, listener: OnFileUploadListener) {
        Thread {
        }.start()
    }

    private fun getRetrofit(): Retrofit = Retrofit.Builder()
            .baseUrl(ApiUtils.BBS_BASE_URL)
            .client(TokenClient.get())
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    interface OnFileUploadListener {
        fun onUploadFail(msg: String)
        fun onUploadSuccess(event: BaseEvent<UploadResultBean>)
        fun onUploading(msg: String)
    }
}

interface UploadToBBSInterface {

    @Multipart
    @POST(ApiUtils.BBS_SEND_POST_IMAGE_URL)
    fun uploadPostImage(@Part image: MultipartBody.Part) : Call<UploadResultBean>

}