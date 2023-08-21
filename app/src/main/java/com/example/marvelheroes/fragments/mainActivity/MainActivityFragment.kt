package com.example.marvelheroes.fragments.mainActivity

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import com.example.marvelheroes.R
import com.example.marvelheroes.databinding.FragmentMainActivityBinding
import com.example.marvelheroes.model.character.CharacterDataWrapper
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import androidx.appcompat.widget.SearchView
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.core.view.isGone
import androidx.navigation.findNavController
import com.example.marvelheroes.MyApplication
import com.example.marvelheroes.model.character.Character
import com.example.marvelheroes.model.repository.Favorites
import com.example.marvelheroes.network.RxSearchObservable
import io.reactivex.rxkotlin.subscribeBy
import java.util.concurrent.TimeUnit
import io.reactivex.disposables.Disposable
import android.widget.Toast

import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import kotlin.math.ceil
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt
import androidx.appcompat.app.AppCompatActivity


import android.view.ViewTreeObserver
import androidx.core.view.doOnPreDraw
import android.text.InputType


class MainActivityFragment : Fragment() {

    private var _binding: FragmentMainActivityBinding? = null
    private val binding get() = _binding!!
    private var screenInches: Int? = null
    private var adapter = MainActivityAdapter()
    private val presenter = MainActivityFragmentPresenter()
    private var characters: CharacterDataWrapper? = null
    private var offset: Int = 0
    private var progressDialog: Dialog? = null
    private var connectionDialog: Dialog? = null
    private var isUpdating: Boolean = false
    private var restoreSearch: Boolean = false
    private var previousSearch: String? = null
    private var searchViewObservable: Disposable? = null
    private var compositeDisposable = CompositeDisposable()
    private var searchView: SearchView? = null
    private var animationFadeInProgressBar: Animation? = null
    private var animationFadeInTextView: Animation? = null
    private var animationFadeOutProgressBar: Animation? = null
    private var animationFadeOutTextView: Animation? = null
    private var items = mutableListOf<MainActivityAdapter.DataItem>()
    private var lastChangedItem: Int? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentMainActivityBinding.inflate(inflater, container, false)
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


