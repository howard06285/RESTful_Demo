package com.shigaga.restfuldemo.ui.main_page

import android.app.Application
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import com.shigaga.restfuldemo.data.SharedPreferencesHelper
import com.shigaga.restfuldemo.data.Weather
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URL
import java.net.UnknownHostException


class MainViewModel(application: Application) : AndroidViewModel(application) {

    companion object {

        private val TAG: String = MainViewModel::class.java.simpleName

        private const val TargetUrl =
            "https://opendata.cwb.gov.tw/api/v1/rest/datastore/F-C0032-001?" +
                    "Authorization=CWB-9172A807-C96D-4EA7-9580-AD8F2031803F&" +
                    "format=JSON&locationName=臺北市&elementName=MinT"

        private const val MY_PREFERENCES = "MyPreferences"

        private const val JSON_TAG_RECORDS = "records"
        private const val JSON_TAG_LOCATION = "location"
        private const val JSON_TAG_WEATHER_ELEMENT = "weatherElement"
        private const val JSON_TAG_MIN_TEMP = "MinT"
        private const val JSON_TAG_ELEMENT_NAME = "elementName"
        private const val JSON_TAG_TIME = "time"

        private const val ERR_NO_DATA_FOUND = "Oops, 似乎沒有資料.."
    }

    private var sharedPreferences: SharedPreferences =
        application.getSharedPreferences(MY_PREFERENCES, MODE_PRIVATE)

    /* 用來管理 SharedPreferences 的 helper */
    private val mSharedPreferencesHelper: SharedPreferencesHelper =
        SharedPreferencesHelper(sharedPreferences)

    /* 判斷是否已顯示過歡迎詞 */
    private var alreadyShown: Boolean = false

    /* 伺服器回傳 天氣資訊LiveData */
    var weathersLiveData: MutableLiveData<ArrayList<Weather?>> = MutableLiveData()
    var toastWithStrInputLiveData: MutableLiveData<String> = MutableLiveData()
    var toastForGreetingLiveData: MutableLiveData<Boolean> = MutableLiveData()

    override fun onCleared() {
        alreadyShown = false
        super.onCleared()
    }


    /** 從 SharedPreferences 抓取 [ 是否為第一次啟動 ] 值 */
    private fun getIsFirstRunValue() =
        mSharedPreferencesHelper.getIsFirstRunValue

    /** 初次啟動後改寫 SharedPreferences 內 [ 是否為第一次啟動 ] 為 false */
    private fun setIsFirstRunValueToFalse() =
        mSharedPreferencesHelper.setIsFirstRunValue(false)


    /** 透過網路請求向伺服器索取資料 */
    fun fetchDataFromOpenData() {

        viewModelScope.launch(Dispatchers.IO) {

            /* 無網路時網路請求、資料為空時 -> 拋出例外 */
            try {
                val targetUrl = URL(TargetUrl)

                val client = OkHttpClient.Builder().build()

                val req = Request.Builder().url(targetUrl).build()

                val response = client.newCall(req).execute()

                response.body?.run {
                    parseJson(string())
                }

                toastWithStrInputLiveData.postValue(null)

            } catch (e: UnknownHostException) {
                e.stackTrace
                toastWithStrInputLiveData.postValue(e.message)

            } catch (e: ArrayIndexOutOfBoundsException) {
                e.stackTrace
                toastWithStrInputLiveData.postValue(ERR_NO_DATA_FOUND)
            }
        }
    }

    /** 解析 JSON */
    private fun parseJson(json: String) {

        val jsonObj = JsonParser.parseString(json).asJsonObject

        val record = jsonObj.get(JSON_TAG_RECORDS).asJsonObject

        val location = record.get(JSON_TAG_LOCATION).asJsonArray

        location.forEach {

            val weatherElement = it.asJsonObject.get(JSON_TAG_WEATHER_ELEMENT).asJsonArray

            weatherElement.forEach { it0 ->

                if (JSON_TAG_MIN_TEMP == it0.asJsonObject.get(JSON_TAG_ELEMENT_NAME).asString) {

                    val time = it0.asJsonObject.get(JSON_TAG_TIME)

                    val weatherResult = Gson().fromJson<ArrayList<Weather>>(time,
                        object : TypeToken<ArrayList<Weather>>() {}.type
                    )

                    val weatherList: ArrayList<Weather?> = ArrayList()

                    weatherList.clear()

                    weatherResult.forEach { it3 ->
                        weatherList.add(it3)
                        /** 每筆資料後方都補一空格 (Type B) */
                        weatherList.add(null)
                    }

                    /** 去除清單最後一比資料（空白項），條件需求為「兩筆資料之間」插入圖片 */
                    weatherList.removeAt(weatherList.size - 1)

                    weathersLiveData.postValue(weatherList)
                }
            }
        }
    }

    /** 若有需要就顯示 歡迎回來 對話氣泡 */
    fun showGreetingToastIfNeed() {

        viewModelScope.launch {

            toastForGreetingLiveData.postValue(!alreadyShown && !getIsFirstRunValue())

            if (!alreadyShown || getIsFirstRunValue())
                alreadyShown = true

            setIsFirstRunValueToFalse()
        }
    }

    /** 將 alreadyShown 設為 true、toastForGreetingLiveData 改寫，防止歡迎詞重複顯示  */
    fun setAlreadyShown() {
        viewModelScope.launch {
            alreadyShown = true
            toastForGreetingLiveData.postValue(!alreadyShown && !getIsFirstRunValue())
        }
    }
}
