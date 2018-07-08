package com.webnation.imdb.model

import android.annotation.SuppressLint
import android.util.Log

import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection

import java.text.SimpleDateFormat
import java.util.*


class Movie : Comparable<Movie> {


    var id: Int = 0
    var title = ""
    private var releaseDateString = ""
    var popularity: Double = 0.toDouble()
    var voteCount: Int = 0
    var posterPath: String = ""
    private val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val simpleDateFormatDisplay = SimpleDateFormat("MMM dd yyyy",Locale.US)
    private var releaseDate: Date = Date()
    var overview = ""
    var budget = 0L
    var revenue = 0L

    constructor(id: Int, title: String, release_date: String, popularity: Float, vote_count: Int, poster_path: String) {
        this.id = id
        this.title = title
        this.releaseDateString = release_date
        this.popularity = popularity.toDouble()
        this.voteCount = vote_count
        this.posterPath = poster_path
    }

    constructor()


    val releaseDateDisplay: String
        get() = simpleDateFormatDisplay.format(releaseDate)


    /**
     * parses the release date
     * @param release_date date
     */
    fun setReleaseDate(release_date: String) {
        this.releaseDateString = release_date
        try {
            releaseDate = simpleDateFormat.parse(release_date)


        } catch (e: Exception) {
            Log.e("DateFormatException", e.toString())
        }

    }


    /**
     * sort movies by date
     * @param movie to compare
     * @return
     */
    override fun compareTo(movie: Movie): Int {
        return movie.releaseDate.compareTo(movie.releaseDate)
    }


    /**
     * provides all the "static" methods
     */
    companion object {
        private const val JSON_ROOT_POPULARITY = "popularity"
        private const val JSON_ROOT_ID = "id"
        private const val JSON_ROOT_TITLE = "title"
        private const val JSON_ROOT_POSTER_PATH = "poster_path"
        private const val JSON_ROOT_VOTE_COUNT = "vote_count"
        private const val JSON_ROOT_RELEASE_DATE = "release_date"
        private const val JSON_ROOT_OVERVIEW = "overview"
        private const val JSON_ROOT_REVENUE = "revenue"
        private const val JSON_ROOT_BUDGET = "budget"
        private const val JSON_ROOT_NAME = "results"
        private const val JSON_RESPONSE_ROOT_STATUS_MESSAGE = "status_message"
        var errorMessage = ""
            private set


        /**
         * gets the error message
         */
        fun doErrorCodes(response: String) : String {
            var stringResult : String
            try {
                val jsonObjectRoot = JSONObject(response)
                stringResult= jsonObjectRoot.getString(JSON_RESPONSE_ROOT_STATUS_MESSAGE)
            } catch (e: JSONException) {
                errorMessage = e.localizedMessage
                stringResult = e.toString()

            }
            return stringResult

        }

        /**
         * parses many movies
         * @param inputStreamString
         * @return arraylist of movies
         */
        @Throws(JSONException::class)
        fun parseMovies(inputStreamString: String): ArrayList<Movie> {
            val arrayListMovies = ArrayList<Movie>()

            val jsonObjectRoot = JSONObject(inputStreamString)
            val jsonArrayMovies = jsonObjectRoot.getJSONArray(JSON_ROOT_NAME)

            for (i in 0 until jsonArrayMovies.length()) {
                val movieJSONObject = jsonArrayMovies.getJSONObject(i)
                val movie = createMovieFromJSON(movieJSONObject)
                arrayListMovies.add(movie)
            }


            return arrayListMovies
        }

        /**
         * parses a single movie
         * @param inputStreamString
         * @return arraylist of movies
         */
        @Throws(JSONException::class)
        fun parseMovie(inputStreamString: String): ArrayList<Movie> {
            val arrayListMovies = ArrayList<Movie>()

            val jsonObjectRoot = JSONObject(inputStreamString)
            val movie = createMovieFromJSON(jsonObjectRoot)
            arrayListMovies.add(movie)


            return arrayListMovies
        }

        /**
         * As the name implies, gets the movie object from the Json
         * @param movieJSONObject
         * @return movie
         */
        @Throws(JSONException::class)
        private fun createMovieFromJSON(movieJSONObject: JSONObject): Movie {
            val movie = Movie()

            try {
                movie.title = movieJSONObject.getString(JSON_ROOT_TITLE)
                movie.setReleaseDate(movieJSONObject.getString(JSON_ROOT_RELEASE_DATE))
                movie.id = movieJSONObject.getInt(JSON_ROOT_ID)
                movie.posterPath = movieJSONObject.getString(JSON_ROOT_POSTER_PATH)
                movie.voteCount = movieJSONObject.getInt(JSON_ROOT_VOTE_COUNT)
                movie.popularity = movieJSONObject.getDouble(JSON_ROOT_POPULARITY)
                movie.overview = movieJSONObject.getString(JSON_ROOT_OVERVIEW)
                if (movieJSONObject.has(JSON_ROOT_BUDGET)) {
                    movie.budget = movieJSONObject.getLong(JSON_ROOT_BUDGET)
                }
                if (movieJSONObject.has (JSON_ROOT_REVENUE)) {
                    movie.revenue = movieJSONObject.getLong(JSON_ROOT_REVENUE)
                }
            } catch (e: JSONException) {
                e.printStackTrace()
                errorMessage = e.localizedMessage
            }

            return movie
        }

    }
}
