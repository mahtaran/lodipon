package nl.utwente.smartspaces.lodipon.data

import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

data class GeodeticLocation(val latitude: Double, val longitude: Double) {
    fun toCartesian(): CartesianLocation {
        val latitude = Math.toRadians(latitude)
        val longitude = Math.toRadians(longitude)

        val x = EARTH_RADIUS * cos(latitude) * cos(longitude)
        val y = EARTH_RADIUS * cos(latitude) * sin(longitude)
        val z = EARTH_RADIUS * sin(latitude)

        return CartesianLocation(x, y, z)
    }
}

data class CartesianLocation(val x: Double, val y: Double, val z: Double) {
    fun toGeodetic(): GeodeticLocation {
        val longitude = Math.toDegrees(atan2(y, x))
        val latitude = Math.toDegrees(asin(z / EARTH_RADIUS))

        return GeodeticLocation(latitude, longitude)
    }

    operator fun plus(other: CartesianLocation): CartesianLocation {
        return CartesianLocation(x + other.x, y + other.y, z + other.z)
    }

    operator fun minus(other: CartesianLocation): CartesianLocation {
        return CartesianLocation(x - other.x, y - other.y, z - other.z)
    }

    operator fun times(scalar: Double): CartesianLocation {
        return CartesianLocation(x * scalar, y * scalar, z * scalar)
    }

    operator fun div(scalar: Double): CartesianLocation {
        return CartesianLocation(x / scalar, y / scalar, z / scalar)
    }

    fun norm(): Double {
        return sqrt(x.pow(2) + y.pow(2) + z.pow(2))
    }

    fun dot(other: CartesianLocation): Double {
        return x * other.x + y * other.y + z * other.z
    }

    fun cross(other: CartesianLocation): CartesianLocation {
        return CartesianLocation(
            y * other.z - z * other.y,
            z * other.x - x * other.z,
            x * other.y - y * other.x
        )
    }
}
