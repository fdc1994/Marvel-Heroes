package com.example.marvelheroes.fragments.heroDetailsFragment

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.transition.TransitionInflater
import android.util.Log
import android.view.*
import android.widget.Button
import androidx.fragment.app.Fragment
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.isGone
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.marvelheroes.R
import com.example.marvelheroes.databinding.FragmentHeroDetailsBinding
import com.example.marvelheroes.model.repository.Favorites
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers

class HeroDetailsFragment : Fragment() {

    private var _binding : FragmentHeroDetailsBinding? = null
    private val binding get() = _binding!!
    private var presenter = HeroDetailsPresenter()
    private val compositeDisposable = CompositeDisposable()
    private lateinit var recyclerView: RecyclerView
    private lateinit var heroNameTextView: TextView
    private lateinit var heroImageView: ImageView
    private var characterName: String? = null
    private var characterDescription: String? = null
    private var characterImage: String? = null
    var data: List<Any>? = null
    var heroId: Int = 0
    private var connectionDialog: Dialog? = null
    private var favoriteTemp: Favorites? = null

    /** OnCreate checks if all arguments are present with a safe null check
     *
     * An instance of a Favourite Object is created so that it can later work
     * with the favourites manager
     *
     * It will set the appropriate Favourite's Button state
     * */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setHasOptionsMenu(false)
        setHasOptionsMenu(false)

        /**
         * We inflate the transition animation and we indicate that we will use the same image and
         * title the previous fragment was using (we do this on onViewCreated by using the same
         * transition names defined on MainActivityAdapter.kt
         */
        val animation = TransitionInflater.from(requireContext()).inflateTransition(android.R.transition.move)
        sharedElementEnterTransition = animation
        sharedElementReturnTransition = animation

        arguments?.let {
            heroId = it.getInt("id")
            characterName = it.getString("name")
            characterImage = it.getString("image")
            characterDescription = it.getString("description")

            favoriteTemp =
                Favorites(null, heroId, characterName!!, characterDescription, characterImage)
            presenter.checkFavorite(favoriteTemp!!)
                ?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribeBy(
                    onError = { Log.i("Fav Manager", "Unable to check Favorite from DB") },
                    onSuccess = {
                        binding.favouritesButtonDetails.isActivated = true
                    },
                    onComplete = {
                        binding.favouritesButtonDetails.isActivated = false
                    }
                )
        }




    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentHeroDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        (activity as AppCompatActivity?)!!.supportActionBar!!.setDisplayShowCustomEnabled(true)
        (activity as AppCompatActivity?)!!.supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        recyclerView = binding.characterDetailsRecyclerView
        heroNameTextView = binding.marvelCharacterTitle
        heroImageView = binding.marvelImage
        setHasOptionsMenu(true)
        heroImageView.transitionName = heroId.toString()
        heroNameTextView.transitionName = characterName
        binding.favouritesButtonDetails.transitionName = "favoritesButton"


        val possibleDestinations = listOf(
            resources.obtainTypedArray(R.array.loading_layouts)
        )
        setFavoritesListener()
        binding.progressBar.indeterminateDrawable =
            possibleDestinations[0].getDrawable((0 until possibleDestinations[0].length()).random())
        //call to get info with asynchronous RxJava
        setupUi(data)
        showLoading()
        getHeroDetails()


    }
    /**This will fetch the current hero's details
     * Comics
     * Stories
     * Events
     * Series*/
    @SuppressLint("CheckResult")
    private fun getHeroDetails() {
        compositeDisposable.addAll( presenter.getHeroEventsObject(heroId)?.doOnComplete {

        }
            ?.doOnSubscribe {
                showLoading()
            }
            ?.subscribe({ response ->
                if (response != null) {
                    setupUi(response)
                } else {
                    hideLoading()
                }

            }, { e ->
                e.message?.let { e ->
                    showConnectionFailedDialog()
                }
            }))
    }
    /**Hides the option menu
     * Search and Favourites option*/

    /**
     * this checks the response from the api and does a new check
     * to see if there were any errors from the api response
     * and if not, it checks the contents of the object and
     * displays the appropriate view
     */
    private fun setupUi(data: List<Any>?) {
        hideLoading()
        heroNameTextView.text = characterName
        binding.marvelCharacterDescription.text = characterDescription
        //Glide apparently needs a secure connection (https)? This line changes the http to https
        val imgUri = characterImage?.toUri()?.buildUpon()?.scheme("https")?.build()

        Glide.with(this)
            .load(imgUri)
            .into(binding.marvelImage)

        data?.let {
            if (data[0] == "API CALL ERROR") {
                showConnectionFailedDialog()
            } else {
                recyclerView.adapter = HeroDetailsAdapter(data)
            }

        }

    }
    /**
     * this shows a progress bar on top of the recycler view when loading information
     */
    private fun showLoading() {
        binding.progressInfo.isGone = false
        binding.characterDetailsRecyclerView.isGone = true

    }
    /**
     * this hides the progress bar on top of the recycler view when loading information
     * and shows the recyclerview
     */
    private fun hideLoading() {
        binding.progressInfo.isGone = true
        binding.characterDetailsRecyclerView.isGone = false
    }
    /**onDestroyView saves the search query and clears the susbscriptions*/
    override fun onDestroyView() {
        super.onDestroyView()
        recyclerView.adapter = null
        compositeDisposable.clear()
        _binding = null
        data = null
        connectionDialog = null
        favoriteTemp = null
    }

    /**
     * this is called if there is any API error or no internet connection
     */
    private fun showConnectionFailedDialog() {

        hideLoading()
        connectionDialog?.dismiss()
        connectionDialog = context?.let { Dialog(it) }
        connectionDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        //Set the screen content from a layout resource
        // The resource will be inflated, adding all top-level views to the screen
        connectionDialog?.setContentView(R.layout.dialog_connection_failed)
        val btnRefreshData = connectionDialog?.findViewById<Button>(R.id.btn_retry_connection)
        btnRefreshData?.setOnClickListener {
            hideConnectionFailedDialog()
        }
        connectionDialog?.setCancelable(false)
        connectionDialog?.setCanceledOnTouchOutside(false)
        //start the dialog and display it on the screen
        connectionDialog?.show()
    }
    /**
     * this hides the noConnectionDialog
     */
    private fun hideConnectionFailedDialog() {
        connectionDialog?.dismiss()
        getHeroDetails()
    }
    /**
     * this sets the favourite's button click listener to call updateFavorite()
     */
    private fun setFavoritesListener() {
        binding.favouritesButtonDetails.setOnClickListener {
            favoriteTemp?.let { it1 -> updateFavorite(it1) }
        }
    }

    /**
     * this method is called by the favourite button to update the favorite's current status
     *
     * It checks for the favorites status and then updates it in the DB
     *
     * Since it is a maybe, it will return onSuccess if there is a record
     * and onComplete if no response is returned
     */
    @SuppressLint("CheckResult")
    private fun updateFavorite(favorite: Favorites) {

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
                            onComplete = {
                                binding.favouritesButtonDetails.isActivated = false
                            },
                            onError = { Log.i("Fav Manager", "Unable to delete Favorite from DB") }
                        ))
                },
                onComplete = {
                    compositeDisposable.addAll( presenter.insertFavorite(favorite)
                        ?.subscribeOn(Schedulers.io())
                        ?.observeOn(AndroidSchedulers.mainThread())
                        ?.subscribeBy(
                            onComplete = {
                                binding.favouritesButtonDetails.isActivated = true
                            },
                            onError = { Log.i("Fav Manager", "Unable to insert Favorite in DB") }
                        ))
                }
            ))

    }


}