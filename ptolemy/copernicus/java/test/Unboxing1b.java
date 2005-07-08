package ptolemy.copernicus.java.test;

import ptolemy.data.ArrayToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;

public class Unboxing1b {
    static IntToken intToken = new IntToken(1);

    static Token[] tokens;

    public static void main(String[] strings) throws IllegalActionException {
        tokens = new Token[1];
        tokens[0] = intToken;

        ArrayToken arraytoken = new ArrayToken(tokens);
        System.out.println("token = " + arraytoken.toString());
    }
}
