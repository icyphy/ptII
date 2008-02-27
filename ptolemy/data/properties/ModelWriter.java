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

import java.io.IOException;
import java.io.Writer;

import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.Transformer;
import ptolemy.data.ActorToken;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// ModelWriter

/**
This actor writes actor tokens to file.


@author  Man-Kit Leung
@version $Id$
@since Ptolemy II 7.0
@Pt.ProposedRating Red (mankit)
@Pt.AcceptedRating Red (mankit)
*/
public class ModelWriter extends Transformer {

    public ModelWriter(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        modelFile = new FileParameter(this, "modelFile");
        modelFile.setTypeEquals(BaseType.STRING);
        
        input.setTypeEquals(ActorToken.TYPE);
        output.setTypeEquals(ActorToken.TYPE);
    }

    public void fire() throws IllegalActionException {
        Writer writer = modelFile.openForWriting();
        ActorToken token = (ActorToken) input.get(0);

        try {
            token.getEntity().exportMoML(writer);
        } catch (IOException ex) {
            throw new IllegalActionException(this, ex,
                    "Cannot write to \"" + 
                    modelFile.stringValue() + "\".");
        }
        
        modelFile.close();
        output.send(0, token);
    }

    public boolean prefire() throws IllegalActionException {
        return super.prefire() && input.hasToken(0);
    }

    public FileParameter modelFile;

    public TypedIOPort moml;
}
