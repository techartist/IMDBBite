package com.webnation.imdb.interfaces

import android.content.Context
import com.webnation.imdb.model.Movie

/*
 * Aggregates all communication operations between MVP pattern layer:
 * Model, View and Presenter
 */
interface MainMVP {

    /**
     * View mandatory methods. Available to Presenter
     * Presenter -> View
     */
    interface RequiredViewOps {
        fun showProgressDialog()
        fun dismissProgressDialog()
        fun setUpRecyclerView(movies: ArrayList<Movie>)
        fun showError(responseCode: String)
        fun showError(message : String, title : String?)
        fun getContext() : Context
    }

    /**
     * Operations offered from Presenter to View
     * View -> Presenter
     */
    interface PresenterOps {
        fun onDestroy()
        fun getMovies(type: String)
        fun getIsConnected() : Boolean
    }


}