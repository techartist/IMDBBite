package com.webnation.imdb

import android.util.Log
import com.nhaarman.mockito_kotlin.any
import com.webnation.imdb.interfaces.MovieDetailMVP
import com.webnation.imdb.model.Movie
import com.webnation.imdb.presenter.MovieDetailPresenter
import com.webnation.imdb.util.TestUtilString
import io.reactivex.observers.TestObserver
import io.reactivex.schedulers.Schedulers
import junit.framework.Assert
import org.json.JSONException
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito


class MovieDetailPresenterTest {

    lateinit var mockView: MovieDetailMVP.RequiredViewOps  //mocks up the main activity
    var movies: ArrayList<Movie> = ArrayList()      //the list of movies
    private val movieId = 468222                    //the mock movie Id
    var spy : MovieDetailPresenter? = null          //will do all the work
    private var strJson = ""                        //test response

    @Before
    fun setup() {
        // Creating the mocks
        mockView = Mockito.mock<MovieDetailMVP.RequiredViewOps>(MovieDetailMVP.RequiredViewOps::class.java)
        // Pass the mocks to a Presenter instance
        spy = Mockito.spy(MovieDetailPresenter(mockView,Schedulers.trampoline(),Schedulers.trampoline()))
        Mockito.doReturn(movies).`when`(spy)?.getMovieFromAPI(movieId)

        strJson = TestUtilString("mock_data_single.json").jsonString
        Assert.assertNotNull(strJson)
        try {
            movies = Movie.parseMovie(strJson)
            Assert.assertTrue(!movies.isEmpty())
        } catch (e: JSONException) {
            Log.e("MovieDetailPresenterTest", e.toString())
        }
    }

    /**
     * tests the main call to the API
     */
    @Test
    fun loadItems_WhenDataIsAvailable_ShouldUpdateViews() {
        spy?.getMovie(movieId)
        Mockito.verify(mockView).setMoviesActivityUIElements(any())
    }

    /**
     * Tests for success of the observable.  We tested the onSubscribe, now
     * test for errors.
     */
    @Test
    fun testgetMovieNoError() {
        val testobserver = TestObserver<ArrayList<Movie>>()
        spy?.getMovie(movieId,testobserver)
        testobserver.assertNoErrors()
    }

}