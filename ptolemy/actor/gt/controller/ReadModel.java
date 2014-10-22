/* An event to read the model in a file into the model parameter.

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

import java.net.URL;

import ptolemy.data.Token;
import ptolemy.data.expr.FileParameter;
import ptolemy.domains.ptera.kernel.PteraDebugEvent;
import ptolemy.domains.ptera.kernel.PteraErrorEvent;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;
import ptolemy.moml.MoMLParser;

//////////////////////////////////////////////////////////////////////////
//// ReadModel

/**
 An event to read the model in a file into the model parameter.

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Yellow (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 @see WriteModel
 */
public class ReadModel extends GTEvent {

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
    public ReadModel(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        modelFile = new FileParameter(this, "modelFile");
    }

    /** Clone the event into the specified workspace. This calls the
     *  base class and then sets the attribute and port public members
     *  to refer to the attributes and ports of the new state.
     *
     *  @param workspace The workspace for the new event.
     *  @return A new event.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        ReadModel newObject = (ReadModel) super.clone(workspace);
        newObject._parser = null;
        return newObject;
    }

    /** Process this event and read the model in file into the model parameter.
     *
     *  @param arguments The arguments used to process this event, which must be
     *   either an ArrayToken or a RecordToken.
     *  @return A refiring data structure that contains a non-negative double
     *   number if refire() should be called after that amount of model time, or
     *   null if refire() need not be called.
     *  @exception IllegalActionException If the file cannot be read, or if
     *   thrown by the superclass.
     */
    @Override
    public RefiringData fire(Token arguments) throws IllegalActionException {
        RefiringData data = super.fire(arguments);

        if (_parser == null) {
            _parser = new MoMLParser();
        } else {
            _parser.reset();
        }

        CompositeEntity model;
        URL url = modelFile.asURL();
        try {
            model = (CompositeEntity) _parser.parse(url, url);
            _debug(new PteraDebugEvent(this, "Read file " + url));
        } catch (Exception e) {
            _debug(new PteraErrorEvent(this, "Unable to read file " + url));
            throw new IllegalActionException(this, e, "Unable to parse the "
                    + "model from file \"" + modelFile.stringValue().trim()
                    + "\" as a CompositeEntity.");
        } finally {
            MoMLParser.purgeModelRecord(url);
        }
        getModelParameter().setModel(model);

        return data;
    }

    /** The model file.
     */
    public FileParameter modelFile;

    /** The moml parser to parse the file.
     */
    private MoMLParser _parser;
}
