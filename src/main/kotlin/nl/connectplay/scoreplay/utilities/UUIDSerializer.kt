package nl.connectplay.scoreplay.utilities

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.util.UUID

/**
 * A custom implementation of a serializer for serializing Java UUIDs.
 *
 * @see <a href=https://www.droidcon.com/2024/04/04/introduction-to-using-kotlin-serialization/">Serialization tutorial by droidcon.com</a>
 */
class UUIDSerializer : KSerializer<UUID> {
    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("UUID", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: UUID) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): UUID {
        decoder.decodeString().let { uuidString -> return UUID.fromString(uuidString) }
    }
}