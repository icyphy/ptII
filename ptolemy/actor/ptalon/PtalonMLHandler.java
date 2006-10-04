package ptolemy.actor.ptalon;

import java.io.File;
import java.util.Hashtable;
import java.util.Stack;

import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.StringToken;
import ptolemy.util.StringUtilities;

import com.microstar.xml.HandlerBase;

public class PtalonMLHandler extends HandlerBase {
    
    /**
     * Create a PtalonMLHandler, which will be used to recover the
     * AST and code manager specified in the PtalonML.
     * @param actor The actor to associate with this handler.
     */
    public PtalonMLHandler(PtalonActor actor) {
        super();
        _attributes = new Hashtable<String, String>();
        _actor = actor;
    }
    
    /**
     * Process a PtalonML attribute.
     * @param aname The name of the attribute.
     * @param value The value of the attribute, or null if the attribute
     *        is <code>#IMPLIED</code>.
     * @param isSpecified True if the value was specified, false if it
     *       was defaulted from the DTD.
     * @exception java.lang.Exception If there is any trouble creating
     * the AST or code manager,
     */
    public void attribute(String aname, String value, boolean isSpecified) throws Exception {
        if ((aname != null) && (value != null)) {
            _attributes.put(aname, value);
        }
    }

    /**
     * Process the end of a PtalonML element.
     * @param elname The element type name.
     * @exception java.lang.Exception If there is any trouble creating
     * the AST or code manager.
     */
    public void endElement(String elname) throws Exception {
        
    }
        
    /**
     * Process the start of a PtalonML element.
     * @param elname The element type name.
     * @exception java.lang.Exception If there is any trouble creating
     * the AST or code manager,
     */
    public void startElement(String elname) throws Exception {
        if (elname.equals("ptalon")) {
            if (_attributes.containsKey("file")) {
                String name = _attributes.get("file");
                name = name.replace(".", "/");
                name = name + ".ptln";
                File file = new File(StringUtilities.getProperty("ptolemy.ptII.dir"));
                file = new File(file, name);
                _actor.ptalonCodeLocation.setToken(new StringToken(file.toString()));
            }
        } else if (elname.equals("ptalonParameter")) {
            if (_attributes.containsKey("name") && _attributes.containsKey("value")) {
                PtalonParameter param = (PtalonParameter) _actor.getAttribute(_attributes.get("name"));
                param.setToken(new StringToken(_attributes.get("value")));
            }
        } else if (elname.equals("ptalonExpressionParameter")) {
            if (_attributes.containsKey("name") && _attributes.containsKey("value")) {
                PtalonExpressionParameter param = (PtalonExpressionParameter) _actor.getAttribute(_attributes.get("name"));
                param.setExpression(_attributes.get("value"));
                _actor.attributeChanged(param);
            }
        }
        _attributes.clear();
    }
    
    /**
     * The actor that created this handler.
     */
    PtalonActor _actor;
    
    /**
     * Each element in this hashtable maps a name to a value.
     */
    Hashtable<String, String> _attributes;
    


    

}
