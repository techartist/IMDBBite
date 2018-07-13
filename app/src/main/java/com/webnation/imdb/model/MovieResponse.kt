package com.webnation.imdb.model

import com.google.gson.annotations.SerializedName

data class MovieResponse(
        @SerializedName("results") var results : ArrayList<Movie>,
        @SerializedName("page") var page : Int,
        @SerializedName("total_pages") var total_pages : Int,
        @SerializedName("total_results") var total_results : Int) {


}