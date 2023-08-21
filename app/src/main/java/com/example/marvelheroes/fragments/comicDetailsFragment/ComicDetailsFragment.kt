package com.example.marvelheroes.fragments.comicDetailsFragment

import android.app.Dialog
import android.os.Bundle
import android.transition.TransitionInflater
import android.view.*
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.example.marvelheroes.databinding.FragmentComicDetailsBinding
import com.example.marvelheroes.model.repository.Favorites

class ComicDetailsFragment : Fragment() {

    private var _binding : FragmentComicDetailsBinding? = null
    private val binding get() = _binding!!
    private var comicTitle: String? = null
    private var comicDescription: String? = null
    private var comicImage: String? = null
    private var comicPages: Int? = null
    private var comicId : Int? = null
    private var connectionDialog: Dialog? = null

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

        val animation = TransitionInflater.from(requireContext()).inflateTransition(android.R.transition.move)
        sharedElementEnterTransition = animation
        sharedElementReturnTransition = animation
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentComicDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        (activity as AppCompatActivity?)!!.supportActionBar!!.setDisplayShowCustomEnabled(true)
        (activity as AppCompatActivity?)!!.supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        setHasOptionsMenu(true)


        setupUi(arguments)

        binding.marvelComicTitle.transitionName = comicTitle
        binding.marvelImage.transitionName = comicId.toString()


    }

    private fun setupUi(data: Bundle?) {
        arguments?.let {
            comicId = it.getInt("id")
            comicTitle = it.getString("title")
            comicImage = it.getString("image")
            comicDescription = it.getString("description")
            comicPages = it.getInt("pageCount")
        }
        binding.marvelComicTitle.text = comicTitle
        binding.marvelComicDescription.text = comicDescription
        binding.marvelComicPageCount.text = comicPages.toString() + " Pages"

        //Glide apparently needs a secure connection (https)? This line changes the http to https
        val imgUri = comicImage?.toUri()?.buildUpon()?.scheme("https")?.build()

        Glide.with(this)
            .load(imgUri)
            .into(binding.marvelImage)

    }

    /**onDestroyView saves the search query and clears the susbscriptions*/
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        connectionDialog = null
    }





}