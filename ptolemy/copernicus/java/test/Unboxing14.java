package ptolemy.copernicus.java.test;

import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.util.IllegalActionException;

public class Unboxing14 {
    private Token _stateToken;

    private Token[] _resultArray;

    protected int _firingCountLimit;

    protected int _iterationCount = 0;

    public final IntToken firingCountLimit_CGToken;

    public final IntToken init_CGToken;

    public final IntToken step_CGToken;

    public void initialize() throws IllegalActionException {
        _iterationCount = 0;

        BaseType.IntType inttype = BaseType.INT;
        _stateToken = inttype.convert(init_CGToken);
        _resultArray[0] = _stateToken;
    }

    public void fire() throws IllegalActionException {
        super.fire();
        BaseType.DoubleType doubletype = BaseType.DOUBLE;
        Token token = doubletype.convert(_stateToken);
        DoubleToken doubletoken = (DoubleToken) token;
        System.out.println("token = " + doubletoken);
    }

    public Unboxing14() throws IllegalActionException {
        _stateToken = null;
        _resultArray = new Token[1];

        IntToken inttoken = new IntToken(0);
        BaseType.IntType inttype = BaseType.INT;
        Token token = inttype.convert(inttoken);
        IntToken inttoken_1_ = (IntToken) token;
        firingCountLimit_CGToken = inttoken_1_;
        _firingCountLimit = 0;
        inttoken = new IntToken(0);
        inttype = BaseType.INT;

        Token token_2_ = inttype.convert(inttoken);
        inttoken_1_ = (IntToken) token_2_;
        init_CGToken = inttoken_1_;
        inttoken = new IntToken(1);
        inttype = BaseType.INT;
        token_2_ = inttype.convert(inttoken);
        inttoken_1_ = (IntToken) token_2_;
        step_CGToken = inttoken_1_;
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
        Unboxing14 object = new Unboxing14();
        object.initialize();
        object.fire();
        object.postfire();
        object.fire();
        object.postfire();
    }
}
