/*

 Copyright (c) 1997-2005 The Regents of the University of California.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

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

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.ActorToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.moml.MoMLParser;

/**

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class ModelReader extends TypedAtomicActor {

    public ModelReader(CompositeEntity container, String name)
    throws IllegalActionException, NameDuplicationException {
        super(container, name);

        setClassName("ptolemy.actor.gt.ModelReader");

        modelString = new TypedIOPort(this, "modelString", true, false);
        modelString.setTypeEquals(BaseType.STRING);
        modelEntity = new TypedIOPort(this, "modelEntity", false, true);
        modelEntity.setTypeEquals(ActorToken.TYPE);
    }

    public void fire() throws IllegalActionException {
        if (modelString.hasToken(0)) {
            StringToken stringToken = (StringToken) modelString.get(0);
            String modelString = stringToken.stringValue();
            _parser.reset();
            try {
                Entity entity = (Entity) _parser.parse(modelString);
                modelEntity.send(0, new ActorToken(entity));
            } catch (Exception e) {
                throw new IllegalActionException(this, e,
                        "Cannot parse model.");
            }
        }
    }

    public StringParameter defaultModel;

    public TypedIOPort modelEntity;

    public TypedIOPort modelString;

    private MoMLParser _parser = new MoMLParser();
}
