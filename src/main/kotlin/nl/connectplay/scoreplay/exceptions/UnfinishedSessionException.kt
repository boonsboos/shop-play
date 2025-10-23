package nl.connectplay.scoreplay.exceptions

import java.util.*

class UnfinishedSessionException(userId: Int, sessionId: UUID)
    : RuntimeException("User $userId attempted to upload scores to $sessionId, but it has not finished yet")
