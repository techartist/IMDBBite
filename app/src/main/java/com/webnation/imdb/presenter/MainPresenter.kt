package com.webnation.imdb.presenter

import android.content.Context
import android.net.ConnectivityManager
import android.support.annotation.VisibleForTesting
import android.util.Log
import com.squareup.okhttp.HttpUrl
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Request
import com.webnation.imdb.R
import com.webnation.imdb.interfaces.MainMVP
import com.webnation.imdb.model.Movie
import com.webnation.imdb.singleton.Constants
import com.webnation.imdb.util.Network
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.SingleObserver
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import org.json.JSONException
import org.json.JSONObject
import java.lang.ref.WeakReference
import java.net.HttpURLConnection
import kotlin.collections.ArrayList


open class MainPresenter(mView: MainMVP.RequiredViewOps, schedulersIo : Scheduler, androidMainThreadScheduler: Scheduler) : MainMVP.PresenterOps {

    // Layer View reference
    private var view: WeakReference<MainMVP.RequiredViewOps>? = null

    private val JSON_ROOT_TOTAL_PAGES = "total_pages"       //JSON root
    private var errorMessage = ""                           //error message to give to the alert dialog
    internal var TAG = "MainPresenter"                      //for Log entries
    private val compositeDisposable = CompositeDisposable() //for the disposable rxjava
    var schedulersIo : Scheduler                   //the thread for the network call
    var androidMainThreadScheduler: Scheduler      //AndroidSchedules main thread

    // Presenter reference
    init {
        this.view = WeakReference(mView)
        this.schedulersIo = schedulersIo
        this.androidMainThreadScheduler = androidMainThreadScheduler

    }

    /**
     * used so we can send in our own observer from the unit tests
     */
    private var singleObserverPresenter = object : SingleObserver<ArrayList<Movie>> {
        override fun onSuccess(t: ArrayList<Movie>) {
            if (!t.isEmpty()) {
                view?.get()?.setUpRecyclerView(t)
            }
            view?.get()?.dismissProgressDialog()
        }

        override fun onSubscribe(d: Disposable) {
            compositeDisposable.add(d)
            view?.get()?.showProgressDialog()
        }

        override fun onError(e: Throwable) {
            Log.e(TAG, e.localizedMessage)
            e.printStackTrace()
            view?.get()?.dismissProgressDialog()
        }
    }

    /**
     * called from activity
     * @param type - String of type
     */
    override fun getMovies(type : String) {
        getMovies(type,null)
    }

    /**
     * does the background call to the API
     * @param type of movie, upcoming or now playing
     * @param singleObserver< - the single observer we are subscribing with
     */
    @VisibleForTesting
    fun getMovies(type: String, singleObserver : SingleObserver<ArrayList<Movie>>? ) {
        var singleObserverPresenterLocal  = singleObserver
        if (singleObserverPresenterLocal == null) {
            singleObserverPresenterLocal = singleObserverPresenter
        }

        Single.fromCallable { getMoviesFromIMDB(type) }
                .subscribeOn(schedulersIo)
                .observeOn(androidMainThreadScheduler)
                .doOnError { throwable -> Log.e(TAG, throwable.message) }
                .subscribe(singleObserverPresenterLocal)
    }


    /**
     * Does the actual work of doing the API call
     * @param type gets the type of movies, in this case upcoming or now playing
     * @return arraylist of movies.
     */
    @VisibleForTesting
    open fun getMoviesFromIMDB(type: String): ArrayList<Movie> {

        val urlIMDB = Constants.API_URL + "/" + type
        var pageNumber = "1"
        var numberOfPages = 1
        val movieList: ArrayList<Movie> = ArrayList()

        var i = 1
        val urlBuilder = HttpUrl.parse(urlIMDB).newBuilder()
        urlBuilder.addQueryParameter(Constants.PARAM_LANGUAGE, Constants.PARAM_ENGLISH)
        urlBuilder.addQueryParameter(Constants.PARAM_API_KEY, Constants.APIKey)
        urlBuilder.addQueryParameter(Constants.PARAM_PAGE, pageNumber)
        do {
            urlBuilder.removeAllQueryParameters(Constants.PARAM_PAGE)
            pageNumber = i.toString()


            urlBuilder.addQueryParameter(Constants.PARAM_PAGE, pageNumber)

            val url = urlBuilder.build().toString()
            Log.d(TAG, url)

            val request = Request.Builder()
                    .url(url)
                    .build()
            val client = OkHttpClient()
            val response = client.newCall(request).execute()
            val stringBuilder = response.body().string()

            when (response.code()) {
                HttpURLConnection.HTTP_OK -> {
                    if (i == 1) {
                        numberOfPages = getNumberOfPages(stringBuilder)
                    }

                    val list = doHttpCode200(stringBuilder)
                    if (list != null) movieList.addAll(list)
                }
                HttpURLConnection.HTTP_NOT_FOUND, HttpURLConnection.HTTP_UNAUTHORIZED -> {
                    errorMessage = Movie.doErrorCodes(stringBuilder)
                    view?.get()?.showError(response.code().toString())
                    movieList.clear()
                }
                else -> {
                    errorMessage = Movie.doErrorCodes(stringBuilder)
                    view?.get()?.showError(errorMessage,null)
                    movieList.clear()
                }
            }

            i++

        } while (i < numberOfPages + 1)

        return movieList

    }

    /**
     * gets the number of pages returned from the server
     * @param response from the server
     * @return number of pages.
     */
    @VisibleForTesting
    fun getNumberOfPages(response: String): Int {
        var totalNumberOfPages = 0
        try {
            val jsonObjectRoot = JSONObject(response)
            totalNumberOfPages = jsonObjectRoot.getInt(JSON_ROOT_TOTAL_PAGES)
        } catch (e: JSONException) {
            Log.e(TAG, e.toString())
        }

        return totalNumberOfPages

    }

    /**
     * parses out the movies if successful http call
     * @param response from server
     * @return array list of all the movies.
     */
    @VisibleForTesting
    fun doHttpCode200(response: String): ArrayList<Movie>? {
        var movieList: ArrayList<Movie>? = null

        try {
            movieList = Movie.parseMovies(response)

        } catch (e: JSONException) {
            Log.d("demo", e.message)
            Log.d("demo", e.localizedMessage)
            e.printStackTrace()
            errorMessage = e.toString()
            view?.get()?.showError(e.localizedMessage,null)
        }
        return movieList
    }

    /**
     * determine if we have an active connection
     * @return true if connected, false, otherwise.
     */
    val isConnectedOnline: Boolean
        get() {
            return Network.isNetworkAvailble(view?.get()?.getContext())
        }

    /**
     * cleans up the disposables and the view.
     */
    override fun onDestroy() {
        view = null
        compositeDisposable.dispose()

    }

    override fun getIsConnected(): Boolean {
        return isConnectedOnline
    }


}
