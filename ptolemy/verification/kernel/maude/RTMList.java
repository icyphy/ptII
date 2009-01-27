package ptolemy.verification.kernel.maude;

import java.util.Iterator;
import java.util.LinkedList;

import ptolemy.kernel.util.IllegalActionException;

public class RTMList extends RTMTerm {
    
    private String saperator;
    private String empty;
    private LinkedList<RTMTerm> items;
    
    public RTMList(String saperator, String emptyrepr) {
        super();
        if (saperator.trim() == "")
            this.saperator = " ";
        else
            this.saperator = " " + saperator.trim() + " ";
        this.empty = emptyrepr;
        this.items = new LinkedList<RTMTerm>();
    }
    
    public void add(RTMTerm t) {
        items.add(t);
    }
    
    public void addStr(String s) {
        add(new RTMFragment(s));
    }
    
    public void addExp(String e, boolean isTime)  throws IllegalActionException {
        add(new RTMPtExp(e, isTime));
    }
    
    public boolean isEmpty() {
    	return items.isEmpty();
    }

    @Override
    public String print(int indent, boolean newline) {
        StringBuffer rs = new StringBuffer("");
        if (items.size() > 0) {
            if (newline)
                rs.append(front(indent));
            rs.append("(");
            for(Iterator<RTMTerm> ti = items.iterator(); ti.hasNext(); ) {
                rs.append("\n" + ti.next().print(indent + indentWidth, true));
                if (ti.hasNext()) rs.append(saperator);
            }
            rs.append(")");
        }
        else
            rs.append(empty);
        return rs.toString();
    }
}
