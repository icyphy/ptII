/* An event to initialize the model parameter to contain an empty model.

 Copyright (c) 2008-2014 The Regents of the University of California.
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
package ptolemy.actor.gt.controller;

import ptolemy.actor.TypedCompositeActor;
import ptolemy.data.BooleanToken;
import ptolemy.data.Token;
import ptolemy.data.expr.StringParameter;
import ptolemy.domains.ptera.kernel.PteraDebugEvent;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// InitModel

/**
 An event to initialize the model parameter to contain an empty model.

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Yellow (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class InitModel extends GTEvent {

    /** Construct an event with the given name contained by the specified
     *  composite entity. The container argument must not be null, or a
     *  NullPointerException will be thrown. This event will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty
     *  string.
     *  Increment the version of the workspace.
     *  This constructor write-synchronizes on the workspace.
     *
     *  @param container The container.
     *  @param name The name of the state.
     *  @exception IllegalActionException If the state cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   that of an entity already in the container.
     */
    public InitModel(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        isInitialEvent.setToken(BooleanToken.TRUE);
        isInitialEvent.setVisibility(Settable.NONE);
        modelName = new StringParameter(this, "modelName");
    }

    /** React to a change in an attribute. If the changed attribute is
     *  the <code>modelName</code> attribute, create a new empty model with that
     *  name.
     *
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If thrown by the superclass
     *   attributeChanged() method, or the new model cannot be created.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == modelName) {
            _emptyModel = new TypedCompositeActor(new Workspace());
            try {
                _emptyModel.setName(modelName.stringValue());
            } catch (NameDuplicationException e) {
                throw new IllegalActionException(this, e, "Unexpected error.");
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Process this event and set the model parameter to contain an empty
     *  model, if it has not been set yet.
     *
     *  @param arguments The arguments used to process this event, which must be
     *   either an ArrayToken or a RecordToken.
     *  @return A refiring data structure that contains a non-negative double
     *   number if refire() should be called after that amount of model time, or
     *   null if refire() need not be called.
     *  @exception IllegalActionException If thrown by the superclass, or if the
     *   model parameter cannot be found.
     */
    @Override
    public RefiringData fire(Token arguments) throws IllegalActionException {
        RefiringData data = super.fire(arguments);

        ModelParameter modelParameter = getModelParameter();
        if (modelParameter.getModel() == null) {
            modelParameter.setModel(_getInitialModel());
        }

        _debug(new PteraDebugEvent(this, "Model initialized."));

        return data;
    }

    /** The name of the empty model.
     */
    public StringParameter modelName;

    /** Get the initial model in a new workspace.
     *
     *  @return The initial model.
     *  @exception IllegalActionException If the initial model cannot be
     *   obtained.
     */
    protected CompositeEntity _getInitialModel() throws IllegalActionException {
        try {
            return (CompositeEntity) _emptyModel.clone(new Workspace());
        } catch (CloneNotSupportedException e) {
            throw new IllegalActionException("Unable to clone an empty model.");
        }
    }

    /** An empty model.
     */
    private CompositeEntity _emptyModel;
}
