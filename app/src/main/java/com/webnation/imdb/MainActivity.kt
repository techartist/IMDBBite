package com.webnation.imdb

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBar
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.MenuItem
import android.view.View
import com.webnation.imdb.adapters.CustomAdapter
import com.webnation.imdb.interfaces.MainMVP
import com.webnation.imdb.model.Movie
import com.webnation.imdb.presenter.MainPresenter
import com.webnation.imdb.singleton.Constants
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.toolbar.*


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, MainMVP.RequiredViewOps {


    private val TAG = "MainActivity"
    private var actionBar: ActionBar? = null
    private var type: String = ""
    private val KEY_INSTANCE_STATE_TYPE = "type"
    private var presenter = MainPresenter(this)
    private lateinit var progressDialog : ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        type = if (savedInstanceState != null) {
            savedInstanceState.getString(KEY_INSTANCE_STATE_TYPE)
        } else {
            Constants.REQUEST_TYPE.UPCOMING.type
        }
        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar)
        actionBar = supportActionBar

        try {
            assert(actionBar != null)
            actionBar?.setDisplayHomeAsUpEnabled(true)
            actionBar?.setHomeButtonEnabled(true)
            actionBar?.subtitle = (presenter.getTitleString(type)
                    + " " + getString(R.string.movies_post_pend))
            actionBar?.setDisplayShowTitleEnabled(true)
        } catch (ignored: Exception) {
            Log.e(TAG, ignored.toString())
        }

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)


        if (isConnectedOnline) {
            presenter.getMovies(type)

        } else {
            text_view_network_not_available.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (progressDialog.isShowing) {
            progressDialog.dismiss()
        }
        presenter.onDestroy()
    }



    override fun onBackPressed() {
        val drawer = findViewById<View>(R.id.drawer_layout) as DrawerLayout
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }


    ////////presenter methods
    /**
     *
     */
    override fun showError(responseCode: Int) {
        AlertDialog.Builder(this)
                .setTitle(resources.getString(R.string.error_getting_movies))
                .setMessage(resources.getString(R.string.unspecified_error) + responseCode)
                .setNeutralButton(resources.getString(R.string.ok), { dialog, id -> dialog.dismiss() }).create()
    }

    /**
     * gets the progress dialog going here.  I'm using a progress dialog because even though it's deprecated, I like it.
     */
    @SuppressLint
    override fun setUpProgressDialog() {

        progressDialog = ProgressDialog(this)
        progressDialog.setMessage(getResources().getString(R.string.progress_dialog_message))
        progressDialog.max = 100
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        progressDialog.setCancelable(java.lang.Boolean.FALSE)

    }

    /**
     * As the name implies, shows the progress dialog
     */
    override fun showProgressDialog() {
        progressDialog.show()
    }

    /**
     * As the name implies, dismisses the dialog
     */
    override fun dismissProgressDialog() {
        if (progressDialog.isShowing) {
            progressDialog.dismiss()
        }
    }

    /**
     * sets up the custom adapter now that we have the movie list and prepares the recycler view
     */
    override fun setUpRecyclerView(movies : ArrayList<Movie>) {
        val customAdapter = CustomAdapter(this, movies)
        customAdapter.setOnItemClickListener(object : CustomAdapter.ClickListener {

            override fun onItemClick(position: Int, v: View) {
                showMovieDetailActivity(movies , position )
                Log.d(TAG, "onItemClick position: $position")
            }

            override fun onItemLongClick(position: Int, v: View) {
                showMovieDetailActivity(movies , position )
                Log.d(TAG, "onItemLongClick pos = $position")
            }
        })
        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = customAdapter

    }

    //////end of presenter methods


    /**
     * determine if we have an active connection
     */
    private val isConnectedOnline: Boolean
        get() {
            val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo = connectivityManager.activeNetworkInfo
            return networkInfo != null && networkInfo.isAvailable
        }


    /**
     * shows the detail activity for the movie
     */
    private fun showMovieDetailActivity(movies : ArrayList<Movie>, position : Int) {
        val intent = Intent(this@MainActivity, MovieDetailActivity::class.java)
        val movie = movies[position]
        intent.putExtra(Constants.KEY_MOVIE_ID, movie.id)
        startActivity(intent)

    }


    /**
     * gets activities from depending on drawer selection.
     */
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        val id = item.itemId
        type = ""

        if (isConnectedOnline) {

            if (id == R.id.nav_upcoming) {
                type = Constants.REQUEST_TYPE.UPCOMING.type

            } else if (id == R.id.nav_now_playing) {
                type = Constants.REQUEST_TYPE.NOW_PLAYING.type

            }
            presenter.getMovies(type)
            actionBar?.subtitle = presenter.getTitleString(type) + " " + getString(R.string.movies_post_pend)

        } else {
            recyclerView?.visibility = View.GONE
            text_view_network_not_available.visibility = View.VISIBLE
        }


        val drawer = findViewById<View>(R.id.drawer_layout) as DrawerLayout
        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    /**
     * save in case of rotation.
     */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putCharSequence(KEY_INSTANCE_STATE_TYPE, type)
    }







}
