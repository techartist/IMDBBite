package com.webnation.imdb.interfaces

import com.webnation.imdb.model.Movie

interface MovieDetailMVP {
    /**
     * View mandatory methods. Available to Presenter
     * Presenter -> View
     */
    interface RequiredViewOps {
        fun showError(responseCode : Int)
        fun showError(message : String)
        fun setMoviesActivityUIElements(movies : ArrayList<Movie>)
    }

    /**
     * Operations offered from Presenter to View
     * View -> Presenter
     */
    interface PresenterOps {
        fun onDestroy()
        fun getMovie(movieId : Int)

    }

}