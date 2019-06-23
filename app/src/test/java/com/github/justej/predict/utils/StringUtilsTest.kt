package com.github.justej.predict.utils

import org.junit.Assert
import org.junit.Test

class StringUtilsTest {

    @Test
    fun normalizeSingleLine() {
        Assert.assertEquals(StringUtils.normalize(" begins with a space"), "begins with a space")
        Assert.assertEquals(StringUtils.normalize("   begins with several spaces"), "begins with several spaces")
        Assert.assertEquals(StringUtils.normalize("Ends with a space "), "Ends with a space")
        Assert.assertEquals(StringUtils.normalize("Ends with several spaces   "), "Ends with several spaces")
    }

    @Test
    fun normalizeMultipleLines() {
        Assert.assertEquals(StringUtils.normalize(" first\nsecond \n  third   "), "first\nsecond\nthird")
    }

    @Test
    fun normalizeKeepsInnerSpacesUntouched() {
        Assert.assertEquals(StringUtils.normalize("Line with multiple   spaces"), "Line with multiple   spaces")
    }

    @Test
    fun normalizeEmptyLinesInMultilineString() {
        Assert.assertEquals(StringUtils.normalize("\nSecond"), "Second")
    }

    @Test
    fun normalizeIdenticalLines() {
        Assert.assertEquals(StringUtils.normalize("Same line\nSame line"), "Same line")
        Assert.assertEquals(StringUtils.normalize("Same line  \nSame line"), "Same line")
        Assert.assertEquals(StringUtils.normalize("One line\nAnother line"), "One line\nAnother line")
    }

}