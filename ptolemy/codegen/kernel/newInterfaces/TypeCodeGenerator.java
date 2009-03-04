package ptolemy.codegen.kernel.newInterfaces;

import ptolemy.codegen.util.PartialResult;

public interface TypeCodeGenerator{

    public PartialResult add(PartialResult PARAM);

    public PartialResult clone();

    public PartialResult equals(PartialResult PARAM);

    public PartialResult isAbstract();

    public PartialResult isInstantiable();

    public PartialResult convert(PartialResult PARAM);

    public PartialResult divide(PartialResult PARAM);

    public PartialResult getTypeHash();

    public PartialResult getTokenClass();

    public PartialResult isCompatible(PartialResult PARAM);

    public PartialResult isConstant();

    public PartialResult isSubstitutionInstance(PartialResult PARAM);

    public PartialResult modulo(PartialResult PARAM);

    public PartialResult multiply(PartialResult PARAM);

    public PartialResult one();

    public PartialResult subtract(PartialResult PARAM);

    public PartialResult zero();

}

