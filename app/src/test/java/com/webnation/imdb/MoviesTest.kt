package com.webnation.imdb

import android.util.Log
import com.webnation.imdb.model.Movie
import com.webnation.imdb.util.TestUtilString
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertNotNull
import org.json.JSONException
import org.junit.Test
import java.util.*

class MoviesTest {

    @Test
    fun testGetSeries() {
        var movies: List<Movie> = ArrayList()
        val strJson = TestUtilString("mock_data.json").jsonString
        assertNotNull(strJson)
        try {
            movies = Movie.parseMovies(strJson)
        } catch (e: JSONException) {
            Log.e("MovieTest", e.toString())
        }

        assertEquals(movies[0].title, "Thor: Ragnarok")
        assertEquals(movies.size, 20)

    }


}
