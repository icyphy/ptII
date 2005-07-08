package ptolemy.copernicus.java.test;

import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;

public class Unboxing2 {
    static IntToken intToken = new IntToken(1);

    static DoubleToken doubleToken = new DoubleToken(2.0);

    static Token token;

    public static void main(String[] strings) {
        if (intToken.getType().equals(BaseType.INT)) {
            token = intToken;
        } else {
            token = doubleToken;
        }

        System.out.println("token = " + token.toString());
    }
}
