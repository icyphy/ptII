/*
 * Created on 2005-3-23
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package ptolemy.codegen.c.domains.sdf.lib;

import java.util.List;

import ptolemy.actor.IOPort;
import ptolemy.codegen.kernel.CodeGeneratorHelper;
import ptolemy.data.ArrayToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Variable;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.IllegalActionException;

/**
 * @author zhouye
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class SampleDelay extends CodeGeneratorHelper {

    /** FIXME
     * @param actor
     */
    public SampleDelay(ptolemy.domains.sdf.lib.SampleDelay actor) {
        super(actor);
    }

    ////////////////////////////////////////////////////////////////////
    ////                     public methods                         ////

    public void generateFireCode(StringBuffer stream)
            throws IllegalActionException {
        stream.append(processCode("$ref(output) = $ref(input);\n"));
    }
    
    
    /**  
     */
    public String generateInitializeCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        ptolemy.domains.sdf.lib.SampleDelay actor =
            (ptolemy.domains.sdf.lib.SampleDelay) getComponent();
        List relations = actor.linkedRelationList();
        Relation relation = (Relation) relations.get(0);
        Variable bufferSizeToken
                = (Variable) relation.getAttribute("bufferSize");
        int bufferSize = 1;
        if (bufferSizeToken != null) {
            bufferSize = ((IntToken) bufferSizeToken.getToken()).intValue();
        }
        Token[] initialOutputs = 
                ((ArrayToken)actor.initialOutputs.getToken()).arrayValue();
        List sinkChannels = getSinkChannels(actor.output, 0);
        for (int i = 0; i < initialOutputs.length; i ++) {
            for (int j = 0; j < sinkChannels.size(); j ++) {
                Channel channel = (Channel) sinkChannels.get(j);
                IOPort port = (IOPort) channel.port;
                code.append(port.getFullName().replace('.', '_'));
                if (port.isMultiport()) {
                    code.append("[" + channel.channelNumber + "]");
                }
                if (bufferSize > 1) {
                    code.append("[" + i + "]");
                }
                code.append(" = ");
            }
            code.append(initialOutputs[i].toString() + ";\n");
        }
        return code.toString();
    }
}
