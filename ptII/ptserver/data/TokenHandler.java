package ptserver.data;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import ptolemy.data.Token;

/*
 * Format:
 *
 * 1 byte - length of string (class name)
 * n bytes - class name
 * 1 byte - length of value (class name)
 * y bytes - value
 */
public interface TokenHandler<T extends Token> {
    public void convertToBytes(T token, ByteArrayOutputStream outputStream)
            throws IOException;

    public T convertToToken(byte[] stream, int startPos, int length);

    public byte getPosition();
}
