/*  This actor opens a window to display the specified model and applies its inputs to the model.

@Copyright (c) 2007-2008 The Regents of the University of California.
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

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.ActorToken;
import ptolemy.data.StringToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.moml.MoMLParser;

//////////////////////////////////////////////////////////////////////////
//// ModelGenerator

/**
This actor opens a window to display the specified model.
If inputs are provided, they are expected to be MoML strings
that are to be applied to the model. This can be used, for
example, to create animations.

@author  Thomas Huining Feng
@version $Id$
@since Ptolemy II 6.0
@Pt.ProposedRating Yellow (eal)
@Pt.AcceptedRating Red (cxh)
*/
public class ModelGenerator extends TypedAtomicActor {

    public ModelGenerator(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        setClassName("ptolemy.actor.gt.ModelGenerator");

        moml = new TypedIOPort(this, "moml", true, false);
        moml.setTypeEquals(BaseType.STRING);

        modelName = new TypedIOPort(this, "modelName", true, false);
        modelName.setTypeEquals(BaseType.STRING);

        model = new TypedIOPort(this, "model", false, true);
        model.setTypeEquals(ActorToken.TYPE);
    }

    public Object clone() throws CloneNotSupportedException {
        ModelGenerator actor = (ModelGenerator) super.clone();
        actor._emptyModel = null;
        actor._parser = new MoMLParser();
        return actor;
    }

    public void fire() throws IllegalActionException {
        try {
            Entity entity;
            if (moml.getWidth() > 0 && moml.hasToken(0)) {
                String momlString = ((StringToken) moml.get(0)).stringValue();
                _parser.reset();
                entity = (Entity) _parser.parse(momlString);
            } else {
                if (_emptyModel == null) {
                    _emptyModel = new TypedCompositeActor(workspace());
                }
                entity = _emptyModel;
            }

            if (modelName.getWidth() > 0 && modelName.hasToken(0)) {
                String name = ((StringToken) modelName.get(0)).stringValue();
                entity.setName(name);
            }

            model.send(0, new ActorToken(entity));
        } catch (Exception e) {
            throw new IllegalActionException(this, "Unable to parse moml.");
        }
    }

    public boolean prefire() throws IllegalActionException {
        return super.prefire()
                && (moml.getWidth() > 0 && moml.hasToken(0) || modelName
                        .getWidth() > 0
                        && modelName.hasToken(0));
    }

    public TypedIOPort model;

    public TypedIOPort modelName;

    public TypedIOPort moml;

    private Entity _emptyModel;

    private MoMLParser _parser = new MoMLParser();
}
