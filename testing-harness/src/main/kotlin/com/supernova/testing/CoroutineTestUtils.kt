package com.supernova.testing

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Helpers for working with coroutines in unit tests.
 */
object CoroutineTestUtils {
    /** Create a [StandardTestDispatcher] for tests. */
    fun testDispatcher(): TestDispatcher = StandardTestDispatcher()

    /** Create a [TestScope] with the provided dispatcher. */
    fun testScope(dispatcher: TestDispatcher = testDispatcher()): TestScope = TestScope(dispatcher)

    /** Run a suspending [block] in a new [TestScope]. */
    fun runTest(
        dispatcher: TestDispatcher = testDispatcher(),
        timeout: Duration = 10.seconds,
        block: suspend TestScope.() -> Unit
    ) {
        val scope = testScope(dispatcher)
        scope.runTest(timeout) { block() }
    }

    /** Advance the [scope] until all tasks are idle. */
    fun advanceUntilIdle(scope: TestScope) {
        scope.testScheduler.advanceUntilIdle()
    }

    /** Advance virtual time by [millis] milliseconds. */
    fun advanceTimeBy(scope: TestScope, millis: Long) {
        scope.testScheduler.advanceTimeBy(millis)
    }

    /** Create a [TestScope] resembling a ViewModel scope. */
    fun viewModelScope(dispatcher: TestDispatcher = testDispatcher()): TestScope =
        TestScope(SupervisorJob() + dispatcher)

    /** Create a [TestScope] resembling a Repository scope. */
    fun repositoryScope(dispatcher: TestDispatcher = testDispatcher()): TestScope =
        TestScope(SupervisorJob() + dispatcher)

    /** Simple dispatcher provider for injection. */
    interface DispatcherProvider {
        val io: CoroutineDispatcher
        val default: CoroutineDispatcher
        val main: CoroutineDispatcher
    }

    /** Provide a [DispatcherProvider] that returns the given [dispatcher] for all dispatchers. */
    fun dispatcherProvider(dispatcher: TestDispatcher = testDispatcher()): DispatcherProvider =
        object : DispatcherProvider {
            override val io: CoroutineDispatcher = dispatcher
            override val default: CoroutineDispatcher = dispatcher
            override val main: CoroutineDispatcher = dispatcher
        }
}