    @SuppressLint("CheckResult", "NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.progressBar.isGone = true
        showCustomProgressDialog()
        (activity as AppCompatActivity?)!!.supportActionBar!!.setDisplayShowCustomEnabled(false)
        (activity as AppCompatActivity?)!!.supportActionBar!!.setDisplayHomeAsUpEnabled(false)

        setupLayoutManager()
        /**
         * Sending lambda functions to adapter to bind to viewholders
         */
        adapter.fragmentManager = activity?.supportFragmentManager
        adapter.onItemClick = { pagination() }
        adapter.onItemClickFavorites = { favorite: Favorites?, position: Int ->
            favorite?.let {
                updateFavorite(
                    it,
                    position
                )
            }
        }

        /**
         * Setting up the recyclerview and it's observable
         * to enable infinite scrolling
         */
        binding.marvelCharList.adapter = adapter
        binding.marvelCharList.itemAnimator?.changeDuration = 0
        setupInfiniteScrollingListener()

        /*postponeEnterTransition()

        (view.parent as? ViewGroup)?.doOnPreDraw {
            startPostponedEnterTransition()
        }*/

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
         * and make the needed adjustments
         */
        presenter.getFavorites()
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.retry(2)
            ?.subscribeBy(
                onError = { Log.i("error Sub Fav", it.message.toString()) },
                onNext = {
                    /**Sending the updated favorites list so the adapter can check it*/
                    adapter.favorites = it.toMutableList()
                    /**If there was a change to a single favorite, this will notify the adapter to check
                     * this specific position only*/
                    lastChangedItem?.let { position ->
                        adapter.notifyItemChanged(
                            position
                        )
                    }
                })
        /**If the characters object is null when starting the fragment,
         * it'll make a new search with the previousSearch
         * which can have no parameters or if available, a search parameter*/
        if (!MyApplication.searchMain.isNullOrEmpty()) {
            previousSearch = MyApplication.searchMain
            restoreSearch = true
        }
        if (characters == null) {

            getPresenterApiData(previousSearch)
        }
        /**If the characters object is not null
         * the progress dialog is hidden and
         * the view is resumed*/
        else {
            hideCustomProgressDialog()
        }
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
                    screenInches!! >= 4 -> {
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
     * We increase the offset (avoids showing the first X elements in the call) in order to "change the page"
     */
    private fun pagination() {
        offset += 20
        isUpdating = true
        characters = null

        getPresenterApiData(previousSearch, offset = offset)
    }


    @SuppressLint("CheckResult")
    private fun getPresenterApiData(name: String? = null, offset: Int = 0) {
        /**Search is started if the object characters is null
         * object characters is = null when a new search happens
         * if this is called without a new search, it will not run
         * a new search and will instead just refresh the ui*/
        if (characters == null) {
            val subscription = presenter.getCharactersObject(name, offset)
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribeOn(Schedulers.io())
                ?.retry(5)
                ?.doOnSubscribe {
                    if (!restoreSearch) {
                        showCustomProgressDialog()
                    }
                    restoreSearch = false
                }?.subscribeBy(
                    onError = { t ->
                        isUpdating = false
                        showConnectionFailedDialog()
                        t.message?.let { it1 ->
                            Log.e(
                                "Error Subscribe",
                                it1

                            )
                        }
                    },
                    onNext = { response ->

                        isUpdating = false
                        Log.i("Response subscribe", response.toString())
                        if (response != null)
                            setupUi(response)
                        else {
                            if (!binding.tvNoResults.isGone) {
                                binding.tvNoResults.startAnimation(animationFadeOutTextView)
                            }

                        }
                    }
                )
            subscription?.let { compositeDisposable.add(it) }

        } else {
            setupUi(characters!!)
        }

    }

    /**onDestroyView saves the search query and clears the subscriptions*/
    override fun onDestroyView() {
        if (previousSearch?.isNotEmpty() == true) {
            MyApplication.searchMain = previousSearch.toString()
        } else {
            MyApplication.searchMain = null
        }
        isUpdating = false
        binding.marvelCharList.adapter = null
        compositeDisposable.clear()
        _binding = null
        connectionDialog = null
        super.onDestroyView()

    }

    /**Sets the Searchview publish Subject which sends new
     * queries to the listener and triggers new searches*/
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
       if(!menu.hasVisibleItems()) {
           inflater.inflate(R.menu.menu, menu)
           val menuItem = menu.findItem(R.id.search_heroes)
           searchView = null
           searchView = menuItem?.actionView as SearchView?
       }

        searchView.let {
            //Sets the searchview width to fit the whole appbar OVER 9000!!!
            searchView!!.maxWidth = 9001
            searchView?.onActionViewExpanded()
            if (previousSearch?.isNotEmpty() == true) {
                searchView?.setQuery(previousSearch, true)
            } else {
                searchView?.setQuery("", true)
            }
            searchView?.clearFocus()
        }


        /**
         * We create an observable in order to add an debounce (command+click to get more information)
         * which prevents excessive api calls (the time can be increased if necessary)
         */

        searchViewObservable = RxSearchObservable.fromView(this.searchView)
            .debounce(350, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnError { }
            .subscribeBy(
                onError = { Log.e("Search Sub Error", it.message.toString()) },
                onNext = { s ->

                    if (isVisible) {
                        isUpdating = true
                        offset = 0
                        if (s.isNotEmpty() && isUpdating && s != previousSearch) {
                            characters = null
                            previousSearch = s
                            getPresenterApiData(s)
                        } else if (s.isEmpty() && isUpdating && s != previousSearch) {
                            characters = null
                            previousSearch = null
                            getPresenterApiData(previousSearch)
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
    private fun setupUi(characters: CharacterDataWrapper?) {
        this.characters = characters
        if (characters?.code == 200) {
            if (characters.data.count == 0) {
                if (offset > 0) {
                    Toast.makeText(
                        context,
                        R.string.no_more_heroes_search,
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    if (binding.tvNoResults.visibility != View.VISIBLE) {
                        binding.tvNoResults.startAnimation(animationFadeInTextView)
                    }
                }
                preparingRVList(characters.data.results, characters.data.offset)
                hideCustomProgressDialog()
            } else {
                if (binding.tvNoResults.visibility != View.INVISIBLE) {
                    binding.tvNoResults.startAnimation(animationFadeOutTextView)
                }
                hideCustomProgressDialog()
                preparingRVList(characters.data.results, characters.data.offset)
            }
        } else if (characters?.code == 0) {

            showConnectionFailedDialog()
        } else {
            getPresenterApiData(null)
        }


    }

    /**
     * We prepare the items before sending them to the adapter (which later will be shown in the recyclerView)
     */
    private fun preparingRVList(list: List<Character>, offsetAPI: Int) {

        if (offsetAPI == 0) {
            items =
                list.map { MainActivityAdapter.DataItem.CharacterItem(it) }.toMutableList()

        } else {
            //items.removeLast()
            items.addAll(list.map { MainActivityAdapter.DataItem.CharacterItem(it) }
                .toMutableList())
        }

        //items.add(MainActivityAdapter.DataItem.LoadMoreButton)

        adapter.submitList(items) {
            binding.marvelCharList.scrollToPosition(offsetAPI)
        }
    }


    /**
     * this shows a CustomProgressDialog
     * If it's the first run, isUpdating is false and it will show a custom dialog which prevents the user
     * from interacting with the UI before any data is available
     *
     * Otherwise it will just show a progress bar on top of the recycler view
     */
    private fun showCustomProgressDialog() {
        binding.progressBar.startAnimation(animationFadeInProgressBar)

    }

    /**
     * this hides all possibly visible progress dialogs or progress bars
     */
    private fun hideCustomProgressDialog() {
        if (!isUpdating) {
            binding.progressBar.startAnimation(animationFadeOutProgressBar)
            //binding.progressBar.startAnimation(animationFadeOutProgressBar)
            progressDialog?.dismiss()
        }
    }

    /**
     * this is called if there is any API error or no internet connection
     */
    private fun showConnectionFailedDialog() {

        hideCustomProgressDialog()
        connectionDialog?.dismiss()
        connectionDialog = context?.let { Dialog(it) }
        connectionDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        //Set the screen content from a layout resource
        // The resource will be inflated, adding all top-level views to the screen
        connectionDialog?.setContentView(R.layout.dialog_connection_failed)
        val btnRefreshData =
            connectionDialog?.findViewById<Button>(R.id.btn_retry_connection)
        btnRefreshData?.setOnClickListener {
            hideConnectionFailedDialog()
        }
        connectionDialog?.setCancelable(false)
        connectionDialog?.setCanceledOnTouchOutside(false)
        //start the dialog and display it on the screen
        connectionDialog?.show()
    }

    /**
     * this is called after pressing the button to retry the connection
     * it will restart a search with the previous used query
     */
    private fun hideConnectionFailedDialog() {
        connectionDialog?.dismiss()
        getPresenterApiData(previousSearch)
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
        //binding.progressBar.visibility = View.GONE


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
    private fun updateFavorite(favorite: Favorites, position: Int) {

        compositeDisposable.addAll(presenter.checkFavorite(favorite)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribeBy(
                onError = { Log.i("Fav Manager", "Unable to check Favorite from DB") },
                onSuccess = {
                    compositeDisposable.addAll(presenter.deleteFavorite(favorite)
                        ?.subscribeOn(Schedulers.io())
                        ?.observeOn(AndroidSchedulers.mainThread())
                        ?.subscribeBy(
                            onComplete = { lastChangedItem = position },
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
                            onComplete = { lastChangedItem = position },
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
     * this will setup a new listener for the recycler view.
     *
     * It will not only check if the last item is scrolled but also
     * compare the y position to make sure that if no further results are shown
     * the app will not search twice when scrolling back up
     */
    private fun setupInfiniteScrollingListener() {
        binding.marvelCharList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (!recyclerView.canScrollVertically(1) && dy > 0) {
                    pagination()
                }
            }
        })
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

