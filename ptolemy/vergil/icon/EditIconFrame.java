/* An icon editor frame for Ptolemy models.

 Copyright (c) 1998-2003 The Regents of the University of California.
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

@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (johnr@eecs.berkeley.edu)
*/

package ptolemy.vergil.icon;

import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.net.URL;

import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.Tableau;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.Workspace;
import ptolemy.moml.LibraryAttribute;
import ptolemy.moml.MoMLParser;
import ptolemy.vergil.actor.ActorGraphModel;
import ptolemy.vergil.basic.BasicGraphFrame;
import diva.canvas.FigureLayer;
import diva.canvas.JCanvas;
import diva.canvas.toolbox.BasicRectangle;
import diva.graph.GraphPane;

//////////////////////////////////////////////////////////////////////////
//// EditIconFrame
/**
This is an icon editor frame for Ptolemy II models.
<p>
One aspect in which this editor differs from the base class is that it
does not support the "drop into" feature, where if you drop a new instance
onto an instance of NamedObj, that instance of NamedObj becomes the container
of the new object.  This feature is not useful for icon editing, and results
in visual elements mysteriously disappearing when they are dropped.

@author  Edward A. Lee
@version $Id$
*/
public class EditIconFrame extends BasicGraphFrame {

    /** Construct a frame to edit the specified icon.
     *  After constructing this, it is necessary
     *  to call setVisible(true) to make the frame appear.
     *  This is typically done by calling show() on the controlling tableau.
     *  This constructor results in a frame that obtains its library
     *  either from the model (if it has one) or the default library defined
     *  in the configuration.
     *  @see Tableau#show()
     *  @param icon The icon to put in this frame.
     *  @param tableau The tableau responsible for this frame.
     */
    public EditIconFrame(EditorIcon icon, Tableau tableau) {
        this(icon, tableau, null);
    }

    /** Construct a frame to edit the specified icon.
     *  After constructing this, it is necessary
     *  to call setVisible(true) to make the frame appear.
     *  This is typically done by calling show() on the controlling tableau.
     *  This constructor results in a graph frame that obtains its library
     *  either from the model (if it has one), or the <i>defaultLibrary</i>
     *  argument (if it is non-null), or the default library defined
     *  in the configuration.
     *  @see Tableau#show()
     *  @param entity The model to put in this frame.
     *  @param tableau The tableau responsible for this frame.
     *  @param defaultLibrary An attribute specifying the default library
     *   to use if the model does not have a library.
     */
    public EditIconFrame(
            EditorIcon entity,
            Tableau tableau,
            LibraryAttribute defaultLibrary) {
        super(entity, tableau, defaultLibrary);
        
        _dropTarget.setDropIntoEnabled(false);

        // Override the default help file.
        helpFile = "ptolemy/configs/doc/vergilGraphEditorHelp.htm";
        
        zoomReset();
        
        _drawReferenceBox();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Set zoom to the nominal.  This overrides the base class to set
     *  a zoom factor and center more appropriate for editing icons.
     */
    public void zoomReset() {
        JCanvas canvas = _jgraph.getGraphPane().getCanvas();
        AffineTransform current =
            canvas.getCanvasPane().getTransformContext().getTransform();
        current.setToScale(_ZOOM_SCALE, _ZOOM_SCALE);
        canvas.getCanvasPane().setTransform(current);
        setCenter(new Point2D.Double(0.0, 0.0));
        if (_graphPanner != null) {
            _graphPanner.repaint();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Create the menus that are used by this frame.
     *  It is essential that _createGraphPane() be called before this.
     */
    protected void _addMenus() {
        super._addMenus();
    }

    /** Create the default library to use if an entity has no
     *  LibraryAttribute.  Note that this is called in the
     *  constructor and therefore overrides in subclasses
     *  should not refer to any members that may not have been 
     *  initialized.  This method overrides the base class to
     *  look for a library called "icon library" in the
     *  configuration. If there is no such library, then
     *  it provides a simple default library, created in
     *  the specified workspace.
     *  @param workspace The workspace in which to create
     *   the library, if one needs to be created.
     *  @return The new library, or null if there is no
     *   configuration.
     */
    protected CompositeEntity _createDefaultLibrary(Workspace workspace) {
        Configuration configuration = getConfiguration();
        if (configuration != null) {
            CompositeEntity result = (CompositeEntity)
                    configuration.getEntity("icon editor library");
            if (result == null) {
                // Create a default library by directly reading the
                // default XML description.
                URL source = getClass().getClassLoader().getResource(
                        "ptolemy/vergil/kernel/attributes/iconEditorLibrary.xml");
                MoMLParser parser = new MoMLParser(workspace);
                try {
                    result = (CompositeEntity)parser.parse(null, source);
                } catch (Exception e) {
                    throw new InternalErrorException(
                    "Unable to open default icon editor library: " + e);
                }
            }
            return result;
        } else {
            return null;
        }
    }

    /** Create a new graph pane. Note that this method is called in
     *  constructor of the base class, so it must be careful to not reference
     *  local variables that may not have yet been created.
     */
    protected GraphPane _createGraphPane() {
        _controller = new EditIconGraphController();
        _controller.setConfiguration(getConfiguration());
        _controller.setFrame(this);
        ActorGraphModel graphModel = new ActorGraphModel(getModel());
        return new GraphPane(_controller, graphModel);
    }
    
    /** Draw a reference box with the default icon size, 60x40.
     */
    protected void _drawReferenceBox() {
        // The background layer is a FigureLayer, despite the fact that
        // getBackgroundLayer() only returns a CanvasLayer.
        FigureLayer layer = (FigureLayer)_jgraph.getGraphPane().getBackgroundLayer();
        layer.setVisible(true);
        BasicRectangle reference = new BasicRectangle(-30.0, -20.0, 60.0, 40.0, 0.1f);
        reference.setStrokePaint(Color.BLUE);
        layer.add(reference);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private EditIconGraphController _controller;

    // The delay time specified that last time animation was set.
    private long _lastDelayTime = 0;
    
    // The default zoom scale.
    private double _ZOOM_SCALE = 4.0;
}
