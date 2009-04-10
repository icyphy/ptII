package ptolemy.verification.kernel.maude;

public class RTMOpTermGenerator {

    // args1 frag1 arg2 frag2 ...
    private String[] op;

    public RTMOpTermGenerator(String ... operator) {
        super();
        this.op = operator;
    }

    public RTMTerm get(RTMTerm ... args) {
        return new RTMOpTerm(op, args);
    }
}
