package com.earthrevealed.immaru.assets

import kotlin.test.Test
import kotlin.test.assertEquals

class DatesTest {
    @Test
    fun `test selectableYear, Month, Day hierarchy`() {
        val test = selectableYear {
            caption = "2020"
            month {
                caption = "03"
                day {
                    caption = "03"
                    numberOfItems = 1
                }
                day {
                    caption = "05"
                    numberOfItems = 2
                }
                day {
                    caption = "27"
                    numberOfItems = 3
                }
            }
            month {
                caption = "05"
                day {
                    caption = "01"
                    numberOfItems = 4
                }
                day {
                    caption = "31"
                    numberOfItems = 5
                }
            }
        }

        assertEquals("2020", test.caption)
        assertEquals(15, test.numberOfItems)
        assertEquals(2, test.selectableMonths.size)

        assertEquals("03", test.selectableMonths[0].caption)
        assertEquals(6, test.selectableMonths[0].numberOfItems)
        assertEquals(3, test.selectableMonths[0].selectableDays.size)
        assertEquals("27", test.selectableMonths[0].selectableDays[2].caption)
        assertEquals(3, test.selectableMonths[0].selectableDays[2].numberOfItems)

        assertEquals("05", test.selectableMonths[1].caption)
        assertEquals(9, test.selectableMonths[1].numberOfItems)
        assertEquals(2, test.selectableMonths[1].selectableDays.size)
        assertEquals("31", test.selectableMonths[1].selectableDays[1].caption)
        assertEquals(5, test.selectableMonths[1].selectableDays[1].numberOfItems)
    }
}