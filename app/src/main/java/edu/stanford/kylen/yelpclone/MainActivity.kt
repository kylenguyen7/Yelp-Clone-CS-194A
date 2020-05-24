package edu.stanford.kylen.yelpclone

import android.content.Context
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout
import android.widget.SearchView
import androidx.core.widget.addTextChangedListener
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
    private var currentSearchTerm = "Avocado Toast"
    private var currentLocation = "New York City"

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.options_menu, menu)

        val searchItem = menu.findItem(R.id.search)
        val searchView : SearchView = searchItem.actionView as SearchView

        searchView.queryHint = "Search..."

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                Log.i(TAG, "onQueryTextSubmit: $query")

                if(query == null) {
                    Log.w(TAG, "Did not receive valid query... exiting")
                    return false;
                }

                currentSearchTerm = query
                newSearch(currentSearchTerm, currentLocation);
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

        // Set up recycler view
        rvRestaurants.adapter = adapter
        rvRestaurants.layoutManager = LinearLayoutManager1(this)

        // Initial example search
        newSearch(currentSearchTerm, currentLocation)

        etLocation.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                currentLocation = s.toString();
                newSearch(currentSearchTerm, currentLocation)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        })
    }

    fun newSearch(searchTerm: String, location: String) {
        // Asynchronous call to API
        yelpService.searchRestaurants("Bearer $API_KEY", searchTerm, location).enqueue(object : Callback<YelpSearchResult> {
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
