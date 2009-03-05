package ptolemy.codegen.kernel.newInterfaces;

import ptolemy.codegen.util.PartialResult;

public interface TokenCodeGenerator{

    public PartialResult pow(PartialResult PARAM);

    public PartialResult add(PartialResult PARAM);

    public PartialResult getType();

    public PartialResult isNil();

    public PartialResult addReverse(PartialResult PARAM);

    public PartialResult divide(PartialResult PARAM);

    public PartialResult divideReverse(PartialResult PARAM);

    public PartialResult isCloseTo(PartialResult PARAM);

    public PartialResult isCloseTo(PartialResult PARAM, PartialResult PARAM2);

    public PartialResult isEqualTo(PartialResult PARAM);

    public PartialResult modulo(PartialResult PARAM);

    public PartialResult moduloReverse(PartialResult PARAM);

    public PartialResult multiply(PartialResult PARAM);

    public PartialResult multiplyReverse(PartialResult PARAM);

    public PartialResult one();

    public PartialResult subtract(PartialResult PARAM);

    public PartialResult subtractReverse(PartialResult PARAM);

    public PartialResult zero();

}

