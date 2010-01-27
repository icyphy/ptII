package ptolemy.apps.ptalon.model;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;

import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.ptalon.PtalonAST;
import ptolemy.actor.ptalon.PtalonLexer;
import ptolemy.actor.ptalon.PtalonRecognizer;
import ptolemy.apps.ptalon.model.controls.PtalonEditorFactory;
import ptolemy.data.StringToken;
import ptolemy.data.expr.ExpertParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.moml.MoMLChangeRequest;

/**
 * @author adam
 * 
 */
public class PtalonModel extends TypedCompositeActor {

    public PtalonModel(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        setClassName("sdc.ptalon.PtalonModel");
        new PtalonEditorFactory(this, "_editorFactory", this);
        _ptalonCode = new ExpertParameter(this, "_ptalonCode");
        _ptalonCode.setTypeEquals(BaseType.STRING);
        _ptalonCode.setPersistent(true);
    }

    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        super.attributeChanged(attribute);
        if (attribute == _ptalonCode) {
            StringToken token = (StringToken) _ptalonCode.getToken();
            if (token == null) {
                return;
            }
            if (_code == null) {
                _code = token.stringValue();
                return;
            }
            String pararemterCode = token.stringValue();
            if (!_code.equals(pararemterCode)) {
                _code = pararemterCode;
            }
        }
    }

    private ExpertParameter _ptalonCode;

    // The code for this Ptalon Model.
    private String _code = null;

    public void setCode(String code) {
        _code = code;
        try {
            _ptalonCode.setToken(new StringToken(_code));
        } catch (Exception e) {
        }
    }

    public String getCode() {
        return _code;
    }

    private File _file = null;

    public void setFile(File fileName) {
        _file = fileName;
    }

    public File getFile() {
        return _file;
    }

    public void updateModel() throws IllegalActionException {
        if (_code != null) {
            clear();
            evaluate();
        }
    }

    private void deepClear() {
        removeAllPorts();
        removeAllRelations();
        removeAllEntities();
        for (Parameter p : parameterList(this)) {
            _removeAttribute(p);
        }
    }

    private void clear() {
        removeAllPorts();
        removeAllRelations();
        removeAllEntities();
        _expressions.clear();
        for (Parameter p : parameterList(this)) {
            _expressions.put(p.getName(), p.getExpression());
            _removeAttribute(p);
        }
    }

    private void evaluate() throws IllegalActionException {
        try {
            // Open the code location. Use urls for WebStart and jar files.
            PtalonLexer lex = null;
            PtalonRecognizer rec = null;
            InputStream inputStream = null;
            inputStream = new ByteArrayInputStream(_code.getBytes());
            lex = new PtalonLexer(inputStream);
            inputStream.close();
            rec = new PtalonRecognizer(lex);
            rec.setASTNodeClass("ptolemy.actor.ptalon.PtalonAST");
            rec.actor_definition();
            PtalonAST ast = (PtalonAST) rec.getAST();
            PtalonModelEvaluator evaluator = new PtalonModelEvaluator();
            int parameterCount = 0;
            while (evaluator.evaluate(this, ast)) {
                if (_expressions.size() == 0) {
                    break;
                }
                List<Parameter> paramList = parameterList(this);
                if (paramList.size() > parameterCount) {
                    parameterCount = parameterList(this).size();
                } else {
                    break;
                }
                HashSet<String> needsRemove = new HashSet<String>();
                for (String name : _expressions.keySet()) {
                    Attribute a = this.getAttribute(name);
                    if (a instanceof Parameter) {
                        Parameter p = (Parameter) a;
                        p.setExpression(_expressions.get(name));
                        needsRemove.add(name);
                    }
                }
                for (String name : needsRemove) {
                    _expressions.remove(name);
                }
            }
            String moml = exportMoML();
            deepClear();
            getContainer().requestChange(
                    new MoMLChangeRequest(this, getContainer(), moml));
        } catch (Exception ex) {
            throw new IllegalActionException(this, ex, ex.getMessage());
        }
    }

    public static List<Parameter> parameterList(NamedObj actor) {
        ArrayList<Parameter> parameters = new ArrayList<Parameter>();
        for (Object attribute : actor.attributeList()) {
            if (attribute instanceof Parameter) {
                Parameter parameter = (Parameter) attribute;
                if (parameter.getVisibility().equals(Settable.FULL)
                        && !parameter.getName().equals("_ptalonCode")) {
                    parameters.add(parameter);
                }
            }
        }
        return parameters;
    }

    private Hashtable<String, String> _expressions = new Hashtable<String, String>();

    public String uniqueName(String prefix) {
        if (prefix == null) {
            prefix = "null";
        }

        String candidate = prefix;

        int uniqueNameIndex = 1;

        while ((getAttribute(candidate) != null)
                || (getPort(candidate) != null)
                || (getEntity(candidate) != null)
                || (getRelation(candidate) != null)) {
            candidate = prefix + ++uniqueNameIndex;
        }

        return candidate;
    }
}
