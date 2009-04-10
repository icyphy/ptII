package ptolemy.verification.kernel.maude;

public class RTMOpTerm extends RTMTerm {

    // args1 frag1 arg2 frag2 ...
    private String[] op;
    private RTMTerm[] terms;

    protected RTMOpTerm(String[] op, RTMTerm[] args) {
        this.op = op;
        this.terms = args;
    }

    @Override
    public String print(int indent, boolean newline) {
        StringBuffer r = new StringBuffer(op[0]);
        int i;
        for (i = 0 ; i < Math.min(op.length-1, terms.length) ; i++)
            r.append(terms[i].print(indent, false) + op[i+1]);
        if (terms.length > op.length - 1)
            for (int j = i ; j < terms.length ; j++)
                r.append(terms[j].print(indent, false));
        else
            for (int j = i ; j < op.length - 1 ; j++)
                r.append(op[j + 1]);
        if (newline)
            return front(indent) + r.toString();
        else
            return r.toString();
    }

}
