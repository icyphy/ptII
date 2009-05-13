package ptolemy.codegen.rtmaude.kernel;

import java.util.HashMap;
import java.util.Map;

import ptolemy.codegen.kernel.CodeStream;
import ptolemy.codegen.rtmaude.kernel.util.ListTerm;
import ptolemy.data.expr.Variable;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.IllegalActionException;

public class Entity extends RTMaudeAdaptor {

    public Entity(ptolemy.kernel.Entity component) {
        super(component);
    }

    /**
     * Generate the fire code for an entity. In the Real-time Maude,
     * any entity of Ptolemy is translated to Object term
     *   < Name : ClassName | attr_1 : attr_value_1, ... , attr_n : attr_value_n >
     */
    protected String _generateFireCode() throws IllegalActionException { 
        final Map<String,String> atts = this._generateAttributeTerms();
        return _generateBlockCode("fireBlock",
                getComponent().getName(),
                _generateBlockCode("className"),
                CodeStream.indent(1,
                    new ListTerm<String>("", "," + _eol, atts.keySet()) {
                        public String item(String v) throws IllegalActionException {
                            return _generateBlockCode("attrBlock", v, 
                                    CodeStream.indent(1, atts.get(v))
                                    );
                        }
                    }.generateCode())
            );
    }
    
    /**
     * Define attribute contents of each actor. In this base class,
     * attributes "store", "status", "ports" and "variables" are defined.
     * Each subclass should extend this method for their own attributes.
     * 
     * @return
     * @throws IllegalActionException
     */
    protected Map<String,String> _generateAttributeTerms() throws IllegalActionException {
        HashMap<String,String> atts = new HashMap<String,String>();
        
        atts.put("store", "emptyMap");
        //generate 'status' attribute
        atts.put("status", _generateBlockCode("statusBlock"));
        
        // generate 'ports' attribute
        atts.put("ports", 
                new ListTerm<Port>("none", "",
                        ((ptolemy.kernel.Entity)getComponent()).portList()) {
                            public String item(Port v) throws IllegalActionException {
                                return ((RTMaudeAdaptor) _getHelper(v)).generateTermCode();
                            }
                        }.generateCode()
            );
        
        // generate 'variables' attribute
        atts.put("variables",
                new ListTerm<Variable>("emptyMap", " ;" + _eol, 
                        getComponent().attributeList(Variable.class)) {
                            public String item(Variable v) throws IllegalActionException {
                                return ((RTMaudeAdaptor) _getHelper(v)).generateTermCode();
                            }
                        }.generateCode()
        );
        return atts;
    }
}
