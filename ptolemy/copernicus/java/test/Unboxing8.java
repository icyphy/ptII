package ptolemy.copernicus.java.test;

import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;

public class Unboxing8 {
    static IntToken intToken = new IntToken(1);

    static Token[] tokens;

    static Token[] tokens2;

    static Token token;

    static Token castToken;

    public static void main(String[] args) throws IllegalActionException {
        tokens = new Token[1];
        tokens2 = tokens;
        tokens2[0] = intToken;
        token = tokens2[0];
        System.out.println("token = " + token.toString());
    }
}
