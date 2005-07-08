package ptolemy.copernicus.java.test;

import ptolemy.data.ArrayToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;

public class Unboxing1d {
    static IntToken intToken = new IntToken(1);

    static Token[] tokens;

    static Token[] tokens2;

    static ArrayToken intArrayToken;

    static ArrayToken intArrayArrayToken;

    public static void main(String[] strings) throws IllegalActionException {
        tokens = new Token[1];
        tokens[0] = intToken;
        intArrayToken = new ArrayToken(tokens);
        tokens2 = new Token[1];
        tokens2[0] = intArrayToken;
        intArrayArrayToken = new ArrayToken(tokens2);
        System.out.println("token = " + intArrayArrayToken.toString());
    }
}
