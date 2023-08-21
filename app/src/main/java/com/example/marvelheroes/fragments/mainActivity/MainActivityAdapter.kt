package com.example.marvelheroes.fragments.mainActivity


import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.marvelheroes.databinding.ListMarvelCharacterBinding
import com.example.marvelheroes.model.character.Character
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.example.marvelheroes.databinding.MarvelCharacterPaginationBtnBinding
import com.example.marvelheroes.model.repository.Favorites
import androidx.fragment.app.FragmentManager
import com.example.marvelheroes.R
import com.example.marvelheroes.fragments.heroDetailsFragment.HeroDetailsFragment


private val TYPE_ITEM_CHAR = 0
private val TYPE_ITEM_BTN = 1


/**
 * Adapter for the [RecyclerView] in [MainActivityFragment]. Displays [Character] data object.
 */
class MainActivityAdapter() :
    ListAdapter<MainActivityAdapter.DataItem, RecyclerView.ViewHolder>(MainActivityDiffCallback()) {

    var onItemClick: (() -> Unit)? = null
    var onItemClickFavorites: ((favorite: Favorites, position: Int) -> Unit)? = null
    var favorites = mutableListOf<Favorites>()
    var fragmentManager: FragmentManager? = null

    /**
     * DiffCallbacks replaces the items that changed instead of replacing the whole list
     */
    class MainActivityDiffCallback : DiffUtil.ItemCallback<DataItem>() {

        override fun areItemsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
            return oldItem == newItem
        }
    }

    /**
     * Create new views (invoked by the layout manager)
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_ITEM_CHAR -> MarvelCharacterViewHolder.inflater(parent)
            TYPE_ITEM_BTN -> ButtonViewHolder.inflater(parent)
            else -> throw ClassCastException("Unknown viewType $viewType")
        }
    }

    /**
     * Replace the contents of a view (invoked by the layout manager)
     */
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is MarvelCharacterViewHolder -> {
                val characterItem = getItem(position) as DataItem.CharacterItem
                holder.display(characterItem.character, favorites, onItemClickFavorites,
                    fragmentManager!!
                )
            }
            is ButtonViewHolder -> {
                holder.binding.loadMoreBtn.setOnClickListener {
                    onItemClick?.invoke()
                }
            }
        }


    }

    /**
     * Check if the element in the list is a Character or a Button
     */
    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is DataItem.CharacterItem -> TYPE_ITEM_CHAR
            is DataItem.LoadMoreButton -> TYPE_ITEM_BTN
        }
    }

    //override fun getItemCount() = dataset.size

    class ButtonViewHolder(val binding: MarvelCharacterPaginationBtnBinding) :
        RecyclerView.ViewHolder(binding.root) {

        companion object {
            fun inflater(parent: ViewGroup): RecyclerView.ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding =
                    MarvelCharacterPaginationBtnBinding.inflate(layoutInflater, parent, false)

                return ButtonViewHolder(binding)

            }
        }

    }

    /**
     * RecyclerView doesn't interact directly with item views, but deals with ViewHolders instead.
     * A ViewHolder represents a single list item view in RecyclerView, and can be reused when possible.
     * A ViewHolder instance holds references to the individual views within a list item layout
     * (hence the name "view holder"). This makes it easier to update the list item view with new data.
     */
    class MarvelCharacterViewHolder private constructor(val binding: ListMarvelCharacterBinding) :
        RecyclerView.ViewHolder(binding.root) {


        fun display(
            marvelChar: Character,
            favorites: MutableList<Favorites>,
            onItemClickFavorites: ((favorite: Favorites, position: Int) -> Unit)?,
            fragmentManager: FragmentManager
        ) {
            var fragment = HeroDetailsFragment()
            binding.marvelCharacterTitle.text = marvelChar.name
            val imgUrl = "${marvelChar.thumbnail.path}.${marvelChar.thumbnail.extension}"
            val favoriteTemp = Favorites(
                null,
                marvelChar.id,
                marvelChar.name,
                marvelChar.description,
                imgUrl
            )
            //Glide apparently needs a secure connection (https)? This line changes the http to https
            val imgUri = imgUrl.toUri().buildUpon().scheme("https").build()

            //Library Glide fetches the image from the url and displays it on the screen
            Glide.with(itemView.context)
                .load(imgUri)
                .into(binding.marvelImageMain)

            val arguments = Bundle()
            arguments.putInt("id", marvelChar.id)
            arguments.putString("name", marvelChar.name)
            arguments.putString("description", marvelChar.description)
            arguments.putString("image", imgUrl)
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
                    .addSharedElement(binding.marvelImageMain, marvelChar.id.toString())
                    .addSharedElement(binding.marvelCharacterTitle, marvelChar.name)
                    .addSharedElement(binding.favouritesButtonMain, "favoritesButton")
                    .commit()
            }
            //checking if current item is in favorites array
            var isFavorite = false
            for (favorite in favorites) {
                if (favorite.heroId == marvelChar.id) {
                    isFavorite = true
                }

            }

            /**
             * We define an unique transition name and create the extras in order to use
             * shared element transition
             */
            binding.marvelImageMain.transitionName = marvelChar.id.toString()
            binding.marvelCharacterTitle.transitionName = marvelChar.name
            binding.favouritesButtonMain.transitionName = "favoritesButton"
            //Setting the correct drawable state depending on favorite status
            binding.favouritesButtonMain.isActivated = isFavorite
            binding.favouritesButtonMain.setOnClickListener {
                onItemClickFavorites?.invoke(favoriteTemp, adapterPosition)
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

    sealed class DataItem {
        abstract val id: Int

        data class CharacterItem(val character: Character) : DataItem() {
            override val id = character.id
        }

        object LoadMoreButton : DataItem() {
            override val id: Int = Int.MAX_VALUE
        }
    }

}