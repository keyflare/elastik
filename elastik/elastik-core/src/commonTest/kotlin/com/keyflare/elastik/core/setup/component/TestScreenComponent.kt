package com.keyflare.elastik.core.setup.component

import com.keyflare.elastik.core.routing.lifecycle.LifecycleEvent
import com.keyflare.elastik.core.routing.router.ComponentContext
import com.keyflare.elastik.core.setup.component.TestScreenComponentEvent.BackHasBeenHandled
import com.keyflare.elastik.core.setup.component.TestScreenComponentEvent.BackHasNotBeenHandled
import com.keyflare.elastik.core.setup.component.TestScreenComponentEvent.LifecycleEventReceived

@Suppress("MemberVisibilityCanBePrivate")
class TestScreenComponent(
    val context: ComponentContext,
    val testReporter: TestScreenComponentsReporter,
) {
    val destinationId: String get() = context.destinationId
    val entryId: Int get() = context.entryId

    private var backEventsToHandleNumber = 0

    init {
        setupLifecycleHandling()
        setupBackHandling()
    }

    fun enableHandlingBackEvents(n: Int) {
        backEventsToHandleNumber = n
    }

    private fun setupBackHandling() {
        context.backHandler.handleBack {
            if (backEventsToHandleNumber == 0) {
                testReporter.report(
                    event = BackHasNotBeenHandled(
                        entryId = entryId,
                        destinationId = destinationId,
                    )
                )
                false
            } else {
                testReporter.report(
                    event = BackHasBeenHandled(
                        entryId = entryId,
                        destinationId = destinationId,
                    )
                )
                backEventsToHandleNumber--
                true
            }
        }
    }

    private fun setupLifecycleHandling() {
        context.lifecycle.subscribe(
            onCreate = {
                testReporter.report(
                    event = LifecycleEventReceived(
                        entryId = entryId,
                        destinationId = destinationId,
                        lifecycleEvent = LifecycleEvent.CREATED
                    )
                )
            },
            onStart = {
                testReporter.report(
                    event = LifecycleEventReceived(
                        entryId = entryId,
                        destinationId = destinationId,
                        lifecycleEvent = LifecycleEvent.STARTED
                    )
                )
            },
            onResume = {
                testReporter.report(
                    event = LifecycleEventReceived(
                        entryId = entryId,
                        destinationId = destinationId,
                        lifecycleEvent = LifecycleEvent.RESUMED
                    )
                )
            },
            onPause = {
                testReporter.report(
                    event = LifecycleEventReceived(
                        entryId = entryId,
                        destinationId = destinationId,
                        lifecycleEvent = LifecycleEvent.PAUSED
                    )
                )
            },
            onStop = {
                testReporter.report(
                    event = LifecycleEventReceived(
                        entryId = entryId,
                        destinationId = destinationId,
                        lifecycleEvent = LifecycleEvent.STOPPED
                    )
                )
            },
            onDestroy = {
                testReporter.report(
                    event = LifecycleEventReceived(
                        entryId = entryId,
                        destinationId = destinationId,
                        lifecycleEvent = LifecycleEvent.DESTROYED
                    )
                )
            },
            onAny = {
                val event = LifecycleEventReceived(
                    entryId = entryId,
                    destinationId = destinationId,
                    lifecycleEvent = it
                )
                check(testReporter.allEvents.lastOrNull() == event) {
                    "Expected last event to be $event, as it has been already set " +
                            "by one of onCreate, onStart, etc callbacks"
                }
            },
        )
    }

    override fun toString(): String {
        return "TestScreenComponent#${context.destinationId}#${context.entryId}"
    }
}
