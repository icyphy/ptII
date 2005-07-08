package ptolemy.copernicus.java.test;

import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;

public class Unboxing3 {
    static IntToken intToken = new IntToken(1);

    static DoubleToken doubleToken = new DoubleToken(2.0);

    static Token token;

    public static void main(String[] strings) throws IllegalActionException {
        token = intToken.add(doubleToken);
        System.out.println("token = " + token.toString());
    }
}
