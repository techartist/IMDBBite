package com.webnation.imdb.presenter

import android.support.annotation.VisibleForTesting
import android.util.Log
import com.squareup.okhttp.HttpUrl
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Request
import com.webnation.imdb.interfaces.MovieDetailMVP
import com.webnation.imdb.model.Movie
import com.webnation.imdb.singleton.Constants
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.SingleObserver
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import org.json.JSONException
import java.io.IOException
import java.lang.ref.WeakReference
import java.net.HttpURLConnection

open class MovieDetailPresenter(mView: MovieDetailMVP.RequiredViewOps, schedulersIo : Scheduler, androidMainThreadScheduler: Scheduler) : MovieDetailMVP.PresenterOps {

    internal var errorMessage = ""                          //error message for displaying in alert dialogs
    internal var TAG = "MovieDetailPresenter"               //for Log messages
    private val compositeDisposable = CompositeDisposable() //disposable for RxJava
    var schedulersIo : Scheduler                   //the thread for the network call
    var androidMainThreadScheduler: Scheduler      //AndroidSchedules main thread

    // Layer View reference
    private var view: WeakReference<MovieDetailMVP.RequiredViewOps>? = null

    // Presenter reference
    init {
        this.view = WeakReference(mView)
        this.schedulersIo = schedulersIo
        this.androidMainThreadScheduler = androidMainThreadScheduler
    }

    override fun onDestroy() {
        compositeDisposable.dispose()
    }

    /**
     * create an Observer we can pass to the function getMovie()
     */
    private var singleObserverPresenter = object : SingleObserver<ArrayList<Movie>> {
        override fun onSuccess(t: ArrayList<Movie>) {
            view?.get()?.setMoviesActivityUIElements(t)
        }

        override fun onSubscribe(d: Disposable) {
            compositeDisposable.add(d)
        }

        override fun onError(e: Throwable) {
            Log.e(TAG, e.localizedMessage)
            e.printStackTrace()

        }

    }

    /**
     * get the movie from the database.
     */
    override fun getMovie(movieId: Int) {
        getMovie(movieId,null)

    }
    /**
     * does the background call to get individual movie.
     * @param movieId
     */
    @VisibleForTesting
    fun getMovie(movieId: Int,singleObserver: SingleObserver<ArrayList<Movie>>?) {
        var singleObserverPresenterLocal  = singleObserver
        if (singleObserverPresenterLocal == null) {
            singleObserverPresenterLocal = singleObserverPresenter
        }
        Single.fromCallable { getMovieFromAPI(movieId) }
                .subscribeOn(schedulersIo)
                .observeOn(androidMainThreadScheduler)
                .doOnError { throwable -> Log.e(TAG, throwable.message) }
                .subscribe(singleObserverPresenterLocal)
    }

    /**
     * the nitty gritty of doing the network call
     * @param movieId
     * @return arraylist of movies
     */
    @VisibleForTesting
    open fun getMovieFromAPI(movieId: Int): ArrayList<Movie> {
        Log.e(TAG, Thread.currentThread().toString())

        val urlIMDB = Constants.API_URL + "/" + movieId
        var movieList: ArrayList<Movie> = ArrayList()
        val urlBuilder = HttpUrl.parse(urlIMDB).newBuilder()
        urlBuilder.addQueryParameter(Constants.PARAM_LANGUAGE, Constants.PARAM_ENGLISH)
        urlBuilder.addQueryParameter(Constants.PARAM_API_KEY, Constants.APIKey)

        try {
            val url = urlBuilder.build().toString()
            val request = Request.Builder()
                    .url(url)
                    .build()
            val client = OkHttpClient()
            val response = client.newCall(request).execute()
            val stringBuilder = response.body().string()

            when (response.code()) {
                HttpURLConnection.HTTP_OK -> movieList = Movie.parseMovie(stringBuilder)

                HttpURLConnection.HTTP_NOT_FOUND, HttpURLConnection.HTTP_UNAUTHORIZED -> {
                    view?.get()?.showError(response.code())
                    errorMessage = Movie.errorMessage
                    movieList.clear()
                }
                else -> {
                    view?.get()?.showError(-1)
                    movieList.clear()
                }
            }


        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return movieList
    }


}