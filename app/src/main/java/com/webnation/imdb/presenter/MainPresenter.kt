package com.webnation.imdb.presenter

import android.util.Log
import com.squareup.okhttp.HttpUrl
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Request
import com.webnation.imdb.interfaces.MainMVP
import com.webnation.imdb.model.Movie
import com.webnation.imdb.singleton.Constants
import io.reactivex.Single
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.json.JSONException
import org.json.JSONObject
import java.lang.ref.WeakReference
import java.net.HttpURLConnection
import java.util.*


class MainPresenter(mView: MainMVP.RequiredViewOps) : MainMVP.PresenterOps {

    // Layer View reference
    private var view: WeakReference<MainMVP.RequiredViewOps>? = null

    private val JSON_ROOT_TOTAL_PAGES = "total_pages"
    private var errorMessage = ""
    internal var TAG = "MainPresenter"
    private val compositeDisposable = CompositeDisposable()
    // Presenter reference
    init {
        this.view = WeakReference(mView)
    }


    /**
     * does the background call to the API
     * @param type of movie, upcoming or now playing
     */
    override fun getMovies(type : String) {

        Single.fromCallable{getMoviesFromIMDB(type)}
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError { throwable -> Log.e(TAG, throwable.message) }
                .unsubscribeOn(Schedulers.io())
                .subscribe(object : SingleObserver<ArrayList<Movie>> {
                    override fun onSuccess(t: ArrayList<Movie>) {
                        if (!t.isEmpty()) {
                            view?.get()?.setUpRecyclerView(t)
                        }
                        view?.get()?.dismissProgressDialog()
                    }

                    override fun onSubscribe(d: Disposable) {
                        compositeDisposable.add(d)
                        view?.get()?.setUpProgressDialog()
                        view?.get()?.showProgressDialog()
                    }

                    override fun onError(e: Throwable) {
                        Log.e(TAG,e.localizedMessage)
                        e.printStackTrace()
                        view?.get()?.dismissProgressDialog()
                    }

                })


    }


    /**
     * Does the actual work of doing the API call
     * @param type gets the type of movies, in this case upcoming or now playing
     * @return arraylist of movies.
     */
   private fun getMoviesFromIMDB(type : String) : ArrayList<Movie> {

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
            Log.d(TAG,url)

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
                    movieList.clear()
                    view?.get()?.showError(response.code())

                }
                else -> {
                    view?.get()?.showError(response.code())
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
    private fun getNumberOfPages(response: String): Int {
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
    private fun doHttpCode200(response: String): ArrayList<Movie>? {
        var movieList : ArrayList<Movie>? = null

        try {
            movieList = Movie.parseMovies(response)

        } catch (e: JSONException) {
            Log.d("demo", e.message)
            Log.d("demo", e.localizedMessage)
            e.printStackTrace()
            errorMessage = e.toString()
        }
        return movieList


    }

    /**
     * parses out the title from the String to handle unique captialization
     * @param title in raw form
     * @return nicely formatted title
     */
    override fun getTitleString(title: String): String {
        var string = title
        val actionableDelimiters = " '-/"
        string = string.replace("_", " ")
        var sb = StringBuilder()
        if (!string.isEmpty()) {
            var capitaliseNext = true
            for (c in string.toCharArray()) {
                var char: Char
                char = if (capitaliseNext) Character.toUpperCase(c) else Character.toLowerCase(c)
                sb.append(char)
                capitaliseNext = actionableDelimiters.indexOf(char) >= 0
            }
            string = sb.toString()
            if (string.startsWith("Mc") && string.length > 2) {
                val char = string[2]
                if (actionableDelimiters.indexOf(char) < 0) {
                    sb = StringBuilder()
                    sb.append(string.substring(0, 2))
                    sb.append(string.substring(2, 3).toUpperCase())
                    sb.append(string.substring(3))
                    string = sb.toString()
                }
            } else if (string.startsWith("Mac") && string.length > 3) {
                val c = string[3]
                if (actionableDelimiters.indexOf(c) < 0) {
                    sb = StringBuilder()
                    sb.append(string.substring(0, 3))
                    sb.append(string.substring(3, 4).toUpperCase())
                    sb.append(string.substring(4))
                    string = sb.toString()
                }
            }
        }
        return string
    }



    override fun onDestroy() {
        view = null
        compositeDisposable.dispose()

    }




}
