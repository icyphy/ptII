package ptolemy.copernicus.java.test;

import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.util.IllegalActionException;

public class Unboxing12 {
    private Token _stateToken;

    private Token[] _resultArray;

    protected int _firingCountLimit;

    protected int _iterationCount = 0;

    public IntToken firingCountLimit_CGToken;

    public DoubleToken init_CGToken;

    public IntToken step_CGToken;

    public void initialize() throws IllegalActionException {
        _iterationCount = 0;

        BaseType.DoubleType doubletype = BaseType.DOUBLE;
        _stateToken = doubletype.convert(init_CGToken);
    }

    public void fire() throws IllegalActionException {
        // Don't call super.fire(); here, the parent is Object.
        BaseType.DoubleType doubletype = BaseType.DOUBLE;
        Token token = doubletype.convert(_stateToken);
        System.out.println("token = " + token.toString());
    }

    public Unboxing12() throws IllegalActionException {
        _stateToken = null;
        _resultArray = new Token[1];

        IntToken inttoken = new IntToken(0);
        BaseType.IntType inttype = BaseType.INT;
        Token token = inttype.convert(inttoken);
        IntToken inttoken_0_ = (IntToken) token;
        firingCountLimit_CGToken = inttoken_0_;

        DoubleToken doubletoken = new DoubleToken(0.0);
        BaseType.DoubleType doubletype = BaseType.DOUBLE;
        Token token_1_ = doubletype.convert(doubletoken);
        DoubleToken doubletoken_2_ = (DoubleToken) token_1_;
        init_CGToken = doubletoken_2_;
        inttoken = new IntToken(1);
        inttype = BaseType.INT;
        token_1_ = inttype.convert(inttoken);
        inttoken_0_ = (IntToken) token_1_;
        step_CGToken = inttoken_0_;
    }

    public boolean postfire() throws IllegalActionException {
        _stateToken = _stateToken.add(step_CGToken);

        int i = _firingCountLimit;
        boolean bool;

        do {
            if (i != 0) {
                _iterationCount = _iterationCount + 1;

                if (_iterationCount == i) {
                    bool = false;
                    break;
                }
            }

            bool = true;
        } while (false);

        return bool;
    }

    public static void main(String[] args) throws IllegalActionException {
        Unboxing12 object = new Unboxing12();
        object.initialize();
        object.fire();
        object.postfire();
        object.fire();
        object.postfire();
    }
}
