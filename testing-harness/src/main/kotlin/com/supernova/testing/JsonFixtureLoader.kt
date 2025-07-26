package com.supernova.testing

open class JsonFixtureLoader {
    protected fun loadJsonFixture(name: String): String {
        val stream = requireNotNull(javaClass.classLoader?.getResource("fixtures/$name")) {
            "Fixture $name not found"
        }
        return stream.readText()
    }
}
