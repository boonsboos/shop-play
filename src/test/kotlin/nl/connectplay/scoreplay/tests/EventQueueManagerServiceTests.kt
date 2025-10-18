package nl.connectplay.scoreplay.tests

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import nl.connectplay.scoreplay.abstraction.services.EventQueueManagerService
import nl.connectplay.scoreplay.events.EventQueueManagerServiceImpl
import nl.connectplay.scoreplay.models.events.FriendRequestEvent
import org.junit.jupiter.api.assertNull
import kotlin.test.Test

class EventQueueManagerServiceTests {

    @Test
    fun `added queues can be removed concurrently and block sending`() = runBlocking {
        val queueManager: EventQueueManagerService = EventQueueManagerServiceImpl()

        repeat(5) { iteration ->
            launch {
                // provision a new queue
                queueManager.provisionQueue(iteration)

                // wait for queue to be deleted from another coroutine
                delay(100)

                val result = queueManager.enqueueEvent(iteration, FriendRequestEvent(1))

                // getting a queue that doesn't exist should return null.
                assertNull(result, "The queue should have been removed.")
            }
            launch {
                println("Removing queue $iteration")
                queueManager.removeQueue(iteration)
            }
        }
    }
}