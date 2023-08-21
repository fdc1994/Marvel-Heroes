package com.example.marvelheroes.fragments.comicsFragment


import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.example.marvelheroes.databinding.MarvelCharacterPaginationBtnBinding
import androidx.fragment.app.FragmentManager
import com.example.marvelheroes.R
import com.example.marvelheroes.databinding.ListMarvelComicsBinding
import com.example.marvelheroes.fragments.comicDetailsFragment.ComicDetailsFragment
import com.example.marvelheroes.model.comic.Comic


private val TYPE_ITEM_CHAR = 0
private val TYPE_ITEM_BTN = 1


/**
 * Adapter for the [RecyclerView] in [ComicsFragment]. Displays [Comic] data object.
 */
class ComicsAdapter() :
    ListAdapter<ComicsAdapter.DataItem, RecyclerView.ViewHolder>(ComicsDiffCallback()) {

    var onItemClick: (() -> Unit)? = null
    //var onItemClickFavorites: ((favorite: Favorites, position: Int) -> Unit)? = null
    var fragmentManager: FragmentManager? = null

    /**
     * DiffCallbacks replaces the items that changed instead of replacing the whole list
     */
    class ComicsDiffCallback : DiffUtil.ItemCallback<DataItem>() {

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
            TYPE_ITEM_CHAR -> ComicsViewHolder.inflater(parent)
            else -> throw ClassCastException("Unknown viewType $viewType")
        }
    }

    /**
     * Replace the contents of a view (invoked by the layout manager)
     */
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ComicsViewHolder -> {
                val comicItem = getItem(position) as DataItem.ComicItem
                holder.display(comicItem.comic,
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
            is DataItem.ComicItem -> TYPE_ITEM_CHAR
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
    class ComicsViewHolder private constructor(val binding: ListMarvelComicsBinding) :
        RecyclerView.ViewHolder(binding.root) {


        fun display(
            marvelComic: Comic,
            fragmentManager: FragmentManager
        ) {
            val fragment = ComicDetailsFragment()
            binding.marvelComicTitle.text = marvelComic.title
            val imgUrl = "${marvelComic.thumbnail.path}.${marvelComic.thumbnail.extension}"
            //Glide apparently needs a secure connection (https)? This line changes the http to https
            val imgUri = imgUrl.toUri().buildUpon().scheme("https").build()
            //Library Glide fetches the image from the url and displays it on the screen
            Glide.with(itemView.context)
                .load(imgUri)
                .into(binding.marvelImageMain)


            val arguments = Bundle()
            if(marvelComic.description.isNullOrEmpty()) {
                if(marvelComic.textObjects.count() >0) {
                    arguments.putString("description", marvelComic.textObjects[0].text)
                }
                else {
                    arguments.putString("description", "No description available")
                }
            }else {
                arguments.putString("description", marvelComic.description)
            }
            arguments.putInt("id", marvelComic.id)
            arguments.putString("title", marvelComic.title)
            arguments.putString("image", imgUrl)
            arguments.putInt("pageCount", marvelComic.pageCount)
            fragment.arguments = arguments
            //Click Listener to start new fragment
            binding.itemParentView.setOnClickListener {
                fragmentManager.beginTransaction()
                    .setReorderingAllowed(true)
                    .addSharedElement(binding.marvelImageMain, marvelComic.id.toString())
                    .addSharedElement(binding.marvelComicTitle, marvelComic.title)
                    .replace(R.id.nav_host_fragment, fragment, fragment.javaClass.simpleName)
                    .addToBackStack(fragment.javaClass.simpleName)
                    .commit()
            }

            binding.marvelImageMain.transitionName = marvelComic.id.toString()
            binding.marvelComicTitle.transitionName = marvelComic.title

        }

        companion object {
            fun inflater(parent: ViewGroup): ComicsViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)

                val binding = ListMarvelComicsBinding.inflate(layoutInflater, parent, false)

                return ComicsViewHolder(binding)
            }
        }
    }

    sealed class DataItem {
        abstract val id: Int

        data class ComicItem(val comic: Comic) : DataItem() {
            override val id = comic.id
        }

        object LoadMoreButton : DataItem() {
            override val id: Int = Int.MAX_VALUE
        }
    }

}