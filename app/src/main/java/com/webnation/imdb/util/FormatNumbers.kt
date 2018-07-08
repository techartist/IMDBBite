package com.webnation.imdb.util

/**
 * A class to prettify the numbers returned from the API
 */
object FormatNumbers {
    private const val THOU = 1000L
    private const val MILL = 1000000L
    private const val BILL = 1000000000L
    private const val TRIL = 1000000000000L
    private const val QUAD = 1000000000000000L
    private const val QUIN = 1000000000000000000L

    /**
     * determines the value and then formats
     * @param value to prettify
     * @param defaultTextIfZero default text to display if vakue is zero
     */
    fun formatValue(value: Long, defaultTextIfZero : String): String {
        if (value == 0L) return defaultTextIfZero
        if (value < THOU) return java.lang.Long.toString(value)
        if (value < MILL) return makeDecimal(value, THOU, "K")
        if (value < BILL) return makeDecimal(value, MILL, "M")
        if (value < TRIL) return makeDecimal(value, BILL, "B")
        if (value < QUAD) return makeDecimal(value, TRIL, "T")
        return if (value < QUIN) makeDecimal(value, QUAD, "Q") else makeDecimal(value, QUIN, "u")
    }

    /**
     * performs the calculations and then sends out the formatted value
     * @param value - value to be prettified
     * @param div - value to divide by
     * @param sfx - string to append
     */
    private  fun makeDecimal(value: Long, div: Long, sfx: String): String {
        var valueToBeFormatted = value
        valueToBeFormatted = valueToBeFormatted / (div / 10)
        val whole = valueToBeFormatted / 10
        val tenths = valueToBeFormatted % 10
        return if (tenths == 0L || whole >= 10) String.format("$%d%s", whole, sfx) else String.format("$%d.%d%s", whole, tenths, sfx)
    }
    
}