package com.webnation.imdb.model

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*


data class Movie(
        var id: Int,                    //movieId
        var title: String,              //movie Title
        var release_date: String,       //release date of movie
        var popularity: Double,         //popularity
        var vote_count: Int = 0,        //vote count by IMDB users
        var poster_path: String = "")   //url to poster graphic
{

    var overview = ""                       //short snopsis of movie
    var budget = 0L                         //budget, in US dollars
    var revenue = 0L                        //revenue to date in US dollars
    val releaseDateDisplay: String
        get() = simpleDateFormatDisplay.format(releaseDate) //display of release date


    private val simpleDateFormatDisplay = SimpleDateFormat("MMM dd yyyy", Locale.US)  //Date display format
    private var releaseDate: Date = Date()  // release date Date object

    constructor() : this(0, "", "", 0.0, 0, "")

    /**
     * provides all the "static" methods
     */
    companion object {
        private const val JSON_RESPONSE_ROOT_STATUS_MESSAGE = "status_message" //get the status message of the call
        var errorMessage = ""
            private set

        /**
         * gets the error message
         * @param response - raw response from the API
         * @return error codes if there are any
         */
        fun doErrorCodes(response: String): String {
            var stringResult: String
            try {
                val jsonObjectRoot = JSONObject(response)
                stringResult = jsonObjectRoot.getString(JSON_RESPONSE_ROOT_STATUS_MESSAGE)
            } catch (e: JSONException) {
                errorMessage = e.localizedMessage
                stringResult = e.toString()

            }
            return stringResult

        }

        /**
         * parses many movies
         * @param inputStreamString //raw response from the API
         * @return arraylist of movies
         */
        fun parseMovies(inputStreamString: String): ArrayList<Movie> {
            val gson = Gson()
            val movieResponseType = object : TypeToken<MovieResponse>() {
            }.getType()
            val jsonMovieResponse = gson.fromJson<MovieResponse>(inputStreamString,movieResponseType)
            return jsonMovieResponse.results
        }

        /**
         * parses a single movie
         * @param inputStreamString //raw response from the API
         * @return arraylist of movies
         */
        fun parseMovie(inputStreamString: String): ArrayList<Movie> {
            val arrayListMovies = ArrayList<Movie>()
            val gson = Gson()
            val movieResponseType = object : TypeToken<Movie>() {
            }.getType()
            val jsonMovieResponse = gson.fromJson<Movie>(inputStreamString,movieResponseType)
            arrayListMovies.add(jsonMovieResponse)
            return arrayListMovies
        }

    }
}
