package com.febers.uestc_bbs.base

import com.febers.uestc_bbs.entity.GithubReleaseBean


open class BaseEvent<T>(var code: BaseCode, var data: T)

enum class BaseCode {
    SUCCESS, SUCCESS_END, FAILURE, LOCAL, UPDATE, TIME_OUT
}

data class MsgEvent(var code: BaseCode, var count: Int, var type: String)

data class TabReselectedEvent(var code: BaseCode, var position: Int)

data class MsgFeedbackEvent(var code: BaseCode, var type: String)

data class PostNewEvent(var description: String)

data class ThemeChangedEvent(var dayNightChanged: Boolean)

data class UpdateCheckEvent(var code: BaseCode, var hasNewer: Boolean, var result: GithubReleaseBean? = null)

