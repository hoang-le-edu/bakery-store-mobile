package com.dev.thecodecup.model.data
import com.dev.thecodecup.R
import com.dev.thecodecup.model.item.RedeemCoffee

val redeemCoffeeList = listOf<RedeemCoffee>(
    RedeemCoffee(
        id = 1,
        name = "Americano",
        imageResId = R.drawable.americano,
        price = 0.0,
        points = 1000
    ),
    RedeemCoffee(
        id = 2,
        name = "Mocha",
        imageResId = R.drawable.mocha,
        price = 0.0,
        points = 1500
    ),
    RedeemCoffee(
        id = 3,
        name = "Cappuccino",
        imageResId = R.drawable.cappuccino,
        price = 0.0,
        points = 2000
    )
)