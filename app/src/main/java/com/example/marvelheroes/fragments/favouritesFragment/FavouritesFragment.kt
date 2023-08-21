package com.example.marvelheroes.fragments.favouritesFragment


import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import com.example.marvelheroes.R
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import androidx.appcompat.widget.SearchView
import android.view.animation.AnimationUtils
import android.view.animation.Animation
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.marvelheroes.databinding.FragmentFavouritesBinding
import com.example.marvelheroes.model.repository.Favorites
import io.reactivex.rxkotlin.subscribeBy
import com.example.marvelheroes.MyApplication
import com.example.marvelheroes.network.RxSearchObservable
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit
import kotlin.math.ceil
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt


class FavouritesFragment : Fragment() {

    private var _binding: FragmentFavouritesBinding? = null
    private val binding get() = _binding!!
    private var screenInches: Int? = null
    private var adapter = FavouritesAdapter()
    private val presenter = FavouritesPresenter()
    private var characters: List<Favorites>? = null
    private var isUpdating: Boolean = false
    private var previousSearch: String? = null
    private var searchViewObservable: Disposable? = null
    private var compositeDisposable = CompositeDisposable()
    private var searchView: SearchView? = null
    private var animationFadeInProgressBar: Animation? = null
    private var animationFadeInTextView: Animation? = null
    private var animationFadeOutProgressBar: Animation? = null
    private var animationFadeOutTextView: Animation? = null
    private var isSearching = false
    private var appClass = MyApplication

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /**this will reset the search query when returning to the fragment if the fragment was destroyed
         *but not if the fragment was not destroyed (if checking hero details from this fragment)
         */
        appClass.searchFavourites = null
        retainInstance = false
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentFavouritesBinding.inflate(inflater, container, false)
        /**
         * Setting animations when creating view to make view calculations in time
         */
        setAnimations()
        /**
         * Checking device screen size to make layout adjustments
         */
        checkDeviceScreenSize()

