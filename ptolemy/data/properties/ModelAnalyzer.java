/*  This actor opens a window to display the specified model and applies its inputs to the model.

 @Copyright (c) 1998-2007 The Regents of the University of California.
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
package ptolemy.data.properties;

import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.Transformer;
import ptolemy.data.ActorToken;
import ptolemy.data.StringToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// ModelGenerator

/**
This actor opens a window to display the specified model.
If inputs are provided, they are expected to be MoML strings
that are to be applied to the model. This can be used, for
example, to create animations.

@author  Man-Kit Leung
@version $Id$
@since Ptolemy II 6.0
@Pt.ProposedRating Yellow (eal)
@Pt.AcceptedRating Red (cxh)
*/
public class ModelAnalyzer extends Transformer {

    public ModelAnalyzer(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        setClassName("ptolemy.data.properties.ModelAnalyzer");

        
        errorMessage = new TypedIOPort(this, "errorMessage", false, true);
        errorMessage.setTypeEquals(BaseType.STRING);
        
        input.setTypeEquals(ActorToken.TYPE);
        
        output.setTypeEquals(ActorToken.TYPE);    
        
        _analyzerWrapper = new AnalyzerAttribute(this, "_analyzerWrapper");
    }

    public TypedIOPort errorMessage;

    /** React to a change in an attribute. 
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the change is not acceptable
     *   to this container (not thrown in this class).
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        
        super.attributeChanged(attribute);
    }
    
    public void fire() throws IllegalActionException {
        
        ActorToken token = (ActorToken) input.get(0);
        CompositeEntity entity = (CompositeEntity) token.getEntity();
        
        String errorString = _analyzerWrapper.analyze(entity);
        errorMessage.send(0, new StringToken(errorString));

        output.send(0, new ActorToken(entity));
    }

    private AnalyzerAttribute _analyzerWrapper;
}
