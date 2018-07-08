package com.webnation.imdb

import com.webnation.imdb.util.FormatNumbers
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertNotNull
import org.junit.Test

class FormatNumberTest {
    @Test
    fun testGetNumberInPrettyFormat() {
        val numbers: LongArray = longArrayOf(0, 1000, 20000, 40000000, 350000000, 500000000000)
        val values = arrayOf("not available","$1K","$20K","$40M","$350M","$500B")

        assertNotNull(numbers)
        assertNotNull(values)
        for (i in 0..numbers.size-1) {
            assertEquals(values[i], FormatNumbers.formatValue(numbers[i],"not available"))

        }


    }
}