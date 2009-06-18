/*  An actor that performs property analysis on input model.

 @Copyright (c) 1998-2009 The Regents of the University of California.
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

 PT_COPYRIGHT_VERSION 2
 COPYRIGHTENDKEY
 */
package ptolemy.domains.properties.lib;

import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.Transformer;
import ptolemy.data.ActorToken;
import ptolemy.data.StringToken;
import ptolemy.data.properties.AnalyzerAttribute;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// ModelAnalyzer

/**
This actor performs property analysis on input model. Upon firing, it 
consumes an input ActorToken and outputs the same model with added 
property annotations. 

@author  Man-Kit Leung
@version $Id$
@since Ptolemy II 7.1
@Pt.ProposedRating Yellow (eal)
@Pt.AcceptedRating Red (cxh)
*/
public class ModelAnalyzer extends Transformer {

    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public ModelAnalyzer(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        errorMessage = new TypedIOPort(this, "errorMessage", false, true);
        errorMessage.setTypeEquals(BaseType.STRING);

        input.setTypeEquals(ActorToken.TYPE);
        output.setTypeEquals(ActorToken.TYPE);

        _analyzerWrapper = new AnalyzerAttribute(this, "_analyzerWrapper");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /**
     * The errorMessage port. It is of type String.
     */
    public TypedIOPort errorMessage;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /** Clone the object into the specified workspace. The new object is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  The result is an attribute with no container.
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException Not thrown in this base class
     *  @return The new Attribute.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        ModelAnalyzer newObject = (ModelAnalyzer) super.clone(workspace);
        if (newObject._analyzerWrapper != null) { 
            // FIXME: Why is this necessary?
            newObject._analyzerWrapper = (AnalyzerAttribute)newObject._analyzerWrapper.getAttribute("_analyzerWrapper");
        }
        return newObject;
    }

    /**
     * Consumes one token from the input and annotate the model contained
     * by the ActorToken with property information. Send a new ActorToken
     * of the annotated model to the output port and any error messages
     * to the errorMessage port.
     */
    public void fire() throws IllegalActionException {
        ActorToken token = (ActorToken) input.get(0);
        CompositeEntity entity = (CompositeEntity) token.getEntity();

        String errorString = _analyzerWrapper.analyze(entity);
        errorMessage.send(0, new StringToken(errorString));

        output.send(0, new ActorToken(entity));
    }

    ///////////////////////////////////////////////////////////////////
    ////                     private variables                     ////

    /** The analyzer attribute. */
    private AnalyzerAttribute _analyzerWrapper;
}
