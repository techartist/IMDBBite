package com.webnation.imdb.presenter

import android.util.Log
import com.squareup.okhttp.HttpUrl
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Request
import com.webnation.imdb.interfaces.MovieDetailMVP
import com.webnation.imdb.model.Movie
import com.webnation.imdb.singleton.Constants
import io.reactivex.Single
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.json.JSONException
import java.io.IOException
import java.lang.ref.WeakReference
import java.net.HttpURLConnection
import java.util.*

class MovieDetailPresenter(mView: MovieDetailMVP.RequiredViewOps) : MovieDetailMVP.PresenterOps {

    internal var errorMessage = ""
    internal var TAG = "MovieDetailPresenter"
    private val compositeDisposable = CompositeDisposable()

    // Layer View reference
    private var view: WeakReference<MovieDetailMVP.RequiredViewOps>? = null

    // Presenter reference
    init {
        this.view = WeakReference(mView)
    }


    override fun onDestroy() {
        compositeDisposable.dispose()
    }

    /**
     * does the background call to get individual movie.
     * @param movieId
     */
    override fun getMovie(movieId: Int) {
        Single.fromCallable { getMovieFromAPI(movieId) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError { throwable -> Log.e(TAG, throwable.message) }
                .unsubscribeOn(Schedulers.io())
                .subscribe(object : SingleObserver<ArrayList<Movie>> {
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

                })
    }

    /**
     * the nitty gritty of doing the network call
     * @param movieId
     * @return arraylist of movies
     */
    private fun getMovieFromAPI(movieId: Int): ArrayList<Movie> {
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