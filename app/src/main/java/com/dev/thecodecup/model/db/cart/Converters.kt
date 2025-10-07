package com.dev.thecodecup.model.db.cart

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromShotLevel(value: ShotLevel): String = value.name
    @TypeConverter
    fun toShotLevel(value: String): ShotLevel = ShotLevel.valueOf(value)

    @TypeConverter
    fun fromCoffeeSize(value: CoffeeSize): String = value.name
    @TypeConverter
    fun toCoffeeSize(value: String): CoffeeSize = CoffeeSize.valueOf(value)

    @TypeConverter
    fun fromIceLevel(value: IceLevel): String = value.name
    @TypeConverter
    fun toIceLevel(value: String): IceLevel = IceLevel.valueOf(value)
}

