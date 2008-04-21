/*

@Copyright (c) 2008 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
SUCH DAMAGE.

THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
ENHANCEMENTS, OR MODIFICATIONS.

                        PT_COPYRIGHT_VERSION_2
                        COPYRIGHTENDKEY



 */

package ptolemy.actor.gt;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.Manager;
import ptolemy.actor.lib.Transformer;
import ptolemy.data.ActorToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.expr.Variable;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;

/**

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 7.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class ModelExecutor extends Transformer {

    /**
     * @param container
     * @param name
     * @throws NameDuplicationException
     * @throws IllegalActionException
     */
    public ModelExecutor(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        input.setTypeEquals(ActorToken.TYPE);

        outputParameter = new StringParameter(this, "outputParameter");
        outputType = new Parameter(this, "outputType");
        outputType.setExpression("general");
    }

    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == outputType) {
            output.setTypeEquals(outputType.getType());
        }
    }

    public void fire() throws IllegalActionException {
        Entity entity = ((ActorToken) input.get(0)).getEntity();
        if (!(entity instanceof CompositeActor)) {
            throw new IllegalActionException("Only CompositeActor can be " +
                    "executed.");
        }

        CompositeActor actor = (CompositeActor) entity;
        Manager manager = actor.getManager();
        if (manager == null) {
            manager = new Manager(actor.workspace(), "_manager");
            actor.setManager(manager);
        }
        try {
            manager.execute();
        } catch (KernelException e) {
            throw new IllegalActionException(this, e, "Execution failed.");
        }

        String outputName = outputParameter.getExpression();
        if (!outputName.equals("")) {
            Attribute outputAttribute = actor.getAttribute(outputName);
            if (outputAttribute instanceof Variable) {
                Token token = ((Variable) outputAttribute).getToken();
                if (!outputType.getType().isCompatible(token.getType())) {
                    token = outputType.getType().convert(token);
                }
                output.send(0, token);
            } else {
                throw new IllegalActionException("Unable to obtain output " +
                        "token from parameter with name \"" + outputName +
                        "\".");
            }
        }
    }

    public boolean prefire() throws IllegalActionException {
        return super.prefire() && input.hasToken(0);
    }

    public StringParameter outputParameter;

    public Parameter outputType;
}
