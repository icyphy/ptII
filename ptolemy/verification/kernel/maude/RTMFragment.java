package ptolemy.verification.kernel.maude;

public class RTMFragment extends RTMTerm {

    protected String frag;
    
    public RTMFragment(String fragment) {
        super();
        this.frag = fragment;
    }
    
    @Override
    public String print(int indent, boolean newline) {
        if (newline)
            return front(indent) + this.frag;
        else
            return this.frag;
    }
}
