package com.ripenapps.adoreandroid.preferences

import android.content.ContentValues.TAG
import android.content.Context
import android.content.SharedPreferences
import com.ripenapps.adoreandroid.models.static_models.SavedAddressModel
import com.google.gson.reflect.TypeToken
import com.google.gson.Gson

object Preferences {
    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(TAG, Context.MODE_PRIVATE)
    }

    fun setStringPreference(context: Context?, key: String?, value: String?) {
        val settings: SharedPreferences = getSharedPreferences(context!!)
        val editor = settings.edit()
        editor.putString(key, value)
        editor.commit()
    }

    fun setIntegerPreference(context: Context?, key: String?, value: Int) {
        val settings: SharedPreferences = getSharedPreferences(context!!)
        val editor = settings.edit()
        editor.putInt(key, value)
        editor.commit()
    }

    fun getStringPreference(context: Context?, key: String?): String? {
        val pref: SharedPreferences = getSharedPreferences(context!!)
        return pref.getString(key, "")
    }

    fun removePreference(context: Context, key: String?) {
        val settings: SharedPreferences =
            getSharedPreferences(context)
        val editor = settings.edit()
        editor.remove(key)
        editor.commit()
    }

    fun removeAllPreference(context: Context) {
        val settings: SharedPreferences = getSharedPreferences(context)
        val editor = settings.edit()
        editor.clear()
        editor.commit()
    }
    fun setStringListPreference(context: Context?, key: String?, value: MutableList<String>?) {
        val settings: SharedPreferences = getSharedPreferences(context!!)
        val editor = settings.edit()

        // Convert the list of strings to a JSON string
        val jsonString = Gson().toJson(value)
        editor.putString(key, jsonString)

        editor.apply()
    }

    fun getStringListPreference(context: Context?, key: String?): MutableList<String>? {
        val pref: SharedPreferences = getSharedPreferences(context!!)
        val jsonString = pref.getString(key, null)

        // Convert the JSON string back to a list of strings
        return Gson().fromJson(jsonString, object : TypeToken<MutableList<String>>() {}.type)
    }
    /*fun setObjectListPreference(context: Context?, key: String?, value: Any?) {
        val settings: SharedPreferences = getSharedPreferences(context!!)
        val editor = settings.edit()

        // Convert the object to a JSON string
        val jsonString = Gson().toJson(value)
        editor.putString(key, jsonString)

        editor.apply()
    }*/

   /* fun getObjectListPreference(context: Context?, key: String?): Any? {
        val pref: SharedPreferences = getSharedPreferences(context!!)
        val jsonString = pref.getString(key, null)

        // Convert the JSON string back to an object
        return Gson().fromJson(jsonString, Any::class.java)
    }*/

    fun setObjectListPreference(context:Context, key: String?,addressList: List<SavedAddressModel>) {
        val settings: SharedPreferences = getSharedPreferences(context!!)
        val json = Gson().toJson(addressList)
        settings.edit().putString(key, json).apply()
    }

    fun getObjectListPreference(context:Context, key: String?): List<SavedAddressModel> {
        val settings: SharedPreferences = getSharedPreferences(context!!)
        val json = settings.getString(key, null)
        val type = object : TypeToken<List<SavedAddressModel>>() {}.type
        return Gson().fromJson(json, type) ?: emptyList()
    }

}