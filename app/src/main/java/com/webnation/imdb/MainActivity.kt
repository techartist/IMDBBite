package com.webnation.imdb

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.content.ContextCompat.startActivity
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
import com.webnation.imdb.R.id.recyclerView
import com.webnation.imdb.adapters.CustomAdapter
import com.webnation.imdb.interfaces.MainMVP
import com.webnation.imdb.model.Movie
import com.webnation.imdb.presenter.MainPresenter
import com.webnation.imdb.receivers.NetworkAvailableReceiver
import com.webnation.imdb.singleton.Constants
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.toolbar.*


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, MainMVP.RequiredViewOps {

    private val TAG = "MainActivity"                //Tag for Log entries
    private var actionBar: ActionBar? = null        //Action bar
    private var type: String = ""                   //type of movie to be passed as part of URL
    private var friendlyName = ""                   //friendly name of type of moview
    private val KEY_INSTANCE_STATE_TYPE = "type"    //key to get save instance state
    lateinit var presenter : MainPresenter          // presenter
    private lateinit var progressDialog : ProgressDialog    //shows dialog for when the recycler view is loading
    private val networkAvailableReceiver = NetworkAvailableReceiver() //the receiver that listens for changes on network.

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            type = savedInstanceState.getString(KEY_INSTANCE_STATE_TYPE)
            if (type.equals(Constants.REQUEST_TYPE.UPCOMING.type)) friendlyName = Constants.REQUEST_TYPE.UPCOMING.getFriendlyName()
            else {friendlyName = Constants.REQUEST_TYPE.NOW_PLAYING.getFriendlyName()}
        } else {
            type = Constants.REQUEST_TYPE.UPCOMING.type
            friendlyName = Constants.REQUEST_TYPE.UPCOMING.getFriendlyName()
        }
        friendlyName = Constants.REQUEST_TYPE.UPCOMING.getFriendlyName()
        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar)
        actionBar = supportActionBar

        try {
            assert(actionBar != null)
            actionBar?.setDisplayHomeAsUpEnabled(true)
            actionBar?.setHomeButtonEnabled(true)
            actionBar?.subtitle = friendlyName + " " + getString(R.string.movies_post_pend)
            actionBar?.setDisplayShowTitleEnabled(true)
        } catch (ignored: Exception) {
            Log.e(TAG, ignored.toString())
        }

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)
        presenter = MainPresenter(this, Schedulers.io(), AndroidSchedulers.mainThread()) //for testing
        setUpProgressDialog()

        if (presenter.getIsConnected()) {
            presenter.getMovies(type)

        } else {
            showError(resources.getString(R.string.network_not_available),resources.getString(R.string.network_not_available_title))
            recyclerView.visibility = View.GONE
        }
    }

    /**
     * this receive receives intents from the broadcast receiver that tests to see if
     * the network is connected.
     */
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val extras = intent.extras

            if (extras != null && extras.getBoolean(Constants.KEY_CONNECTED,false)) {
                if (!(recyclerView.visibility == View.VISIBLE)) {
                    presenter.getMovies(type)
                }
            } else {
                showError(resources.getString(R.string.network_not_available),resources.getString(R.string.network_not_available_title))
                recyclerView.visibility = View.GONE
            }
        }
    }


    /**
     * register the receiver to receive intents from our broadcast receiver
     */
    override fun onStart() {
        super.onStart()
        val filterConnectivityFilter = IntentFilter("android.net.conn.CONNECTIVITY_CHANGE")
        registerReceiver(networkAvailableReceiver,filterConnectivityFilter)
        val filter = IntentFilter("com.webnation.imdb.MainActivity");
        registerReceiver(receiver, filter)

    }

    /**
     * unregister the recievers.
     */
    override fun onStop() {
        super.onStop()
        unregisterReceiver(receiver)
        unregisterReceiver(networkAvailableReceiver)
    }

    /**
     * clean up progress dialogs, call presenter destroy
     */
    override fun onDestroy() {
        super.onDestroy()
        if (progressDialog.isShowing) {
            progressDialog.dismiss()
        }
        presenter.onDestroy()
    }

    /**
     * handle presses by user, close drawer if open
     */
    override fun onBackPressed() {
        val drawer = findViewById<View>(R.id.drawer_layout) as DrawerLayout
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    /**
     * handle navigation in the drawer
     */
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        val id = item.itemId
        type = ""

        if (presenter.isConnectedOnline) {

            if (id == R.id.nav_upcoming) {
                type = Constants.REQUEST_TYPE.UPCOMING.type

            } else if (id == R.id.nav_now_playing) {
                type = Constants.REQUEST_TYPE.NOW_PLAYING.type

            }
            presenter.getMovies(type)
            actionBar?.subtitle = friendlyName + " " + getString(R.string.movies_post_pend)

        } else {

        }

        val drawer = findViewById<View>(R.id.drawer_layout) as DrawerLayout
        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    /**
     * save instance state on rotation
     */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putCharSequence(KEY_INSTANCE_STATE_TYPE, type)
    }

    //////////////////////presenter methods
    /**
     * displays errors from retrieving data from the API
     */
    override fun showError(responseCode: String) {
        AlertDialog.Builder(this)
                .setTitle(resources.getString(R.string.error_getting_movies))
                .setMessage(resources.getString(R.string.unspecified_error) + " " + responseCode)
                .setNeutralButton(resources.getString(R.string.ok)) { dialog, _ -> dialog.dismiss() }.create().show()
    }

    /**
     * general purpose other messages
     */
    override fun showError(message: String, title : String?) {
        var titleLocal = title
        if (titleLocal == null) {
            titleLocal = resources.getString(R.string.error)
        }
        AlertDialog.Builder(this)
                .setTitle(titleLocal)
                .setMessage(message)
                .setNeutralButton(resources.getString(R.string.ok)) { dialog, _ -> dialog.dismiss() }.create().show()
    }

    /**
     * gets the progress dialog going here.  I'm using a progress dialog because even though it's deprecated, I like it.
     */
    @SuppressLint("ProgressDialog")
    fun setUpProgressDialog() {
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
        if (!progressDialog.isShowing) {
            progressDialog.show()
        }
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
     * @param movies list of movies retrieved from the api
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

        recyclerView.layoutManager = LinearLayoutManager(this ,LinearLayoutManager.VERTICAL ,false)
        recyclerView.adapter = customAdapter
        recyclerView.visibility = View.VISIBLE

    }

    override fun getContext(): Context {
        return this
    }
    ////////////////////end of presenter methods

    /**
     * shows the detail activity for the movie
     * @param movies - list of movies in the adapter
     * @param position - position in adapter being clicked.
     */
    private fun showMovieDetailActivity(movies : ArrayList<Movie>, position : Int) {
        val intent = Intent(this@MainActivity, MovieDetailActivity::class.java)
        val movie = movies[position]
        intent.putExtra(Constants.KEY_MOVIE_ID, movie.id)
        startActivity(intent)
    }

}
