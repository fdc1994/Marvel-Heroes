package com.example.marvelheroes.fragments.heroDetailsFragment


import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.core.view.isGone
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.marvelheroes.R
import com.example.marvelheroes.databinding.ListCharacterDetailsBinding
import com.example.marvelheroes.model.comic.Comic
import com.example.marvelheroes.model.event.Event
import com.example.marvelheroes.model.series.Series
import com.example.marvelheroes.model.story.Story


class HeroDetailsAdapter(
    private val dataset: List<Any>,
) : RecyclerView.Adapter<HeroDetailsAdapter.CharacterDetailsViewHolder>() {
    private val flags = mutableListOf(true, true, true, true)

    class CharacterDetailsViewHolder(val binding: ListCharacterDetailsBinding) :
        RecyclerView.ViewHolder(binding.root)

    /**
     * Create new views (invoked by the layout manager)
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CharacterDetailsViewHolder {
        val binding = ListCharacterDetailsBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)

        return CharacterDetailsViewHolder(binding)
    }

    /**
     * Replace the contents of a view (invoked by the layout manager)
     */
    override fun onBindViewHolder(holder: CharacterDetailsViewHolder, position: Int) {
        with(holder) {
            val count = dataset.size - 1
            Log.i("Count Adapter", count.toString())
            Log.i(" Position", position.toString())
            when (val marvelChar = dataset[position]) {

                is Comic -> {
                    if (flags[0]) {
                        binding.sectionContainer.isGone = false
                        binding.sectionTitle.text =
                            holder.itemView.context.resources.getString(R.string.comics)
                        flags[0] = false
                    }
                    try {
                        val imgUrl = "${marvelChar.thumbnail.path}.${marvelChar.thumbnail.extension}"

                        //Glide apparently needs a secure connection (https)? This line changes the http to https
                        val imgUri = imgUrl.toUri().buildUpon().scheme("https").build()

                        Glide.with(holder.itemView.context)
                            .load(imgUri)
                            .into(binding.marvelImageDetails)
                    }catch (e:Exception) {
                        binding.marvelImageDetails.isGone = true
                        Log.e("URI Build Error", e.message.toString())
                        Log.e("URI Build Link", "${marvelChar?.thumbnail?.path}.${marvelChar?.thumbnail?.extension}")
                    }

                    binding.characterDetailTitle.text = marvelChar.title
                    binding.characterDetailDescription.text = marvelChar.description
                    if (position < count) {
                        when (dataset[position + 1]) {
                            !is Comic -> binding.individualDivider.isGone = true
                        }
                    }


                }
                is Event -> {
                    if (flags[1]) {
                        binding.sectionContainer.isGone = false
                        binding.sectionTitle.text =
                            holder.itemView.context.resources.getString(R.string.events)
                        flags[1] = false
                    }
                    binding.characterDetailTitle.text = marvelChar.title
                    if(!marvelChar.description.isNullOrEmpty()) {
                        binding.characterDetailDescription.text = marvelChar.description
                    }else {
                        binding.characterDetailDescription.text =  holder.itemView.context.resources.getString(R.string.no_information)
                    }
                    try {
                        val imgUrl = "${marvelChar.thumbnail.path}.${marvelChar.thumbnail.extension}"

                        //Glide apparently needs a secure connection (https)? This line changes the http to https
                        val imgUri = imgUrl.toUri().buildUpon().scheme("https").build()

                        Glide.with(holder.itemView.context)
                            .load(imgUri)
                            .into(binding.marvelImageDetails)
                    }catch (e:Exception) {
                        binding.marvelImageDetails.isGone = true
                        Log.e("URI Build Error", e.message.toString())
                        Log.e("URI Build Link", "${marvelChar?.thumbnail?.path}.${marvelChar?.thumbnail?.extension}")
                    }

                    if (position < count) {
                        when (dataset[position + 1]) {
                            !is Event -> binding.individualDivider.isGone = true
                        }
                    }

                }
                is Series -> {
                    if (flags[2]) {
                        binding.sectionContainer.isGone = false
                        binding.sectionTitle.text =
                            holder.itemView.context.resources.getString(R.string.series)
                        flags[2] = false
                    }
                    binding.characterDetailTitle.text = marvelChar.title
                    if(!marvelChar.description.isNullOrEmpty()) {
                        binding.characterDetailDescription.text = marvelChar.description
                    }else {
                        binding.characterDetailDescription.text =  holder.itemView.context.resources.getString(R.string.no_information)
                    }
                    try {
                        val imgUrl = "${marvelChar.thumbnail.path}.${marvelChar.thumbnail.extension}"

                        //Glide apparently needs a secure connection (https)? This line changes the http to https
                        val imgUri = imgUrl.toUri().buildUpon().scheme("https").build()

                        Glide.with(holder.itemView.context)
                            .load(imgUri)
                            .into(binding.marvelImageDetails)
                    }catch (e:Exception) {
                        binding.marvelImageDetails.isGone = true
                        Log.e("URI Build Error", e.message.toString())
                        Log.e("URI Build Link", "${marvelChar?.thumbnail?.path}.${marvelChar?.thumbnail?.extension}")
                    }
                    if (position < count) {
                        when (dataset[position + 1]) {
                            !is Series -> binding.individualDivider.isGone = true
                        }
                    }
                }
                is Story -> {
                    if (flags[3]) {
                        binding.sectionContainer.isGone = false
                        binding.sectionTitle.text =
                            holder.itemView.context.resources.getString(R.string.stories)
                        flags[3] = false
                    }
                    binding.characterDetailTitle.text = marvelChar.title
                    if(!marvelChar.description.isNullOrEmpty()) {
                        binding.characterDetailDescription.text = marvelChar.description
                    }else {
                        binding.characterDetailDescription.text =  holder.itemView.context.resources.getString(R.string.no_information)
                    }
                    try {
                        val imgUrl = "${marvelChar.thumbnail.path}.${marvelChar.thumbnail.extension}"

                        //Glide apparently needs a secure connection (https)? This line changes the http to https
                        val imgUri = imgUrl.toUri().buildUpon().scheme("https").build()

                        Glide.with(holder.itemView.context)
                            .load(imgUri)
                            .into(binding.marvelImageDetails)
                    }catch (e:Exception) {
                        binding.marvelImageDetails.isGone = true
                        Log.e("URI Build Error", e.message.toString())
                        Log.e("URI Build Link", "${marvelChar?.thumbnail?.path}.${marvelChar?.thumbnail?.extension}")
                    }

                    if (position < count) {
                        when (dataset[position + 1]) {
                            !is Story -> binding.individualDivider.isGone = true
                        }
                    }
                }
                else -> {

                    binding.characterDetailTitle.text =
                        holder.itemView.context.resources.getString(R.string.no_information)
                    binding.sectionContainer.isGone= true
                    binding.detailContainer.isGone = true
                }
            }
        }


    }

    override fun getItemCount() = dataset.size
}