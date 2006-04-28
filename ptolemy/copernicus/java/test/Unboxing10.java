package ptolemy.copernicus.java.test;

import ptolemy.data.ArrayToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.util.IllegalActionException;

public class Unboxing10 {
    ArrayToken arraytoken;

    ArrayToken arraytoken2;

    public final ArrayToken initialOutputs_CGToken;

    public static void main(String[] strings) throws IllegalActionException {
        new Unboxing10().initialize();
    }

    public Unboxing10() throws IllegalActionException {
        IntToken[] inttokens = new IntToken[1];
        inttokens[0] = new IntToken(0);
        ((Unboxing10) this).arraytoken = new ArrayToken(inttokens);

        ArrayType arraytype = new ArrayType(BaseType.INT);
        ptolemy.data.Token[] tokens = ((Unboxing10) this).arraytoken
                .arrayValue();
        IntToken[] inttokens_0_ = new IntToken[tokens.length];

        for (int i = 0; i < tokens.length; i++) {
            ((ptolemy.data.Token[]) inttokens_0_)[i] = tokens[i];
        }

        ((Unboxing10) this).arraytoken2 = new ArrayToken(inttokens_0_);
        initialOutputs_CGToken = ((Unboxing10) this).arraytoken2;
        System.out.println("token = " + initialOutputs_CGToken.toString());
    }

    public void initialize() throws IllegalActionException {
        ptolemy.data.Token[] tokens = initialOutputs_CGToken.arrayValue();
        int i = initialOutputs_CGToken.length();

        for (int i_1_ = 0; i_1_ < i; i_1_++) {
            ptolemy.data.Token token = tokens[i_1_];
            DoubleToken doubletoken;

            if (token instanceof DoubleToken) {
                doubletoken = (DoubleToken) token;
            } else {
                if (!(token instanceof IntToken)) {
                    throw new IllegalActionException("Token Exception");
                }

                doubletoken = new DoubleToken(((IntToken) token).doubleValue());
            }

            System.out.println("token = " + doubletoken.toString());
        }
    }
}
