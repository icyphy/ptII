/*

 Copyright (c) 2008-2009 The Regents of the University of California.
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

//////////////////////////////////////////////////////////////////////////
//// View

/**


 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 7.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class View extends GTEvent {

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

        _init();
    }

    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        View newObject = (View) super.clone(workspace);
        newObject._init();
        return newObject;
    }

    public RefiringData fire(Token arguments) throws IllegalActionException {
        RefiringData data = super.fire(arguments);

        Effigy effigy = EventUtils.findToplevelEffigy(this);
        if (effigy == null) {
            // The effigy may be null if the model is closed.
            return data;
        }

        CompositeEntity entity = getModelParameter().getModel();
        try {
            entity = (CompositeEntity) GTTools.cleanupModel(entity, _parser);
        } finally {
            _parser.reset();
        }

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
                sizeAttribute.setExpression("[" + newSize.width + ", " +
                        newSize.height + "]");
            }

            boolean reopen = ((BooleanToken) reopenWindow.getToken())
                    .booleanValue();
            Tableau tableau = EventUtils.getTableau(this, referredTableau,
                    this.tableau);
            if (tableau != null &&
                    !(tableau.getFrame() instanceof ExtendedGraphFrame)) {
                EventUtils.setTableau(this, referredTableau, this.tableau,
                        null);
                EventUtils.closeTableau(tableau);
                tableau = null;
            }

            boolean openNewWindow = true;
            if (!reopen  && tableau != null) {
                JFrame frame = tableau.getFrame();
                if (frame instanceof BasicGraphFrame &&
                        ((BasicGraphFrame) frame).getEffigy() != null) {
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
                IntMatrixToken location =
                    (IntMatrixToken) screenLocation.getToken();
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
                newLocation.x = Math.min(newLocation.x,
                        screenSize.width - newSize.width);
                newLocation.y = Math.min(newLocation.y,
                        screenSize.height - newSize.height);
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
            entity.setDeferringChangeRequests(false);
        } catch (NameDuplicationException e) {
            throw new IllegalActionException(this, e, "Cannot open model.");
        } catch (Exception e) {
            throw new IllegalActionException(this, e, "Cannot parse model.");
        }

        return data;
    }

    public void initialize() throws IllegalActionException {
        super.initialize();
        _workspace.removeAll();
    }

    public StringParameter referredTableau;

    public Parameter reopenWindow;

    public Parameter screenLocation;

    public Parameter screenSize;

    public TableauParameter tableau;

    public Parameter title;

    private void _init() {
        _workspace = new Workspace();
        _parser = new MoMLParser(_workspace);
    }

    private MoMLParser _parser;

    private Workspace _workspace;
}
