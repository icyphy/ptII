package ptolemy.copernicus.java.test;

import ptolemy.data.ArrayToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.util.IllegalActionException;

public class Unboxing9 {
    ArrayToken arraytoken;

    ArrayToken arraytoken2;

    public final ArrayToken initialOutputs_CGToken;

    public void initialize() throws IllegalActionException {
        Token[] tokens = initialOutputs_CGToken.arrayValue();
        int i = initialOutputs_CGToken.length();
        BaseType.DoubleType doubletype = BaseType.DOUBLE;

        for (int i_0_ = 0; i_0_ < i; i_0_++) {
            Token token = tokens[i_0_];
            token = doubletype.convert(token);

            DoubleToken doubletoken = (DoubleToken) token;
            System.out.println("token = " + doubletoken.toString());
        }
    }

    public Unboxing9() throws IllegalActionException {
        IntToken[] inttokens = new IntToken[1];
        IntToken inttoken = new IntToken(0);
        inttokens[0] = inttoken;
        arraytoken = new ArrayToken(inttokens);

        ArrayType arraytype = new ArrayType(BaseType.INT);
        arraytoken2 = (ArrayToken) arraytype.convert(arraytoken);
        initialOutputs_CGToken = arraytoken2;
        System.out.println("token = " + initialOutputs_CGToken.toString());
    }

    public static void main(String[] args) throws IllegalActionException {
        Unboxing9 object = new Unboxing9();
        object.initialize();
    }
}
