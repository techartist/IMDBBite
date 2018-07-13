package com.webnation.imdb.adapters


import android.annotation.SuppressLint
import android.content.Context
import android.support.v4.content.ContextCompat
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
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.*

/**
 * the custom adpater for the recycler view
 */
class CustomAdapter(private var context: Context, movieList: ArrayList<Movie>) : RecyclerView.Adapter<CustomAdapter.MyViewHolder>() {

    private val moviesList: List<Movie>  //list of movies to be presented to user

    init {
        moviesList = movieList
    }

    /**
     * standard class cutomized for the view holder, holds interface for clicking on items.
     */
    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener, View.OnLongClickListener {
        var title: TextView             //title of movie
        var releaseDate: TextView       //release date of move
        var popularity: TextView        //popularity voted by users
        var voteCount: TextView         //actual vote count
        var poster: ImageView           //movie poster graphic

        init {
            view.setOnClickListener(this)
            title = view.findViewById(R.id.title)
            releaseDate = view.findViewById(R.id.release_date)
            voteCount = view.findViewById(R.id.vote_count)
            popularity = view.findViewById(R.id.popularity)
            poster = view.findViewById(R.id.poster)
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
         * @param view being clicked on
         */
        override fun onLongClick(view: View): Boolean {
            clickListener?.onItemLongClick(adapterPosition, view)
            return false
        }
    }

    /**
     * sets the click listener for the recyclerview
     * @param clickListener listener that listens to regular and long clicks
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

        val df = DecimalFormat("#.##")
        df.roundingMode = RoundingMode.CEILING
        holder.popularity.text = context.getString(R.string.money_placeholder, df.format(movie.popularity))
        holder.voteCount.text = context.getString(R.string.space_placeholder, movie.vote_count)
        holder.releaseDate.text = movie.releaseDateDisplay
        val options = RequestOptions()
        options.centerCrop().fitCenter()
        if (movie.poster_path != "") {
            Glide.with(context)
                    .load(Constants.IMAGE_URL + movie.poster_path)
                    .apply(options)
                    .into(holder.poster)
        } else {
            val drawable = ContextCompat.getDrawable(context, R.drawable.no_image_available)
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
