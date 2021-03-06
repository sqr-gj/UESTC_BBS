package com.febers.uestc_bbs.home

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkRequest
import android.os.Build
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.febers.uestc_bbs.MyApp
import com.febers.uestc_bbs.R
import com.febers.uestc_bbs.base.*
import com.febers.uestc_bbs.entity.GithubReleaseBean
import com.febers.uestc_bbs.entity.MoreItemBean
import com.febers.uestc_bbs.entity.PushMessageBean
import com.febers.uestc_bbs.entity.UserSimpleBean
import com.febers.uestc_bbs.module.context.ClickContext
import com.febers.uestc_bbs.module.context.LoginContext
import com.febers.uestc_bbs.module.image.ImageLoader
import com.febers.uestc_bbs.module.more.*
import com.febers.uestc_bbs.module.post.view.PListActivity
import com.febers.uestc_bbs.module.search.view.SearchActivity
import com.febers.uestc_bbs.module.service.HeartMsgService
import com.febers.uestc_bbs.module.service.NetworkCallbackImpl
import com.febers.uestc_bbs.module.setting.AboutActivity
import com.febers.uestc_bbs.module.setting.AccountActivity
import com.febers.uestc_bbs.module.setting.SettingActivity
import com.febers.uestc_bbs.module.setting.push.PushManager
import com.febers.uestc_bbs.module.setting.push.PushMessageListener
import com.febers.uestc_bbs.module.theme.ThemeActivity
import com.febers.uestc_bbs.module.theme.ThemeManager
import com.febers.uestc_bbs.module.update.UpdateDialogHelper
import com.febers.uestc_bbs.module.user.view.UserHistoryActivity
import com.febers.uestc_bbs.module.user.view.UserPostActivity
import com.febers.uestc_bbs.utils.PreferenceUtils
import com.febers.uestc_bbs.utils.colorAccent
import com.febers.uestc_bbs.utils.postSticky
import com.febers.uestc_bbs.view.adapter.MoreItemAdapter
import com.febers.uestc_bbs.view.dialog.PushMessageDialog
import kotlinx.android.synthetic.main.activity_home_2.*
import kotlinx.android.synthetic.main.activity_home_2.fab_home
import kotlinx.android.synthetic.main.layout_drawer_home_2.*
import kotlinx.android.synthetic.main.layout_header_drawer.*
import kotlinx.android.synthetic.main.layout_toolbar_common.*
import me.yokeyword.fragmentation.ISupportFragment
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class HomeActivity2: BaseActivity() {

    private lateinit var networkCallback: ConnectivityManager.NetworkCallback
    private lateinit var connectivityManager: ConnectivityManager

    private var mFragments : MutableList<ISupportFragment> = ArrayList()
    private var pagePositionNow = PAGE_POSITION_HOME

    private var navigationDialog: AlertDialog? = null
    private var tvNaviLostAndFound: LinearLayout? = null
    private var tvNaviSchoolBus: LinearLayout? = null
    private var tvNaviCalendar: LinearLayout? = null
    private var tvNaviNewer: LinearLayout? = null
    private var btnNaviEnter: Button? = null

    private var msgMenuItem: MenuItem? = null

    private var actionAfterDrawerClose: ()->Unit = { }

    override fun setView(): Int = R.layout.activity_home_2

    override fun setToolbar(): Toolbar? = toolbar_common

    override fun registerEventBus(): Boolean = true

    override fun initView() {
        setSwipeBackEnable(false)
        initToolbar()
        val firstFragment: ISupportFragment? = findFragment(HomeFirstContainer::class.java)
        if (firstFragment == null) {
            with(mFragments) {
                add(PAGE_POSITION_HOME, HomeFirstContainer())
                add(PAGE_POSITION_BLOCK, HomeSecondContainer())
                add(PAGE_POSITION_MESSAGE, HomeThirdContainer())
            }
            loadMultipleRootFragment(R.id.container_home_2, 0,
                    mFragments[0], mFragments[1], mFragments[2])
        } else {
            with(mFragments) {
                add(PAGE_POSITION_HOME, firstFragment)
                add(PAGE_POSITION_BLOCK, findFragment(HomeSecondContainer::class.java))
                add(PAGE_POSITION_MESSAGE, findFragment(HomeThirdContainer::class.java))
            }
        }
        ThemeManager.viewInitAndSubscribe(fab_home)
        fab_home.setOnClickListener {
            ClickContext.clickToPostEdit(ctx, fid = 0, title = "") }
        initDrawer()
        startService()
        getShortcutMsg()
    }

    private fun initToolbar() {
        toolbar_common.title = getString(R.string.home_page)
        val toggle = ActionBarDrawerToggle(ctx,
                drawer_layout_home_2, toolbar_common,
                R.string.search, R.string.search)
        toggle.syncState()
        drawer_layout_home_2.addDrawerListener(toggle)
    }

    private fun initDrawer() {
        initDrawerHeader(MyApp.user())
        drawer_header_home.setOnClickListener {
            drawer_layout_home_2.closeDrawers()
            actionAfterDrawerClose = {
                ClickContext.clickToUserDetail(ctx, MyApp.user().uid)
            }
        }

        val menuItemAdapter1 = MoreItemAdapter(ctx, initDrawerItem1(), false)
        menuItemAdapter1.setOnItemClickListener { p0, p1, p2 -> onFirstItemClick(position = p2) }
        recycler_view_drawer_1.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = menuItemAdapter1
        }

        val menuItemAdapter2 = MoreItemAdapter(ctx, initDrawerItem2(), false).apply {
            setOnItemClickListener { p0, p1, p2 -> onSecondItemClick(position = p2) }
            setOnItemChildClickListener(R.id.switch_more_item) {
                viewHolder, moreItemBean, i ->
                drawer_layout_home_2.closeDrawers()
                actionAfterDrawerClose = {
                    ThemeManager.dayAndNightThemeChange(ctx)
                }
            }
        }
        recycler_view_drawer_2.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = menuItemAdapter2
        }
        drawer_layout_home_2.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerStateChanged(newState: Int) { }
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) { }
            override fun onDrawerOpened(draweriew: View) { }
            override fun onDrawerClosed(drawerView: View) {
                actionAfterDrawerClose.invoke()
                actionAfterDrawerClose = { }
            }
        })
    }

    private fun initDrawerHeader(user: UserSimpleBean) {
        if (user.valid) {
            tv_user_name_drawer_header.text = user.name
            tv_user_level_drawer_header.text = user.title
            tv_user_name_drawer_header.setTextColor(ThemeManager.colorRefreshText())
            tv_user_level_drawer_header.setTextColor(ThemeManager.colorRefreshText())
            ImageLoader.load(ctx, user.avatar, iv_user_avatar_drawer_header, clickToViewer = false)
        } else {
            tv_user_name_drawer_header.text = getString(R.string.please_login_or_sign_up)
            tv_user_level_drawer_header.text = " "
            ImageLoader.loadResource(ctx, R.drawable.ic_default_avatar_circle, iv_user_avatar_drawer_header, isCircle = true)
        }
    }

    private fun initDrawerItem1(): List<MoreItemBean> {
        val itemStart = MoreItemBean(getString(R.string.my_start_post), R.drawable.xic_edit_blue_24dp)
        val itemReply = MoreItemBean(getString(R.string.my_reply_post), R.drawable.xic_reply_red_24dp)
        val itemFav = MoreItemBean(getString(R.string.my_fav_post), R.drawable.xic_star_border_teal_24dp)
        val itemHistory = MoreItemBean(getString(R.string.browsing_history), R.drawable.xic_history_purple)
        return listOf(itemStart, itemReply, itemFav)
    }

    private fun initDrawerItem2(): List<MoreItemBean> {
        val itemNav = MoreItemBean(getString(R.string.bbs_navigation), R.drawable.xic_navigation_blue_24dp)
        val itemTheme = MoreItemBean(getString(R.string.theme_style), R.drawable.xic_style_pink_24dp, showSwitch = true, isCheck = ThemeManager.isNightTheme())
        val itemAccount = MoreItemBean(getString(R.string.account), R.drawable.xic_person_blue_24dp)
        val itemSetting = MoreItemBean(getString(R.string.setting), R.drawable.ic_setting_gray)
        val itemAbout = MoreItemBean(getString(R.string.about), R.drawable.xic_emot_blue_24dp)
        return listOf(itemNav, itemTheme, itemAccount, itemSetting, itemAbout)
    }

    private fun onFirstItemClick(position: Int) {
        if (!LoginContext.userState(ctx)) return
        drawer_layout_home_2.closeDrawers()
        actionAfterDrawerClose =  when(position) {
            USER_POST_ITEM -> {
                {
                    startActivity(Intent(ctx, UserPostActivity::class.java).apply {
                        putExtra(USER_ID, MyApp.user().uid)
                        putExtra(USER_POST_TYPE, USER_START_POST) })
                }
            }
            USER_REPLY_ITEM -> {
                {
                    startActivity(Intent(ctx, UserPostActivity::class.java).apply {
                        putExtra(USER_ID, MyApp.user().uid)
                        putExtra(USER_POST_TYPE, USER_REPLY_POST) })
                }
            }
            USER_FAV_ITEM -> {
                {
                    startActivity(Intent(ctx, UserPostActivity::class.java).apply {
                        putExtra(USER_ID, MyApp.user().uid)
                        putExtra(USER_POST_TYPE, USER_FAV_POST) })
                }
            }
            USER_HISTORY_ITEM -> {
                { startActivity(Intent(ctx, UserHistoryActivity::class.java)) }
            }
            else -> { { } }
        }
    }

    private fun onSecondItemClick(position: Int) {
        drawer_layout_home_2.closeDrawers()
        actionAfterDrawerClose = when(position) {
            NAVIGATION_ITEM -> {
                { showNavigationDialog() }
            }
            THEME_ITEM -> {
                { startActivity(Intent(ctx, ThemeActivity::class.java)) }
            }
            ACCOUNT_ITEM -> {
                { startActivity(Intent(ctx, AccountActivity::class.java)) }
            }
            SETTING_ITEM -> {
                { startActivity(Intent(ctx, SettingActivity::class.java)) }
            }
            ABOUT_ITEM -> {
                { startActivity(Intent(ctx, AboutActivity::class.java)) }
            }
            else -> { { } }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.menu_item_search_home_2 -> { startActivity(Intent(ctx, SearchActivity::class.java)) }
            R.id.menu_item_block_home_2 -> {
                pagePositionNow = if (pagePositionNow == PAGE_POSITION_BLOCK) {
                    showHideFragment(mFragments[PAGE_POSITION_HOME])
                    toolbar_common.title = getString(R.string.home_page)
                    PAGE_POSITION_HOME
                } else {
                    showHideFragment(mFragments[PAGE_POSITION_BLOCK])
                    toolbar_common.title = getString(R.string.forum_list_page)
                    PAGE_POSITION_BLOCK
                }
            }
            R.id.menu_item_msg_home_2 -> {
                pagePositionNow = if (pagePositionNow == PAGE_POSITION_MESSAGE) {
                    showHideFragment(mFragments[PAGE_POSITION_HOME])
                    toolbar_common.title = getString(R.string.home_page)
                    PAGE_POSITION_HOME
                } else {
                    msgMenuItem?.icon = ctx.resources.getDrawable(R.drawable.xic_menu_msg)
                    showHideFragment(mFragments[PAGE_POSITION_MESSAGE])
                    toolbar_common.title = getString(R.string.message_page)
                    PAGE_POSITION_MESSAGE
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressedSupport() {
        if (pagePositionNow != PAGE_POSITION_HOME) {
            showHideFragment(mFragments[PAGE_POSITION_HOME])
            toolbar_common.title = getString(R.string.home_page)
            pagePositionNow = PAGE_POSITION_HOME
        } else {
            ActivityCompat.finishAfterTransition(this)
        }
        //super.onBackPressedSupport()
    }

    /**
     *  当后台Service接收到新消息时，此方法会接受到相应的消息
     *  接收到一个msgCount的参数，代表未读消息的数目
     */
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = false)
    fun onReceiveNewMsg(event: MsgEvent) {
        if (pagePositionNow != PAGE_POSITION_MESSAGE) {
            msgMenuItem?.icon = ctx.resources.getDrawable(R.drawable.xic_msg_notice_white_24dp)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onUserUpdate(event: UserUpdateEvent) {
        if (event.user.valid) {
            tv_user_name_drawer_header.text = event.user.name
            tv_user_level_drawer_header.text = event.user.title
            ImageLoader.load(ctx, event.user.avatar, iv_user_avatar_drawer_header, clickToViewer = false)
        } else {
            tv_user_name_drawer_header.text = getString(R.string.please_login_or_sign_up)
            tv_user_level_drawer_header.text = " "
            ImageLoader.loadResource(ctx, R.drawable.ic_default_avatar_circle, iv_user_avatar_drawer_header, isCircle = true)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_home_2, menu)
        msgMenuItem = menu?.findItem(R.id.menu_item_msg_home_2)
        return true
    }

    /**
     * Activity通过Intent的FLAG_ACTIVITY_SINGLE_TOP启动时，即通过通知打开应用时
     * 生命周期为 onNewIntent -> onResume()
     * 在其中接收Service传过来的数据
     *
     * 暂时失效，下版本修复
     */
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent?.getIntExtra(MSG_COUNT, 0) ?: 0 > 0) {
            showHideFragment(mFragments[PAGE_POSITION_MESSAGE])
        }
        getShortcutMsg()
    }

    private fun showNavigationDialog() {
        if (navigationDialog == null) {
            navigationDialog = AlertDialog.Builder(ctx)
                    .create()
        }
        navigationDialog?.show()
        navigationDialog?.setContentView(getNavigationDialogView())
        tvNaviLostAndFound?.setOnClickListener {
            startActivity(Intent(ctx, PListActivity::class.java).apply {
                putExtra(FID, 305)
                putExtra(TITLE, "失物招领")
            })
            navigationDialog?.dismiss()
        }
        tvNaviSchoolBus?.setOnClickListener {
            ClickContext.clickToPostDetail(ctx, 1430861)
            navigationDialog?.dismiss()
        }
        tvNaviCalendar?.setOnClickListener {
            ClickContext.clickToPostDetail(ctx, 1493930)
            navigationDialog?.dismiss()
        }
        tvNaviNewer?.setOnClickListener {
            ClickContext.clickToPostDetail(ctx, 1456557)
            navigationDialog?.dismiss()
        }
        btnNaviEnter?.setOnClickListener {
            navigationDialog?.dismiss()
        }
    }

    private fun getNavigationDialogView(): View {
        val view = LayoutInflater.from(ctx).inflate(R.layout.dialog_navigation, null)
        tvNaviLostAndFound = view.findViewById(R.id.navigation_lost_and_found)
        tvNaviSchoolBus = view.findViewById(R.id.navigation_school_bus)
        tvNaviCalendar = view.findViewById(R.id.navigation_calendar)
        tvNaviNewer = view.findViewById(R.id.navigation_novice)
        btnNaviEnter = view.findViewById(R.id.navigation_enter)
        btnNaviEnter?.setTextColor(colorAccent())
        return view
    }

    override fun onDestroy() {
        super.onDestroy()
        stopService(Intent(this, HeartMsgService::class.java))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        }
    }

    private fun startService() {
        val loopReceiveMsg by PreferenceUtils(MyApp.context(), LOOP_RECEIVE_MSG, true)
        if (loopReceiveMsg) {
            val intent = Intent(this, HeartMsgService::class.java)
            startService(intent)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            networkCallback = NetworkCallbackImpl()
            connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            connectivityManager.registerNetworkCallback(NetworkRequest.Builder().build(), networkCallback)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN) fun onThemeChanged(event: ThemeChangedEvent) {
        tv_user_name_drawer_header.setTextColor(ThemeManager.colorRefreshText())
        tv_user_level_drawer_header.setTextColor(ThemeManager.colorRefreshText())
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onCheckUpdateResult(event: UpdateCheckEvent) {
        if (event.code == BaseCode.SUCCESS) {
            showUpdateDialog(event.result!!)
        }
    }

    private fun showUpdateDialog(githubReleaseBean: GithubReleaseBean) {
        val dialogHelper = UpdateDialogHelper(ctx)
        dialogHelper.showGithubUpdateDialog(githubReleaseBean)
    }

    private fun getShortcutMsg() {
        if (intent?.getBooleanExtra(SHORTCUT_HOT, false) == true) {
            postSticky(TabSelectedEvent(position = 2))
        }
        if (intent?.getBooleanExtra(SHORTCUT_MSG, false) == true) {
            showHideFragment(mFragments[PAGE_POSITION_MESSAGE])
        }
    }

    override fun afterCreated() {
        PushManager.getHttpMessages(object : PushMessageListener {
            override fun success(message: PushMessageBean) {
                if (!message.msg.isNullOrEmpty()) {
                    if (message.msg!!.first().show) {
                        var lastId by PreferenceUtils(this@HomeActivity2, PUSH_MESSAGE_ID_LAST, 0)
                        if (message.msg!!.first().id > lastId) {
                            runOnUiThread { PushMessageDialog(this@HomeActivity2).show(message.msg!!.first().text) }
                            lastId = message.msg!!.first().id
                        }
                    }
                }
            }
            override fun fail(message: String) {
            }
        })
    }
}