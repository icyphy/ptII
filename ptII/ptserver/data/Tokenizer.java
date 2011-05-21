package ptserver.data;

import java.io.UnsupportedEncodingException;

import ptolemy.data.Token;

public class Tokenizer {
    private int pointer;
    private byte[] payload;

    public Tokenizer() {
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
        pointer = 0;
    }

    public Token getNextToken() {
        if (pointer < payload.length) {
            byte nameLength = payload[pointer++];
            String name = null;
            try {
                name = new String(payload, pointer, nameLength, "UTF-8");
            } catch (UnsupportedEncodingException e1) {
            }
            pointer += nameLength;
            int valueLength = payload[pointer++];
            TokenHandler<?> handler = HandlerParser.getHandler(name);
            Token token = handler.convertToToken(payload, pointer, valueLength);
            pointer += valueLength;
            return token;
        }
        return null;
    }

}
