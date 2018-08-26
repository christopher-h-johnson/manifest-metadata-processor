package de.ubleipzig.metadata.indexer;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.UUID;

/**
 * UUIDType5.
 *
 * @author christopher-johnson
 */
public final class UUIDv5 {

    public static final UUID NAMESPACE_URL = UUID.fromString("6ba7b811-9dad-11d1-80b4-00c04fd430c8");
    private static final Charset UTF8 = Charset.forName("UTF-8");

    private UUIDv5() {
    }

    /**
     * nameUUIDFromNamespaceAndString.
     *
     * @param namespace namespace
     * @param name name
     * @return {@link UUID}
     */
    public static UUID nameUUIDFromNamespaceAndString(final UUID namespace, final String name) {
        return nameUUIDFromNamespaceAndBytes(namespace, Objects.requireNonNull(name, "name == null").getBytes(UTF8));
    }

    /**
     * @param algorithm String
     * @return MessageDigest
     */
    public static MessageDigest getDigest(final String algorithm) {
        try {
            return MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException nsae) {
            throw new InternalError(algorithm + " not supported");
        }
    }

    /**
     * nameUUIDFromNamespaceAndBytes.
     *
     * @param namespace namespace
     * @param name name
     * @return {@link UUID}
     */
    public static UUID nameUUIDFromNamespaceAndBytes(final UUID namespace, final byte[] name) {
        final MessageDigest md = getDigest("SHA-1");
        md.update(toBytes(Objects.requireNonNull(namespace, "namespace is null")));
        md.update(Objects.requireNonNull(name, "name is null"));
        final byte[] sha1Bytes = md.digest();
        sha1Bytes[6] &= 0x0f;  /* clear version        */
        sha1Bytes[6] |= 0x50;  /* set to version 5     */
        sha1Bytes[8] &= 0x3f;  /* clear variant        */
        sha1Bytes[8] |= 0x80;  /* set to IETF variant  */
        return fromBytes(sha1Bytes);
    }

    /**
     * fromBytes.
     *
     * @param data data
     * @return {@link UUID}
     */
    private static UUID fromBytes(final byte[] data) {
        // Based on the private UUID(bytes[]) constructor
        long msb = 0;
        long lsb = 0;
        for (int i = 0; i < 8; i++) {
            msb = (msb << 8) | (data[i] & 0xff);
        }
        for (int i = 8; i < 16; i++) {
            lsb = (lsb << 8) | (data[i] & 0xff);
        }
        return new UUID(msb, lsb);
    }

    /**
     * toBytes.
     *
     * @param uuid {@link UUID}
     * @return byte[]
     */
    private static byte[] toBytes(final UUID uuid) {
        // inverted logic of fromBytes()
        final byte[] out = new byte[16];
        final long msb = uuid.getMostSignificantBits();
        final long lsb = uuid.getLeastSignificantBits();
        for (int i = 0; i < 8; i++) {
            out[i] = (byte) ((msb >> ((7 - i) * 8)) & 0xff);
        }
        for (int i = 8; i < 16; i++) {
            out[i] = (byte) ((lsb >> ((15 - i) * 8)) & 0xff);
        }
        return out;
    }
}
