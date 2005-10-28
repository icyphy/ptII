package ptolemy.copernicus.java.test;

import ptolemy.data.BitwiseOperationToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;

public class Unboxing13 {
    IntToken inputToken;

    BitwiseOperationToken bitwiseoperationtoken;

    public void fire() throws IllegalActionException {
        // Don't call super.fire(); here, the parent is Object.
        IntToken inttoken = new IntToken(1);
        inputToken = inttoken;
        inttoken = inputToken;
        bitwiseoperationtoken = inttoken;
        bitwiseoperationtoken = bitwiseoperationtoken.bitwiseNot();

        Token token = (Token) bitwiseoperationtoken;

        if (!(token instanceof IntToken)) {
            throw new RuntimeException("Token Exception");
        }

        System.out.println("token = " + token.toString());
    }

    public static void main(String[] args) throws IllegalActionException {
        Unboxing13 object = new Unboxing13();
        object.fire();
    }
}
