package nl.connectplay.scoreplay.utilities

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import nl.connectplay.scoreplay.models.events.BaseEvent

/**
 * Generic JSON serializer voor BaseEvent.
 * Werkt voorlopig alleen als je zelf polymorphic module toevoegt.
 */
class BaseEventJsonSerializer(private val json: Json) : KSerializer<BaseEvent> {

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("BaseEventJson", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: BaseEvent) {
        // Polymorphic serialization via Json.encodeToString
        val jsonString = json.encodeToString(BaseEvent.serializer(), value)
        encoder.encodeString(jsonString)
    }

    override fun deserialize(decoder: Decoder): BaseEvent {
        val jsonString = decoder.decodeString()
        return try {
            json.decodeFromString(BaseEvent.serializer(), jsonString)
        } catch (e: Exception) {
            throw SerializationException("Could not deserialize BaseEvent: ${e.message}")
        }
    }
}
