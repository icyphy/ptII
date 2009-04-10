package ptolemy.verification.kernel.maude;

import java.util.HashMap;
import java.util.Iterator;

import ptolemy.kernel.util.IllegalActionException;

public class RTMObject extends RTMTerm {

    private String name;
    private String objClass;
    private HashMap<String,RTMTerm> attribute;

    public RTMObject(String name, String classname) {
        super();
        this.name = name;
        this.objClass = classname;
        this.attribute = new HashMap<String,RTMTerm>();
    }

    public void addAttr(String name, RTMTerm attr) {
        attribute.put(name, attr);
    }
    public void addStrAttr(String name, String attr) {
        addAttr(name, new RTMFragment(attr));
    }

    public void addExpAttr(String name, String exp, boolean isTime) throws IllegalActionException {
        addAttr(name, new RTMPtExp(exp, isTime));
    }

    public void setClass(String classname) {
            this.objClass = classname;
    }

    @Override
    public String print(int indent, boolean newline) {
        StringBuffer ret = new StringBuffer("");
        if (newline)
            ret.append(front(indent));
        ret.append("< " + transId(name) + " : " + objClass + " | ");
        for (Iterator<String> ki = attribute.keySet().iterator() ; ki.hasNext() ; ) {
            String k = ki.next();
            ret.append("\n" + front(indent + indentWidth) + k + " : ");
            ret.append(attribute.get(k).print(indent + indentWidth, false));
            if (ki.hasNext()) ret.append(", ");
        }
        ret.append(" >");
        return ret.toString();
    }

}
