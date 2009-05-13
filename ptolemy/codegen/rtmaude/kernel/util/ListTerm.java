package ptolemy.codegen.rtmaude.kernel.util;

import java.util.Iterator;

import ptolemy.kernel.util.IllegalActionException;

public class ListTerm<T> {

    protected String delimiter;
    protected String empty;
    protected Iterator<T> iter;
    
    public ListTerm(String empty, String delimiter, Iterable<T> target) {
        this.iter = target.iterator();
        this.empty = empty;
        this.delimiter = delimiter;
    }
    public String generateCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        while (iter.hasNext()) {
            String v = this.item(iter.next());
            if (v != null) {        // if null, it's screened out
                code.append(v);  
                if (iter.hasNext()) code.append(delimiter);
            }
        }
        if (code.length() > 0)
            return code.toString();
        else
            return empty;
    }
    
    public String item(T v) throws IllegalActionException {
        return v.toString();
    }
}
