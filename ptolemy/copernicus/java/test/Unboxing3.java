import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;

public class Unboxing3
{
    static IntToken intToken = new IntToken(1);
    static DoubleToken doubleToken = new DoubleToken(2.0);
    static Token token;
    
    public void foo() {
        boolean flag = true;
	if (flag)
	    token = intToken;
	else
	    token = doubleToken;
	System.out.println("token = " + token.toString());
    }
}
