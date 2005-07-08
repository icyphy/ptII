package ptolemy.copernicus.java.test;

import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;

public class Unboxing6 {
    static IntToken intToken = new IntToken(1);

    static Token[] tokens;

    static Token token;

    static Token castToken;

    public static void main(String[] args) throws IllegalActionException {
        tokens = new Token[1];
        tokens[0] = intToken;
        token = tokens[0];
        castToken = (IntToken) token;
        System.out.println("token = " + castToken.toString());
    }
}
