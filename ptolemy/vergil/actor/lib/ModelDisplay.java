/*  This actor opens a window to display the specified model and applies its inputs to the model.

 @Copyright (c) 2007-2014 The Regents of the University of California.
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
package ptolemy.vergil.actor.lib;

import java.awt.Container;
import java.awt.Dimension;
import java.net.URL;
import java.util.List;

import ptolemy.actor.TypedIOPort;
import ptolemy.actor.gui.AbstractPlaceableActor;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.PtolemyEffigy;
import ptolemy.actor.gui.SizeAttribute;
import ptolemy.actor.gui.Tableau;
import ptolemy.actor.gui.TableauFrame;
import ptolemy.data.StringToken;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.ChangeListener;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.moml.MoMLParser;
import ptolemy.moml.ParserAttribute;
import ptolemy.vergil.actor.ActorEditorGraphController;
import ptolemy.vergil.actor.ActorGraphModel;
import diva.graph.GraphPane;
import diva.graph.JGraph;

///////////////////////////////////////////////////////////////////
//// ModelDisplay

/**
 This actor opens a window to display the specified model.
 If inputs are provided, they are expected to be MoML strings
 that are to be applied to the model. This can be used, for
 example, to create animations.

 @author  Edward A. Lee, Elaine Cheong
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (cxh)
 */
public class ModelDisplay extends AbstractPlaceableActor implements
        ChangeListener {

    /** Construct an actor with the specified container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public ModelDisplay(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        modelURL = new FileParameter(this, "modelURL");
        input = new TypedIOPort(this, "input", true, false);
        input.setTypeEquals(BaseType.STRING);
    }

    ///////////////////////////////////////////////////////////////////
    ////                   ports and parameters                    ////

    /** The file or URL from which to read the starting point model.
     *  The model is read when this parameter is set or changed.
     */
    public FileParameter modelURL;

    /** The input port through which to provide MoML to modify the model.
     *  This has type string.
     */
    public TypedIOPort input;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to a change in an attribute.  If the attribute is
     *  modelURL, then read the specified URL and parse it to create
     *  the entity to display.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the change is not acceptable
     *   to this container (not thrown in this base class).
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == modelURL) {
            URL url = modelURL.asURL();
            if (url != null) {
                MoMLParser parser = new MoMLParser();
                try {
                    _entity = parser.parse(null, url);
                    ParserAttribute parserAttribute = new ParserAttribute(
                            _entity, "_parser");
                    parserAttribute.setParser(parser);
                } catch (Exception ex) {
                    throw new IllegalActionException(this, ex,
                            "Failed to read model from: " + url);
                }
            } else {
                // No URL given, so we should create a blank entity.
                _entity = _createBlankEntity();
            }
            // Make sure there is no display of the old entity.
            place(null);
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Do nothing.
     *  @param change The change that succeeded.
     */
    @Override
    public void changeExecuted(ChangeRequest change) {
    }

    /** Stop executing the model.
     *  @param change The change.
     *  @param exception The exception.
     */
    @Override
    public void changeFailed(ChangeRequest change, Exception exception) {
        stop();
    }

    /** Read the input, if there is any, and issue a change
     *  request to apply the MoML in the input to the displayed model.
     *  @exception IllegalActionException If there is an error reading
     *   the input.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        if (input.isOutsideConnected() && input.hasToken(0)) {
            String moml = ((StringToken) input.get(0)).stringValue();
            MoMLChangeRequest request = new MoMLChangeRequest(this, _entity,
                    moml);
            request.addChangeListener(this);
            _entity.requestChange(request);
        }
    }

    /** Clone the actor into the specified workspace.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class has an
     *   attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        ModelDisplay newObject = (ModelDisplay) super.clone(workspace);
        newObject._entity = _createBlankEntity();
        return newObject;
    }

    /** If the model is not yet displayed, then display it in its
     *  own window.
     *  @exception IllegalActionException If there is an constructing
     *   the display.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();

        // If we have no entity at this point, then create a simple
        // top-level entity into which we can put attributes.
        if (_entity == null) {
            _entity = _createBlankEntity();
        }

        // If there is no graph display yet, then create a
        // standalone window in which to display the model.
        if (_graph == null) {
            Effigy containerEffigy = Configuration.findEffigy(toplevel());
            try {
                _effigy = new PtolemyEffigy(containerEffigy,
                        "ModelDisplay Effigy");
                _effigy.setModel(_entity);
                _tableau = new Tableau(_effigy, "tableau");
            } catch (NameDuplicationException e) {
                throw new IllegalActionException(this, e,
                        "Failed to create tableau.");
            }
            _frame = new TableauFrame(_tableau);
            setFrame(_frame);
            _tableau.setFrame(_frame);
            place(_frame.getContentPane());
            _frame.pack();
        }
        if (_frame != null) {
            ((TableauFrame) _frame).show();
            _frame.toFront();
        }
    }

    /** Place the display in the specified container.
     *  @param container The container, or null to remove it from any
     *   existing container.
     */
    @Override
    public void place(Container container) {
        if (container == null) {
            if (_frame != null) {
                _frame.dispose();
            }
            _frame = null;
            if (_tableau != null) {
                try {
                    _tableau.setContainer(null);
                    _effigy.setContainer(null);
                } catch (Exception e) {
                    throw new InternalErrorException(e);
                }
            }
            _tableau = null;
            _graph = null;
        } else {
            ActorEditorGraphController controller = new ActorEditorGraphController();
            // _entity might be null, in which case we have to make an empty model.
            if (_entity == null) {
                _entity = _createBlankEntity();
            }

            ActorGraphModel graphModel = new ActorGraphModel(_entity);
            GraphPane graphPane = new GraphPane(controller, graphModel);
            _graph = new JGraph(graphPane);

            // If the model has a recorded size, use it.
            List size = _entity.attributeList(SizeAttribute.class);
            if (size.size() > 0) {
                ((SizeAttribute) size.get(0)).setSize(_graph);
            } else {
                _graph.setMinimumSize(new Dimension(200, 200));
                _graph.setMaximumSize(new Dimension(200, 200));
                _graph.setPreferredSize(new Dimension(200, 200));
                _graph.setSize(200, 200);
            }
            // _graph.setBackground(Color.white);

            container.add(_graph);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Create a blank entity associated with this display.
     */
    private static NamedObj _createBlankEntity() {
        String moml = "<entity name=\"top\" class=\"ptolemy.kernel.CompositeEntity\"/>";
        NamedObj entity = null;
        MoMLParser parser = new MoMLParser();
        try {
            entity = parser.parse(null, moml);
            ParserAttribute parserAttribute = new ParserAttribute(entity,
                    "_parser");
            parserAttribute.setParser(parser);
        } catch (Exception ex) {
            throw new InternalErrorException(ex);
        }
        return entity;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The effigy representing the model. */
    private PtolemyEffigy _effigy;

    /** The top-level entity read from the file or URL. */
    private NamedObj _entity;

    /** The graph display pane. */
    private JGraph _graph;

    /** The tableau, if the model is displayed in its own window. */
    private Tableau _tableau;
}
