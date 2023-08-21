package com.example.marvelheroes.utils

import android.content.Context
import android.util.Log
import java.math.BigInteger
import java.security.MessageDigest

import android.content.SharedPreferences
import android.content.res.Configuration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager


object Helper {

    fun md5(param: String): String {
        val md = MessageDigest.getInstance("MD5")
        return BigInteger(1, md.digest(param.toByteArray())).toString(16).padStart(32, '0')

    }

    /**
     * Checking orientation and setting grid or list view
     */
    /*private fun setupLayoutManager(context: Context) {

        when (context.resources.configuration.orientation) {
            Configuration.ORIENTATION_LANDSCAPE -> {
                when {
                    screenInches!! >= 9.5 -> {
                        binding.marvelCharList.layoutManager = GridLayoutManager(context, 4)
                    }
                    screenInches!! >= 7.5 -> {
                        binding.marvelCharList.layoutManager = GridLayoutManager(context, 3)
                    }
                    screenInches!! >= 6.5 -> {
                        binding.marvelCharList.layoutManager = GridLayoutManager(context, 2)
                    }
                    else -> {
                        binding.marvelCharList.layoutManager = LinearLayoutManager(context)
                    }
                }
            }
            else -> {
                when {
                    screenInches!! >= 6.5 -> {
                        binding.marvelCharList.layoutManager = GridLayoutManager(context, 2)
                    }
                    else -> {
                        binding.marvelCharList.layoutManager = LinearLayoutManager(context)
                    }
                }
            }
        }
    }*/

}