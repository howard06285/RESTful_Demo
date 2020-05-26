package com.shigaga.restfuldemo.data

import android.content.SharedPreferences


/**
 * 用來輔助處理 [SharedPreferences] 的 Helper 類別.
 */
class SharedPreferencesHelper
/**
 * Constructor with dependency injection.
 *
 * @param mSharedPreferences
 * The injected SharedPreferences implementation to use for persistence.
 *
 */(
    private val mSharedPreferences: SharedPreferences
) {

    companion object {
        //儲存 [是否為初啟動] 數值的 KEY 值
        const val SP_KEY_IS_FIRST_RUN = "is_first_run"
    }

    /**
     * Retrieves the first run flag from [SharedPreferences].
     *
     * @return [SP_KEY_IS_FIRST_RUN] value.
     */
    val getIsFirstRunValue: Boolean
        get() {
            // Get data from the SharedPreferences.
            return  mSharedPreferences.getBoolean(
                SP_KEY_IS_FIRST_RUN,
                true
            )
        }

    fun setIsFirstRunValue(IsFirstRun: Boolean): Boolean {
        // Start a SharedPreferences transaction.
        val editor = mSharedPreferences.edit()
        editor.putBoolean(
            SP_KEY_IS_FIRST_RUN,
            IsFirstRun
        )
        // Commit changes to SharedPreferences.
        return editor.commit()
    }
}
