package leveltool.bubblelevel.level.leveler.adsManager

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.a4glite.R
import com.example.a4glite.AdsImplimentation.TinyDB
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import java.util.*

//shared may bolean store ker hai like this
fun String.printIt() {
    Log.e("-->", this)
}

fun Context.isAppOpenAdShow(showOpenAd: Boolean) =
    TinyDB(this).putBoolean("showOpenAd", showOpenAd)

fun Context.checkAdShow() = TinyDB(this).getBoolean("showOpenAd")

fun Context.isInterAdShow(isInterAdShow: Boolean) =
    TinyDB(this).putBoolean("isInterAdShow", isInterAdShow)

fun Context.checkInterAdShow() = TinyDB(this).getBoolean("isInterAdShow")

class AppOpenAdX(var application: Application) : Application.ActivityLifecycleCallbacks {

    init {
        application.registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                super.onStart(owner)
                "ON_START".printIt()
                if (application.checkAdShow()) {
                    if (!application.checkInterAdShow()) {

                        Handler().postDelayed({
                            showAdIfAvailable()

                        }, 1000)

                    }
                    "showAdIfAvailable".printIt()
                } else {
                    "else".printIt()
                }
                application.isAppOpenAdShow(true)
            }
        })
        showAdIfAvailable()
    }
    interface OnShowAdCompleteListener {
        fun onShowAdComplete()
    }
    private fun Context.getAppOpenKey(): String {
        return resources.getString(R.string.app_open_ad_id)
    }
    var isAdShow = true
    private var currentActivity: Activity? = null

    private var appOpenAd: AppOpenAd? = null
    private var isLoadingAd = false
    var isShowingAd = false

    private var loadTime: Long = 0
    fun loadAd() {
        if (isLoadingAd || isAdAvailable()) {
            return
        }

        isLoadingAd = true
        val request = AdRequest.Builder().build()

        try {
            AppOpenAd.load(
                application,
                application.getAppOpenKey(),
                request,
                AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
                object : AppOpenAd.AppOpenAdLoadCallback() {

                    override fun onAdLoaded(ad: AppOpenAd) {
                        appOpenAd = ad
                        isLoadingAd = false
                        loadTime = Date().time
                        "OpenAdLoaded".printIt()
                    }


                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        isLoadingAd = false
                        "OpenAdFailed".printIt()
                    }
                })
        } catch (e: Exception) {

        }

    }

    private fun wasLoadTimeLessThanNHoursAgo(numHours: Long): Boolean {
        val dateDifference: Long = Date().time - loadTime
        val numMilliSecondsPerHour: Long = 3600000
        return dateDifference < numMilliSecondsPerHour * numHours
    }

    private fun isAdAvailable(): Boolean {
        return appOpenAd != null && wasLoadTimeLessThanNHoursAgo(4)
    }

    fun showAdIfAvailable() {
        showAdIfAvailable(
            object : OnShowAdCompleteListener {
                override fun onShowAdComplete() {
                }
            })
    }

    private fun showAdIfAvailable(
        onShowAdCompleteListener: OnShowAdCompleteListener,
    ) {
        if (isShowingAd ) {
            return
        }

        if (!isAdAvailable()) {
            onShowAdCompleteListener.onShowAdComplete()
            loadAd()
            return
        }

        appOpenAd!!.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                isShowingAd = false
                onShowAdCompleteListener.onShowAdComplete()


            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                appOpenAd = null
                isShowingAd = false
                adError.message.printIt()
                onShowAdCompleteListener.onShowAdComplete()
                loadAd()
            }

            override fun onAdShowedFullScreenContent() {
                appOpenAd = null
                loadAd()
            }
        }
        isShowingAd = true
        currentActivity?.let { appOpenAd!!.show(it) }
    }


    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {

    }

    override fun onActivityStarted(activity: Activity) {
        if (!isShowingAd) {
            currentActivity = activity
        }
    }

    override fun onActivityResumed(activity: Activity) {

    }

    override fun onActivityPaused(activity: Activity) {

    }

    override fun onActivityStopped(activity: Activity) {

    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {

    }

    override fun onActivityDestroyed(activity: Activity) {
    }
}