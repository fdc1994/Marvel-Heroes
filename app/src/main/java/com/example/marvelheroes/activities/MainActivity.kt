package com.example.marvelheroes.activities

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.core.view.get
import androidx.core.view.isVisible

import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import com.example.marvelheroes.R
import com.example.marvelheroes.databinding.ActivityMainBinding
import com.example.marvelheroes.fragments.comicsFragment.ComicsFragment
import com.example.marvelheroes.fragments.favouritesFragment.FavouritesFragment
import com.example.marvelheroes.fragments.mainActivity.MainActivityFragment


class MainActivity : AppCompatActivity() {


    private lateinit var navController: NavController

    private val heroesFragment = MainActivityFragment()
    private val favoritesFragment = FavouritesFragment()
    private val comicsFragment = ComicsFragment()
    private var currentFragment: Fragment? = null

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("fragment", currentFragment?.tag.toString())
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        val savedFragment = savedInstanceState.getString("fragment")
        if (savedFragment != null) {
            when (savedFragment) {
                "MainActivityFragment" -> currentFragment = heroesFragment
                "FavouritesFragment"   -> currentFragment = favoritesFragment
                "ComicsFragment"       -> currentFragment = comicsFragment
            }
        }
        super.onRestoreInstanceState(savedInstanceState)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        /*
        // Get the navigation host fragment from this Activity
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        // Instantiate the navController using the NavHostFragment
        navController = navHostFragment.navController*/
        // Make sure actions in the ActionBar get propagated to the NavController


        binding.myToolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24)
        if (supportActionBar?.isShowing != true) {
            setSupportActionBar(binding.myToolbar)
        }


        binding.bottomNavigation.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.navigation_heroes    -> replaceFragment(heroesFragment)
                R.id.navigation_favorites -> replaceFragment(favoritesFragment)
                R.id.navigation_comics    -> replaceFragment(comicsFragment)
            }
            true
        }

        supportFragmentManager.addOnBackStackChangedListener {
            if(supportFragmentManager.backStackEntryCount == 0) {
                binding.bottomNavigation.menu.findItem(R.id.navigation_heroes).isChecked = true


            }
        }



    }


    override fun onResume() {
        if (currentFragment == null) {
            currentFragment = heroesFragment
        }
        replaceFragment(currentFragment!!)

        super.onResume()
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            if (supportFragmentManager.backStackEntryCount > 0) {
                supportFragmentManager.popBackStackImmediate()
            } else {
                supportFragmentManager.popBackStack()
            }
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }


    private fun replaceFragment(fragment: Fragment) {
        val ft = supportFragmentManager.beginTransaction()
        ft.setCustomAnimations(
            R.anim.slide_in_right, R.anim.slide_out_right
        ).replace(R.id.nav_host_fragment, fragment, fragment.javaClass.simpleName).addToBackStack(fragment.tag).commit()
        currentFragment = fragment
    }
    fun getTopFragment(): Fragment? {
        if (supportFragmentManager.backStackEntryCount == 0) {
            return null
        }
        val fragmentTag = supportFragmentManager.getBackStackEntryAt(supportFragmentManager.backStackEntryCount - 1).name
        return supportFragmentManager.findFragmentByTag(fragmentTag)
    }
}





