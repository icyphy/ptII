/* An event to view the model in the model parameter in a separate window.

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

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.net.URI;

import javax.swing.JFrame;

import ptolemy.actor.gt.GTTools;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.SizeAttribute;
import ptolemy.actor.gui.Tableau;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntMatrixToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.ptera.lib.EventUtils;
import ptolemy.domains.ptera.lib.TableauParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.attributes.URIAttribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;
import ptolemy.moml.MoMLParser;
import ptolemy.vergil.basic.BasicGraphFrame;
import ptolemy.vergil.basic.ExtendedGraphFrame;
import ptolemy.vergil.gt.GTFrameTools;

///////////////////////////////////////////////////////////////////
//// View

/**
 An event to view the model in the model parameter in a separate window. If the
 tableau parameter is ignored as the default, a new window is opened for each
 such event. If the {@link #referredTableau} parameter is specified with the
 name of a tableau, then the specified tableau will be used to view the model.

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Yellow (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class View extends GTEvent {

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
    public View(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        referredTableau = new StringParameter(this, "referredTableau");

        title = new Parameter(this, "title");
        title.setStringMode(true);
        title.setExpression("");

        screenLocation = new Parameter(this, "screenLocation");
        screenLocation.setTypeEquals(BaseType.INT_MATRIX);
        screenLocation.setToken("[-1, -1]");

        screenSize = new Parameter(this, "screenSize");
        screenSize.setTypeEquals(BaseType.INT_MATRIX);
        screenSize.setToken("[-1, -1]");

        reopenWindow = new Parameter(this, "reopenWindow");
        reopenWindow.setTypeEquals(BaseType.BOOLEAN);
        reopenWindow.setToken(BooleanToken.FALSE);

        tableau = new TableauParameter(this, "tableau");
        tableau.setPersistent(false);
        tableau.setVisibility(Settable.EXPERT);

        isPersistent = new Parameter(this, "isPersistent");
        isPersistent.setTypeEquals(BaseType.BOOLEAN);
        isPersistent.setToken(BooleanToken.FALSE);

        _init();
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** If the isPersistent parameter is false, then the user will not
     *  be prompted to save the model upon closing.  Models in the
     *  test suite might want to have this parameter set to false so
     *  as to avoid a dialog asking if the user wants to save the
     *  model.  The default is a boolean with a value of false,
     *  indicating that the user will be not prompted to save the model if
     *  the model has changed.
     */
    public Parameter isPersistent;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

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
        View newObject = (View) super.clone(workspace);
        newObject._init();
        return newObject;
    }

    /** Process this event and show the model in the model parameter in the
     *  designated tableau.
     *
     *  @param arguments The arguments used to process this event, which must be
     *   either an ArrayToken or a RecordToken.
     *  @return A refiring data structure that contains a non-negative double
     *   number if refire() should be called after that amount of model time, or
     *   null if refire() need not be called.
     *  @exception IllegalActionException If the tableau cannot be used, or if
     *   thrown by the superclass.
     */
    @Override
    public RefiringData fire(Token arguments) throws IllegalActionException {
        RefiringData data = super.fire(arguments);

        Effigy effigy = EventUtils.findToplevelEffigy(this);
        if (effigy == null) {
            // The effigy may be null if the model is closed.
            return data;
        }
        _parser.reset();
        CompositeEntity entity = (CompositeEntity) GTTools.cleanupModel(
                getModelParameter().getModel(), _parser);

        try {
            // Compute size of the new frame.
            IntMatrixToken size = (IntMatrixToken) screenSize.getToken();
            int width = size.getElementAt(0, 0);
            int height = size.getElementAt(0, 1);
            Dimension newSize = null;
            if (width >= 0 && height >= 0) {
                newSize = new Dimension(width, height);
                SizeAttribute sizeAttribute = (SizeAttribute) entity
                        .getAttribute("_vergilSize", SizeAttribute.class);
                if (sizeAttribute == null) {
                    sizeAttribute = new SizeAttribute(entity, "_vergilSize");
                }
                sizeAttribute.setExpression("[" + newSize.width + ", "
                        + newSize.height + "]");
            }

            boolean reopen = ((BooleanToken) reopenWindow.getToken())
                    .booleanValue();
            Tableau tableau = EventUtils.getTableau(this, referredTableau,
                    this.tableau);

            if (tableau != null
                    && !(tableau.getFrame() instanceof ExtendedGraphFrame)) {

                EventUtils
                .setTableau(this, referredTableau, this.tableau, null);
                EventUtils.closeTableau(tableau);
                tableau = null;
            }

            boolean openNewWindow = true;
            if (!reopen && tableau != null) {
                JFrame frame = tableau.getFrame();
                if (frame instanceof BasicGraphFrame
                        && ((BasicGraphFrame) frame).getEffigy() != null) {
                    openNewWindow = false;
                }
            }
            if (openNewWindow) {
                if (tableau != null) {
                    EventUtils.closeTableau(tableau);
                }
                Configuration configuration = (Configuration) effigy.toplevel();
                tableau = configuration.openInstance(entity, effigy);
                EventUtils.setTableau(this, referredTableau, this.tableau,
                        tableau);
                // Set uri to null so that we don't accidentally overwrite the
                // original file by pressing Ctrl-S.
                ((Effigy) tableau.getContainer()).uri.setURI(null);
            } else {
                GTFrameTools.changeModel((BasicGraphFrame) tableau.getFrame(),
                        entity, true, true);
            }

            if (openNewWindow) {
                JFrame frame = tableau.getFrame();

                // Compute location of the new frame.
                IntMatrixToken location = (IntMatrixToken) screenLocation
                        .getToken();
                int x = location.getElementAt(0, 0);
                int y = location.getElementAt(0, 1);
                Point newLocation;
                if (x >= 0 && y >= 0) {
                    newLocation = new Point(x, y);
                } else {
                    newLocation = frame.getLocation();
                }

                if (newSize == null) {
                    newSize = frame.getSize();
                }

                // Move the frame to the edge if it exceeds the
                // screen.
                Toolkit toolkit = Toolkit.getDefaultToolkit();
                Dimension screenSize = toolkit.getScreenSize();
                newLocation.x = Math.min(newLocation.x, screenSize.width
                        - newSize.width);
                newLocation.y = Math.min(newLocation.y, screenSize.height
                        - newSize.height);
                frame.setLocation(newLocation);
            }

            String titleValue = ((StringToken) title.getToken()).stringValue();
            String titleString = null;
            String modelName = entity.getName();
            URI uri = URIAttribute.getModelURI(entity);
            if (titleValue.equals("")) {
                if (uri == null || modelName.equals("")) {
                    titleString = "Unnamed";
                } else {
                    titleString = uri.toString();
                }
                titleString += " (" + getName() + ")";
            } else {
                titleString = titleValue;
            }
            tableau.setTitle(titleString);
            boolean isPersistentValue = ((BooleanToken) isPersistent.getToken())
                    .booleanValue();
            // Mark the Effigy as not persistent so that when the
            // Tableau is closed we don't prompt the user for
            // saving.

            // To replicate, run $PTII/bin/vergil
            // ~/ptII/ptolemy/actor/gt/demo/ConstOptimization/ConstOptimization.xml
            // and then close the optimized model.  You should not be
            // prompted for save.
            ((Effigy) tableau.getContainer()).setPersistent(isPersistentValue);
            entity.setDeferringChangeRequests(false);
        } catch (NameDuplicationException e) {
            throw new IllegalActionException(this, e, "Cannot open model.");
        } catch (Exception e) {
            throw new IllegalActionException(this, e, "Cannot parse model.");
        }

        return data;
    }

    /** Initialize this event.
     *
     *  @exception IllegalActionException If thrown by the superclass.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        _workspace.removeAll();
    }

    /** Name of the tableau referred to, or an empty string if the default
     *  tableau is to be used.
     */
    public StringParameter referredTableau;

    /** Whether the window should be closed and reopened on each update.
     */
    public Parameter reopenWindow;

    /** Location of the new window, or [-1, -1] if the default location is to be
     *  used.
     */
    public Parameter screenLocation;

    /** Size of the new window, or [-1, -1] if the default size is to be used.
     */
    public Parameter screenSize;

    /** The default tableau.
     */
    public TableauParameter tableau;

    /** Title of the window.
     */
    public Parameter title;

    /** Create a parser for parsing models.
     */
    private void _init() {
        _workspace = new Workspace();
        _parser = new MoMLParser(_workspace);
    }

    /** The parser.
     */
    private MoMLParser _parser;

    /** The workspace for the parser.
     */
    private Workspace _workspace;
}
