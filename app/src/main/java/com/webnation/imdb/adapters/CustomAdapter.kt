package com.webnation.imdb.adapters


import android.annotation.SuppressLint
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.webnation.imdb.R
import com.webnation.imdb.model.Movie
import com.webnation.imdb.singleton.Constants
import java.util.*

/**
 * the custom adpater for the recycler view
 */
class CustomAdapter(private var context: Context, movieList: ArrayList<Movie>) : RecyclerView.Adapter<CustomAdapter.MyViewHolder>() {

    private val moviesList: List<Movie>


    init {
        moviesList = movieList
    }

    /**
     * standard class cutomized for the view holder, holds interface for clicking on items.
     */
    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener, View.OnLongClickListener {
        var title: TextView
        var releaseDate: TextView
        var popularity: TextView
        var voteCount: TextView
        var poster: ImageView

        init {
            view.setOnClickListener(this)
            title = view.findViewById<View>(R.id.title) as TextView
            releaseDate = view.findViewById<View>(R.id.release_date) as TextView
            voteCount = view.findViewById<View>(R.id.vote_count) as TextView
            popularity = view.findViewById<View>(R.id.popularity) as TextView
            poster = view.findViewById<View>(R.id.poster) as ImageView
        }

        /**
         * handled in MainActivity
         * @param view being clicked on
         */
        override fun onClick(view: View) {
            clickListener?.onItemClick(adapterPosition, view)

        }

        /**
         * handled in MainActivity
         * @param view
         */
        override fun onLongClick(view: View): Boolean {
            clickListener?.onItemLongClick(adapterPosition, view)
            return false
        }

    }

    /**
     * sets the click listener for the recyclerview
     * @param clickListener
     */
    fun setOnItemClickListener(clickListener: ClickListener) {
        CustomAdapter.clickListener = clickListener
    }

    /**
     * handles the interface between main activity and the recycler view
     */
    interface ClickListener {
        fun onItemClick(position: Int, v: View)
        fun onItemLongClick(position: Int, v: View)
    }


    /**
     * standard ViewHolder implementation
     * @param parent - ViewGroup
     * @param viewType - not used.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.recyclerview, parent, false)

        return MyViewHolder(itemView)
    }


    /**
     * shows the movie details
     * @param holder - myViewHolder
     * @param position - position in recycler view
     */
    @SuppressLint
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val movie = moviesList[position]
        holder.title.text = movie.title
        holder.popularity.text = context.getString(R.string.space_placeholder_double, movie.popularity)
        holder.voteCount.text = context.getString(R.string.space_placeholder, movie.voteCount)
        holder.releaseDate.text = movie.releaseDateDisplay
        val options = RequestOptions()
        options.centerCrop().fitCenter()
        if (movie.posterPath != "") {
            Glide.with(context)
                    .load(Constants.IMAGE_URL + movie.posterPath)
                    .apply(options)
                    .into(holder.poster)
        } else {
            val drawable = context.resources.getDrawable(R.drawable.no_image_available)
            holder.poster.setImageDrawable(drawable)
        }

    }

    override fun getItemCount(): Int {
        return moviesList.size
    }

    companion object {
        internal var clickListener: ClickListener? = null
    }
}
