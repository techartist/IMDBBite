package com.webnation.imdb

import android.util.Log
import com.webnation.imdb.model.Movie
import com.webnation.imdb.util.TestUtilString
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertNotNull
import org.json.JSONException
import org.junit.Test
import java.util.*

class MovieTestGetSingle {

    @Test
    fun testGetSingle() {
        var movie = ArrayList<Movie>()
        val strJson = TestUtilString("mock_data_single.json").jsonString
        assertNotNull(strJson)
        try {
            movie = Movie.parseMovie(strJson)
        } catch (e: JSONException) {
            Log.e("MovieSingleTest", e.toString())
        }

        assertEquals(movie[0].title, "Incredibles 2")
        assertEquals(movie[0].budget, 200000000)
        assertEquals(movie[0].revenue, 485074690)


    }


}

