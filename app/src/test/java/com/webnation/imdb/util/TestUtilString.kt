package com.webnation.imdb.util


import java.io.IOException
import java.nio.charset.Charset

class TestUtilString(nameOfResource: String) {

    var jsonString = ""

    init {
        jsonString = getJsonString(nameOfResource)
    }

    fun getJsonString(nameOfResource: String): String {
        val q = javaClass.classLoader.getResourceAsStream(nameOfResource)
        var contents = ""
        try {
            contents = q.readBytes().toString(Charset.defaultCharset())

        } catch (e: IOException) {
            e.printStackTrace()

        }

        return contents
    }
}