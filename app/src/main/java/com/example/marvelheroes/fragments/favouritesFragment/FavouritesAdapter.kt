package com.example.marvelheroes.fragments.favouritesFragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.marvelheroes.databinding.ListMarvelCharacterBinding
import com.example.marvelheroes.model.repository.Favorites
import android.view.View
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.example.marvelheroes.R
import com.example.marvelheroes.fragments.heroDetailsFragment.HeroDetailsFragment


/**
 * Adapter for the [RecyclerView] in [FavouritesFragment]. Displays [Character] data object.
 */
class FavouritesAdapter : ListAdapter<Favorites, FavouritesAdapter.MarvelCharacterViewHolder>(
    FavouritesFragmentDiffCallback(),
) {
    var onItemClickFavorites: ((favorite: Favorites) -> Unit)? = null
var fragmentManager : FragmentManager? = null
    class FavouritesFragmentDiffCallback : DiffUtil.ItemCallback<Favorites>() {
        override fun areItemsTheSame(oldItem: Favorites, newItem: Favorites): Boolean {

            return if (newItem.heroId < 0) {
                oldItem.heroId == 0

            } else {
                oldItem.heroId == newItem.heroId
            }
        }

        override fun areContentsTheSame(oldItem: Favorites, newItem: Favorites): Boolean {
            return oldItem == newItem
        }
    }


    private val TYPE_ITEM_CHAR = 0
    private val TYPE_ITEM_BTN = 1


    /**
     * Create new views (invoked by the layout manager)
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MarvelCharacterViewHolder {
        return MarvelCharacterViewHolder.inflater(parent)
    }

    /**
     * Replace the contents of a view (invoked by the layout manager)
     */
    override fun onBindViewHolder(holder: MarvelCharacterViewHolder, position: Int) {
        if (position >= 0) {
            val character = getItem(position)

            holder.display(character, onItemClickFavorites, fragmentManager!!)
        }

    }

    /*override fun getItemViewType(position: Int): Int {
        return if (position == dataset.size) TYPE_ITEM_BTN else TYPE_ITEM_CHAR
    }*/

    //override fun getItemCount() = dataset.size

    /**
     * RecyclerView doesn't interact directly with item views, but deals with ViewHolders instead.
     * A ViewHolder represents a single list item view in RecyclerView, and can be reused when possible.
     * A ViewHolder instance holds references to the individual views within a list item layout
     * (hence the name "view holder"). This makes it easier to update the list item view with new data.
     */
    class MarvelCharacterViewHolder private constructor(val binding: ListMarvelCharacterBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun display(marvelChar: Favorites, onItemClickFavorites: ((favorite: Favorites) -> Unit)?,  fragmentManager: FragmentManager) {
            val fragment = HeroDetailsFragment()
            binding.marvelCharacterTitle.text = marvelChar.heroName
            //Glide apparently needs a secure connection (https)? This line changes the http to https
            val imgUri = marvelChar.heroImage?.toUri()?.buildUpon()?.scheme("https")?.build()
            Glide.with(itemView.context)
                .load(imgUri)
                .into(binding.marvelImageMain)
            val arguments = Bundle()
            arguments.putInt("id", marvelChar.heroId)
            arguments.putString("name", marvelChar.heroName)
            arguments.putString("description", marvelChar.heroDescription)
            arguments.putString("image", marvelChar.heroImage)
            fragment.arguments = arguments
            //Click Listener to start new fragment
            binding.itemParentView.setOnClickListener {
                fragmentManager.beginTransaction()
                    .setReorderingAllowed(true)
                    .setCustomAnimations(
                        R.anim.slide_in_right,
                        R.anim.slide_out_right,
                        R.anim.slide_in_left,
                        R.anim.slide_out_left
                    )
                    .replace(R.id.nav_host_fragment, fragment, fragment.javaClass.simpleName)
                    .addToBackStack(fragment.javaClass.simpleName)
                    .addSharedElement(binding.marvelImageMain, marvelChar.heroId.toString())
                    .addSharedElement(binding.marvelCharacterTitle, marvelChar.heroName)
                    .addSharedElement(binding.favouritesButtonMain, "favoritesButton")
                    .commit()
            }

            /**
             * We define an unique transition name and create the extras in order to use
             * shared element transition
             */
            binding.marvelImageMain.transitionName = marvelChar.heroId.toString()
            binding.marvelCharacterTitle.transitionName = marvelChar.heroName
            binding.favouritesButtonMain.transitionName = "favoritesButtonMain"
           binding.favouritesButtonMain.isActivated = true;

            binding.favouritesButtonMain.setOnClickListener {
                Log.i("favourites btn", "Clicked")
                onItemClickFavorites?.invoke(Favorites(null, marvelChar.heroId,
                    marvelChar.heroName,marvelChar.heroDescription,marvelChar.heroImage))
            }
        }

        companion object {
            fun inflater(parent: ViewGroup): MarvelCharacterViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)

                val binding = ListMarvelCharacterBinding.inflate(layoutInflater, parent, false)

                return MarvelCharacterViewHolder(binding)
            }
        }
    }



}
