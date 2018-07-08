package com.webnation.imdb.singleton


/**
 * For common values across the activities
 */
object Constants {
    const val APIKey = "4a027128e091f6837b521fad33cf7be0"

    //forming URL for REST call
    const val API_URL = "https://api.themoviedb.org/3/movie"
    const val IMAGE_URL = "https://image.tmdb.org/t/p/w300"
    const val GET_METHOD = "GET"
    const val POST_METHOD = "POST"

    //URL PARAMS
    const val PARAM_LANGUAGE = "language"
    const val PARAM_ENGLISH = "en-US"
    const val PARAM_PAGE = "page"
    const val PARAM_API_KEY = "api_key"


    //Pass Around Extra
    const val KEY_MOVIE_ID = "movie_id"


    enum class REQUEST_TYPE constructor(val type: String) {
        UPCOMING("upcoming"),
        NOW_PLAYING("now_playing")

    }




}
