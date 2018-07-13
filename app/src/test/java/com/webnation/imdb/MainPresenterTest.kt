package com.webnation.imdb

import android.util.Log
import com.webnation.imdb.interfaces.MainMVP
import com.webnation.imdb.model.Movie
import com.webnation.imdb.presenter.MainPresenter
import com.webnation.imdb.util.TestUtilString
import io.reactivex.Scheduler
import io.reactivex.observers.TestObserver
import io.reactivex.schedulers.TestScheduler
import junit.framework.Assert
import org.json.JSONException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.verify


class MainPresenterTest {

    lateinit var mockView: MainMVP.RequiredViewOps  //mocks up the main activity
    var testScheduler: Scheduler = TestScheduler()  // Mock scheduler using RxJava TestScheduler.
    var movies: ArrayList<Movie> = ArrayList()      //the list of movies
    private val type = "Upcoming"                   //whether or not the mvoes are upcoming
    var spy : MainPresenter? = null                 //will do all the work
    private var strJson = ""                        //test response

    @Before
    fun setup() {

        // Creating the mocks
        mockView = Mockito.mock<MainMVP.RequiredViewOps>(MainMVP.RequiredViewOps::class.java)
        // Pass the mocks to a Presenter instance
        spy = Mockito.spy(MainPresenter(mockView,testScheduler,testScheduler))
        Mockito.doReturn(movies).`when`(spy)?.getMoviesFromIMDB(type)

        strJson = TestUtilString("mock_data.json").jsonString
        Assert.assertNotNull(strJson)
        try {
            movies = Movie.parseMovies(strJson)
            Assert.assertTrue(!movies.isEmpty())
        } catch (e: JSONException) {
            Log.e("MainPresenterTest", e.toString())
        }

    }

    /**
     * tests the main call to the API
     */
    @Test
    fun loadItems_WhenDataIsAvailable_ShouldUpdateViews() {
        spy?.getMovies(type)
        verify(mockView).showProgressDialog()
    }

    /**
     * Tests for success of the observable.  We tested the onSubscribe, now
     * test for errors.
     */
    @Test
    fun testgetMovies() {
        val testobserver = TestObserver<ArrayList<Movie>>()
        spy?.getMovies(type,testobserver)
        testobserver.assertNoErrors()
    }

    /**
     * tests for the number of pages
     */
    @Test
    fun testNumberOfPages() {
        val numberOfPages = spy?.getNumberOfPages(strJson)
        assertEquals(numberOfPages,17)
    }

    /**
     * tests the presenter parsing.
     */
    @Test
    fun doHttpErrorCodesTest() {
        val movies = spy?.doHttpCode200(strJson)
        assertNotNull(movies)
        Assert.assertEquals(movies?.get(0)?.title, "Thor: Ragnarok")
        Assert.assertEquals(movies?.size, 20)
    }


}