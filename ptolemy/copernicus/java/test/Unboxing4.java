import ptolemy.data.ArrayToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.util.IllegalActionException;

public class Unboxing4
{
    static IntToken intToken = new IntToken(1);
    static Token[] tokens;
    static Token token;
    
    public static void main(String args[]) throws IllegalActionException {
	tokens = new Token[1];
	tokens[0] = intToken;
	ArrayToken arraytoken = new ArrayToken(tokens);
	if (intToken.getType().equals(BaseType.INT))
	    token = intToken;
	else
	    token = arraytoken;
	System.out.println("token = " + token.toString());
    }
}
