package ptolemy.copernicus.java.test;

import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.util.IllegalActionException;


public class Unboxing11 {
    private int _numberOfInputs;
    private int _numberOfLags;
    private int _lengthOfOutput;
    private boolean _symmetricOutput;
    private Token[] _outputs;
    public IntToken numberOfInputs_CGToken;
    public IntToken numberOfLags_CGToken;
    public BooleanToken biased_CGToken;
    public BooleanToken symmetricOutput_CGToken;
    public Token[] tokens;
    public Token token;
    public Token token_4_;
    public Token[] tokens_6_;

    public void fire() throws IllegalActionException {
        int i = _numberOfInputs;
        int i_0_;
        tokens = new Token[i];

        for (int i_1_ = 0; i_1_ < i; i_1_++) {
            DoubleToken doubletoken = new DoubleToken(1.0);
            tokens[i_1_] = doubletoken;
        }

        if (_symmetricOutput) {
            i = 0;
        } else {
            i = 1;
        }

        int i_2_ = i;
        token = tokens[0];

        for (int i_3_ = _numberOfLags; i_3_ >= 0; i_3_--) {
            token_4_ = tokens[0].zero();
            i = 0;

            int i_5_;

            for (;;) {
                i_5_ = _numberOfInputs - i_3_;

                if (i >= i_5_) {
                    break;
                }

                token_4_ = token_4_.add(tokens[i].multiply(tokens[i + i_3_]));
                i++;
            }

            tokens_6_ = _outputs;
            i_0_ = (i_3_ + _numberOfLags) - i_2_;

            IntToken inttoken = new IntToken(i_5_);
            tokens_6_[i_0_] = token_4_.divide(inttoken);
        }

        int i_7_ = _numberOfLags;

        for (i_0_ = i_7_ - 1 - i_2_; i_0_ >= 0; i_0_--) {
            Token[] tokens_8_ = _outputs;
            tokens_8_[i_0_] = tokens_8_[(2 * (i_7_ - i_2_)) - i_0_];
        }

        ArrayToken arraytoken = new ArrayToken(_outputs);
        System.out.println("token = " + arraytoken.toString());
    }

    public Unboxing11() throws IllegalActionException {
        IntToken inttoken = new IntToken(10);
        BaseType.IntType inttype = BaseType.INT;
        Token token = inttype.convert(inttoken);
        IntToken inttoken_11_ = (IntToken) token;
        numberOfInputs_CGToken = inttoken_11_;
        _numberOfInputs = 10;
        _numberOfLags = 3;
        _symmetricOutput = true;

        if (_numberOfInputs <= 0) {
            throw new RuntimeException("Invalid numberOfInputs: "
                + _numberOfInputs);
        }

        int i = _numberOfLags;

        if (i <= 0) {
            throw new RuntimeException("Invalid numberOfLags: " + _numberOfLags);
        }

        if (_symmetricOutput) {
            _lengthOfOutput = (2 * i) + 1;
        } else {
            _lengthOfOutput = 2 * i;
        }

        Token[] tokens = _outputs;

        if ((tokens == null) || (_lengthOfOutput != tokens.length)) {
            _outputs = new Token[_lengthOfOutput];
        }

        inttoken = new IntToken(3);
        inttype = BaseType.INT;

        Token token_12_ = inttype.convert(inttoken);
        inttoken_11_ = (IntToken) token_12_;
        numberOfLags_CGToken = inttoken_11_;
        _numberOfInputs = 10;
        _numberOfLags = 3;
        _symmetricOutput = true;

        if (_numberOfInputs <= 0) {
            throw new RuntimeException("Invalid numberOfInputs: "
                + _numberOfInputs);
        }

        i = _numberOfLags;

        if (i <= 0) {
            throw new RuntimeException("Invalid numberOfLags: " + _numberOfLags);
        }

        if (_symmetricOutput) {
            _lengthOfOutput = (2 * i) + 1;
        } else {
            _lengthOfOutput = 2 * i;
        }

        tokens = _outputs;

        if ((tokens == null) || (_lengthOfOutput != tokens.length)) {
            _outputs = new Token[_lengthOfOutput];
        }

        BooleanToken booleantoken = new BooleanToken(false);
        BaseType.BooleanType booleantype = BaseType.BOOLEAN;
        token_12_ = booleantype.convert(booleantoken);

        BooleanToken booleantoken_13_ = (BooleanToken) token_12_;
        biased_CGToken = booleantoken_13_;
        booleantoken = new BooleanToken(true);
        booleantype = BaseType.BOOLEAN;
        token_12_ = booleantype.convert(booleantoken);
        booleantoken_13_ = (BooleanToken) token_12_;
        symmetricOutput_CGToken = booleantoken_13_;
        _numberOfInputs = 10;
        _numberOfLags = 3;
        _symmetricOutput = true;

        if (_numberOfInputs <= 0) {
            throw new RuntimeException("Invalid numberOfInputs: "
                + _numberOfInputs);
        }

        i = _numberOfLags;

        if (i <= 0) {
            throw new RuntimeException("Invalid numberOfLags: " + _numberOfLags);
        }

        if (_symmetricOutput) {
            _lengthOfOutput = (2 * i) + 1;
        } else {
            _lengthOfOutput = 2 * i;
        }

        tokens = _outputs;

        if ((tokens == null) || (_lengthOfOutput != tokens.length)) {
            _outputs = new Token[_lengthOfOutput];
        }
    }

    public static void main(String[] args) throws IllegalActionException {
        Unboxing11 object = new Unboxing11();
        object.fire();
    }
}
