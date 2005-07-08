package ptolemy.copernicus.java.test;

import ptolemy.data.ArrayToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;

public class Unboxing1c {
    public static void main(String[] strings) throws IllegalActionException {
        IntToken inttoken = new IntToken(1);
        Token[] tokens = new Token[1];
        tokens[0] = inttoken;

        ArrayToken arraytoken = new ArrayToken(tokens);
        Token[] tokens2 = new Token[1];
        tokens2[0] = arraytoken;

        ArrayToken arraytoken2 = new ArrayToken(tokens2);
        System.out.println("token = " + arraytoken2.toString());
    }
}
