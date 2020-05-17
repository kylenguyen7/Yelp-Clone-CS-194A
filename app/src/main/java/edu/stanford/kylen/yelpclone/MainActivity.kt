package edu.stanford.kylen.yelpclone

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout
import android.widget.SearchView
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import androidx.recyclerview.widget.LinearLayoutManager as LinearLayoutManager1

private const val TAG = "MainActivity"
private const val BASE_URL = "https://api.yelp.com/v3/"
private const val API_KEY = "2mn9fPSfEqsFLV644PIdcHCBIqSv5qvcZMIbsIYoLDs-cTL_oF28QkGMZvtjJp7yy6wYrjrgZjWuiJpsITXbA9IGa1hO2646byThH7yHYxEzZpAFcQF9685L4DDBXnYx"

class MainActivity : AppCompatActivity() {
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    private val yelpService = retrofit.create(YelpService::class.java)
    private var restaurants = mutableListOf<YelpRestaurant>()
    private var adapter = RestaurantsAdapter(this, restaurants)

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.options_menu, menu)

        val searchItem = menu.findItem(R.id.search)
        val searchView : SearchView = searchItem.actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                Log.i(TAG, "onQueryTextSubmit: $query")

                if(query == null) {
                    Log.w(TAG, "Did not receive valid query... exiting")
                    return false;
                }

                newSearch(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                Log.i(TAG, "onQueryTextChange")
                return true
            }

        })

        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rvRestaurants.adapter = adapter
        rvRestaurants.layoutManager = LinearLayoutManager1(this)

        newSearch("Avocado Toast")
    }

    fun newSearch(searchTerm: String) {
        // Asynchronous call to API
        yelpService.searchRestaurants("Bearer $API_KEY", searchTerm, "New York").enqueue(object : Callback<YelpSearchResult> {
            override fun onResponse(call: Call<YelpSearchResult>, response: Response<YelpSearchResult>) {
                Log.i(TAG, "onResponse $response")
                val body = response.body()

                if(body == null) {
                    Log.w(TAG, "Did not receive valid response body from Yelp API... exiting")
                    return
                }
                restaurants.clear()
                restaurants.addAll(body.restaurants)
                adapter.notifyDataSetChanged()
            }

            override fun onFailure(call: Call<YelpSearchResult>, t: Throwable) {
                Log.i(TAG, "onFailure $t")
            }
        })
    }
}