        return binding.root
    }


    @SuppressLint("CheckResult")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        //This will hide the back button
        (activity as AppCompatActivity?)!!.supportActionBar!!.setDisplayShowCustomEnabled(false)
        (activity as AppCompatActivity?)!!.supportActionBar!!.setDisplayHomeAsUpEnabled(false)
        if (!MyApplication.searchFavourites.isNullOrEmpty()) {
            previousSearch = MyApplication.searchFavourites
            isSearching = true
        }
        setupLayoutManager()
        binding.marvelCharList.adapter = adapter
        adapter.onItemClickFavorites = { favorite: Favorites? ->
            favorite?.let {
                updateFavorite(favorite)
            }
        }
        adapter.fragmentManager = activity?.supportFragmentManager

        /**
         * When using a RecyclerView, you must wait for any data to load and for the RecyclerView
         * items to be ready to draw before starting the transition. So we use postponeTransition
         */
        postponeEnterTransition()
        val viewTreeObserver: ViewTreeObserver = binding.marvelCharList.viewTreeObserver
        viewTreeObserver.addOnPreDrawListener {
            startPostponedEnterTransition()
            true
        }

        /**
         * Setting up a flowable observable to always return the DB information
         * and update the list if there are any changes to the favorites
         */
        compositeDisposable.addAll(
            presenter.getFavorites()
                ?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.retry(2)
                ?.subscribeBy(
                    onError = { Log.i("error Sub Fav", it.message.toString()) },
                    onNext = {
                        if (!isSearching) {
                            characters = it.toMutableList(); setupUi(characters)
                        }
                    })
        )

    }

    /**
     * Checking orientation and setting grid or list view
     */
    private fun setupLayoutManager() {

        when (resources.configuration.orientation) {
            Configuration.ORIENTATION_LANDSCAPE -> {
                when {
                    screenInches!! >= 10.5 -> {
                        binding.marvelCharList.layoutManager = GridLayoutManager(context, 4)
                    }
                    screenInches!! >= 8.5 -> {
                        binding.marvelCharList.layoutManager = GridLayoutManager(context, 3)
                    }
                    screenInches!! >= 7.5 -> {
                        binding.marvelCharList.layoutManager = GridLayoutManager(context, 2)
                    }
                    else -> {
                        binding.marvelCharList.layoutManager = LinearLayoutManager(context)
                    }
                }
            }
            else -> {
                when {
                    screenInches!! >= 9 -> {
                        binding.marvelCharList.layoutManager = GridLayoutManager(context, 2)
                    }
                    else -> {
                        binding.marvelCharList.layoutManager = LinearLayoutManager(context)
                    }
                }
            }
        }
    }


    /**
     * Clearing subscriptions and saving query
     */
    override fun onDestroyView() {
        compositeDisposable.clear()
        isUpdating = false
        hideCustomProgressDialog()
        binding.marvelCharList.adapter = null
        compositeDisposable.clear()
        _binding = null
        characters = null
        if (!previousSearch.isNullOrEmpty()) {
            MyApplication.searchMain = previousSearch.toString()
        } else {
            MyApplication.searchMain = null
        }
        super.onDestroyView()
    }

    /**Sets the Search view publish Subject which sends new
     * queries to the listener and triggers new searches*/
    @SuppressLint("CheckResult")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        if(!menu.hasVisibleItems()) {
            inflater.inflate(R.menu.menu, menu)
            val menuItem = menu.findItem(R.id.search_heroes)
            searchView = menuItem?.actionView as SearchView?
        }
        searchView.let {
            //Sets the searchview width to fit the whole appbar OVER 9000!!!
            searchView!!.maxWidth = 9001
        }
        if (searchView != null && previousSearch?.isNotEmpty() == true) {
            searchView?.onActionViewExpanded()
            isSearching = true
            searchView?.setQuery(previousSearch, true)
            /**
             * this is required to force a new search when restoring from orientation change
             * */
            compositeDisposable.addAll(presenter.getFavoritesSearch(previousSearch!!)
                ?.subscribeOn(Schedulers.io())
                ?.doOnSubscribe { showCustomProgressDialog(); characters = null }
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribeBy(
                    onSuccess = { list ->
                        if (characters != list) {
                            characters = list
                            setupUi(characters)
                        }
                    },
                    onError = {
                        Log.i(
                            "Favourites Manager",
                            "unable to search: ${it.message.toString()}"
                        )
                    },
                    onComplete = {
                        setupUi(characters)

                    }
                ))
            searchView?.clearFocus()
            hideCustomProgressDialog()

        } else {
            searchView?.onActionViewExpanded()
            searchView?.setQuery("", false)
            searchView?.clearFocus()
            hideCustomProgressDialog()
        }

        searchViewObservable = RxSearchObservable.fromView(this.searchView)
            .debounce(100, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnError { }
            .subscribeBy(
                onError = { Log.e("Search Sub Error", it.message.toString()) },
                onNext = { s ->
                    if (isVisible) {
                        isUpdating = true
                        if (s != "") {
                            isSearching = true
                            previousSearch = s
                            presenter.getFavoritesSearch(s)?.subscribeOn(Schedulers.io())
                                ?.doOnSubscribe { showCustomProgressDialog(); characters = null }
                                ?.observeOn(AndroidSchedulers.mainThread())
                                ?.subscribeBy(
                                    onSuccess = { list ->
                                        if (characters != list) {
                                            characters = list
                                            setupUi(characters)
                                        }
                                    },
                                    onError = {
                                        Log.i(
                                            "Favourites Manager",
                                            "unable to search: ${it.message.toString()}"
                                        )
                                    },
                                    onComplete = {
                                        setupUi(characters)
                                    }
                                )
                        } else {
                            isSearching = false
                            previousSearch = null
                            presenter.getFavoritesMaybe()?.subscribeOn(Schedulers.io())
                                ?.doOnSubscribe { showCustomProgressDialog(); characters = null }
                                ?.observeOn(AndroidSchedulers.mainThread())
                                ?.subscribeBy(
                                    onSuccess = { list -> characters = list; setupUi(characters) },
                                    onError = {
                                        Log.i("Favourites Manager", "unable to search")
                                    },
                                    onComplete = {
                                        Log.i("Favourites Manager", "unable Complete")
                                    }
                                )
                        }
                    }

                }
            )

        /**
         * added to composite disposable to clear it when view is destroyed
         */
        compositeDisposable.add(searchViewObservable!!)


    }

    /**
     * this checks the response from the api and does a new check
     * to see if there were any errors from the api response
     * and if not, it checks the contents of the object and
     * displays the appropriate view
     */
    private fun setupUi(characters: List<Favorites>?) {
        this.characters = characters
        if (characters?.size!! <= 0) {
            if (binding.tvNoResults.visibility != View.VISIBLE) {
                binding.tvNoResults.startAnimation(animationFadeInTextView)
            }
        } else {
            if (binding.tvNoResults.visibility == View.VISIBLE) {
                binding.tvNoResults.startAnimation(animationFadeOutTextView)
            }
        }
        adapter.submitList(characters) {
            binding.marvelCharList.scrollToPosition(0)
        }
        hideCustomProgressDialog()
    }

    /**
     * this shows a progress bar on top of the recycler view
     */
    private fun showCustomProgressDialog() {
        binding.progressBar.startAnimation(animationFadeInProgressBar)
    }

    /**
     * this hides all possibly visible progress dialogs or progress bars
     */
    private fun hideCustomProgressDialog() {
        binding.progressBar.startAnimation(animationFadeOutProgressBar)
    }


    /**
     * this sets the animation properties and it's listeners
     */
    private fun setAnimations() {
        binding.tvNoResults.visibility = View.INVISIBLE
        animationFadeInProgressBar = AnimationUtils.loadAnimation(context, R.anim.fade_in)
        animationFadeInProgressBar?.duration = 250
        animationFadeInProgressBar?.startOffset = 0
        animationFadeInTextView = AnimationUtils.loadAnimation(context, R.anim.fade_in)
        animationFadeInTextView?.duration = 250
        animationFadeInTextView?.startOffset = 0
        animationFadeOutProgressBar = AnimationUtils.loadAnimation(context, R.anim.fade_out)
        animationFadeOutProgressBar?.duration = 250
        animationFadeOutProgressBar?.startOffset = 0
        animationFadeOutTextView = AnimationUtils.loadAnimation(context, R.anim.fade_out)
        animationFadeOutTextView?.duration = 250
        animationFadeOutTextView?.startOffset = 0
        animationFadeInProgressBar?.fillBefore = true
        animationFadeInProgressBar?.fillAfter = true
        animationFadeOutProgressBar?.fillBefore = true
        animationFadeOutProgressBar?.fillAfter = true
        animationFadeInProgressBar?.setAnimationListener(object :
            Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {
                binding.progressBar.visibility = View.VISIBLE
            }

            override fun onAnimationEnd(animation: Animation) {
                binding.progressBar.alpha = 1f
            }

            override fun onAnimationRepeat(animation: Animation) {}
        })
        animationFadeInTextView?.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {
                binding.tvNoResults.visibility = View.VISIBLE
            }

            override fun onAnimationEnd(animation: Animation) {}
            override fun onAnimationRepeat(animation: Animation) {}
        })

        animationFadeOutProgressBar?.setAnimationListener(object :
            Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {
                binding.progressBar.visibility = View.VISIBLE
            }

            override fun onAnimationEnd(animation: Animation) {
                // binding.progressBar.visibility = View.GONE
            }

            override fun onAnimationRepeat(animation: Animation) {}
        })

        animationFadeOutTextView?.setAnimationListener(object :
            Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}
            override fun onAnimationEnd(animation: Animation) {
                binding.tvNoResults.visibility = View.GONE
            }

            override fun onAnimationRepeat(animation: Animation) {}
        })
        binding.tvNoResults.visibility = View.GONE
        binding.progressBar.visibility = View.GONE





    }


    /**
     * this method is called by the viewHolders to update the favorite's current status
     *
     * It checks for the favorites status and then updates it in the DB, triggering
     * an OnNext from the Flowable getting all the favorites which will then
     * push the changes to the adapter
     *
     * Since it is a maybe, it will return onSuccess if there is a record
     * and onComplete if no response is returned
     */
    @SuppressLint("CheckResult")
    private fun updateFavorite(favorite: Favorites) {
        compositeDisposable.addAll(presenter.checkFavorite(favorite)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.doOnSubscribe { }
            ?.subscribeBy(
                onError = { Log.i("Fav Manager", "Unable to check Favorite from DB") },
                onSuccess = {
                    compositeDisposable.addAll(presenter.deleteFavorite(favorite)
                        ?.subscribeOn(Schedulers.io())
                        ?.observeOn(AndroidSchedulers.mainThread())
                        ?.subscribeBy(
                            onComplete = { },
                            onError = {
                                Log.i(
                                    "Fav Manager",
                                    "Unable to delete Favorite from DB"
                                )
                            }
                        ))
                },
                onComplete = {
                    compositeDisposable.addAll(presenter.insertFavorite(favorite)
                        ?.subscribeOn(Schedulers.io())
                        ?.observeOn(AndroidSchedulers.mainThread())
                        ?.subscribeBy(
                            onComplete = {},
                            onError = {
                                Log.i(
                                    "Fav Manager",
                                    "Unable to insert Favorite in DB"
                                )
                            }
                        ))
                }
            ))

    }

    /**
     * this get the diagonal of the screen in pixels and divide it by
     * the dpi density which will provide the real size of the screen
     */
    private fun checkDeviceScreenSize() {
        val dm = resources.displayMetrics
        val width = dm.widthPixels
        val height = dm.heightPixels
        val diagonal = sqrt(width.toDouble().pow(2.0) + height.toDouble().pow(2.0))
        val dens = dm.densityDpi
        screenInches = (ceil(diagonal / dm.xdpi)).roundToInt()

    }


}