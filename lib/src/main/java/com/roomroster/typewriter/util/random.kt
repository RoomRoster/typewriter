package com.roomroster.typewriter.util

fun randomPlusOrMinusBetween(value: Int = 100) =
    (Math.ceil(Math.random() * (value * 2 + 1)) - (value + 1)) -
        Math.floor(Math.random() * (value * 2 + 1))