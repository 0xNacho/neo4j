package org.neo4j.helpers.idcompression;

import java.nio.ByteBuffer;

/**
 * @author mh
 * @since 17.03.13
 */
public interface LongEncoder
{
    int encode( ByteBuffer target, long value );

    long decode( ByteBuffer source );
}
