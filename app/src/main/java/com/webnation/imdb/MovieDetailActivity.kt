package com.webnation.imdb

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.ActionBar
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import com.bumptech.glide.Glide
import com.webnation.imdb.interfaces.MovieDetailMVP
import com.webnation.imdb.model.Movie
import com.webnation.imdb.presenter.MovieDetailPresenter
import com.webnation.imdb.singleton.Constants
import com.webnation.imdb.util.FormatNumbers
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.content_movie_detail.*
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.*

class MovieDetailActivity : AppCompatActivity(), MovieDetailMVP.RequiredViewOps {
    lateinit var presenter :  MovieDetailPresenter          //presenter for this activity
    private var movieId = -1                                //movie id to pass to API
    private var actionBar: ActionBar? = null                //action bar
    private val TAG = "MovieDetailActivity"                 //Log messaging tags
    private val KEY_INSTANCE_STATE_MOVIE_ID = "movie_id"    //key to be retienved from saved instance state

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            movieId = savedInstanceState.getInt(KEY_INSTANCE_STATE_MOVIE_ID)
        } else {
            if (intent != null)
                movieId = intent.getIntExtra(Constants.KEY_MOVIE_ID, -1)
        }

        setContentView(R.layout.activity_movie_detail)

        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        actionBar = supportActionBar

        try {
            assert(actionBar != null)
            actionBar?.setDisplayHomeAsUpEnabled(true)
            actionBar?.setHomeButtonEnabled(true)
            actionBar?.setDisplayShowTitleEnabled(true)
        } catch (ignored: Exception) {
            Log.e(TAG, ignored.toString())
        }
        presenter = MovieDetailPresenter(this,Schedulers.io(),AndroidSchedulers.mainThread())
        presenter.getMovie(movieId)
    }

    /**
     * handle home button
     */
    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        if (menuItem.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(menuItem)
    }

    /**
     * handle rotations
     */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(KEY_INSTANCE_STATE_MOVIE_ID, movieId)
    }

    /**
     * clean up presenter
     */
    override fun onDestroy() {
        super.onDestroy()
        presenter.onDestroy()
    }

    ////////////////presenter call backs
    /**
     * shows the Error message from bad network calls.
     * @param responseCode
     */
    override fun showError(responseCode: Int) {
        var message = resources.getString(R.string.unspecified_error)
        if (responseCode > -1) {
            message = resources.getString(R.string.unspecified_error) + responseCode
        }
        displayAlertMessage(message)
    }

    /**
     * shows the Error message from bad network calls.
     * @param - string of message
     */
    override fun showError(message: String) {
        displayAlertMessage(message)
    }

    /**
     * Sets up the UI with data retireved from the API.
     * @param movies - arraly list of movies to be displayed
     */
    @SuppressLint
    override fun setMoviesActivityUIElements(movies: ArrayList<Movie>) {
        if (!movies.isEmpty()) {
            val movie = movies[0]
            val defaultStringIfZero = resources.getString(R.string.defaultStringIfZero)
            val title = findViewById<TextView>(R.id.title) as TextView
            title.visibility = View.INVISIBLE
            val df = DecimalFormat("#.##")
            df.roundingMode = RoundingMode.CEILING
            popularity.text = resources.getString(R.string.money_placeholder, df.format(movie.popularity))
            vote_count.text = resources.getString(R.string.space_placeholder, movie.vote_count)
            revenue.text = resources.getString(R.string.money_placeholder, FormatNumbers.formatValue(movie.revenue, defaultStringIfZero))
            budget.text = resources.getString(R.string.money_placeholder, FormatNumbers.formatValue(movie.budget, defaultStringIfZero))
            overview.text = movie.overview

            release_date.text = movie.releaseDateDisplay
            if (movie.poster_path != "") {
                Glide.with(this)
                        .load(Constants.IMAGE_URL + movie.poster_path)
                        .into(poster)
            } else {
                val drawable = ContextCompat.getDrawable(this, R.drawable.no_image_available)
                poster.setImageDrawable(drawable)
            }
            try {
                assert(actionBar != null)
                actionBar?.title = movie.title
            } catch (ignored: Exception) {
                Log.e(TAG, ignored.toString())
            }
        } else {
            relative_layout.visibility = View.GONE
            error_message.visibility = View.VISIBLE
            error_message.text = presenter.errorMessage
        }
    }
    /////////////end of presenter methods

    /**
     * As the name implies, shows alert dialgo, called from showError
     * @param message -- to be displayed
     */
    private fun displayAlertMessage(message: String) {
        AlertDialog.Builder(this)
                .setTitle(resources.getString(R.string.error_getting_movies))
                .setMessage(message)
                .setNeutralButton(resources.getString(R.string.ok), { dialog, _ -> dialog.dismiss() }).create().show()
    }


}
