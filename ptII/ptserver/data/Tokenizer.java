package ptserver.data;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;

public class Tokenizer {
    private DataInputStream inputStream;

    public Tokenizer() {
    }

    public void setPayload(byte[] payload) {
        inputStream = new DataInputStream(new ByteArrayInputStream(payload));
    }

    public Token getNextToken() throws IOException, IllegalActionException {
        if (inputStream.available() > 0)
            return TokenParser.getInstance().convertToToken(inputStream);
        return null;
    }

}
