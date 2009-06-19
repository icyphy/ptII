/* A button to perform automatic graph layout.

 Copyright (c) 2008 The Regents of the University of California.
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
package ptolemy.vergil.basic.layout;

import javax.swing.JFrame;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.gui.properties.Button;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.MessageHandler;
import ptolemy.vergil.actor.ActorGraphFrame;
import ptolemy.vergil.basic.BasicGraphFrame;
import ptolemy.vergil.basic.layout.kieler.KielerLayout;
import diva.graph.GraphController;
import diva.graph.GraphModel;
import diva.graph.basic.BasicLayoutTarget;

//////////////////////////////////////////////////////////////////////////
//// LayoutButton

/**
 * A button to place all elements on the canvas, i.e. all connected and unconnected
 * objects. It does not do routing.
 * 
 * @author Hauke Fuhrmann
 * @version $Id: LayoutButton.java 53203 2009-04-24 00:27:45Z haf $
 * @since Ptolemy II 7.1
 * @Pt.ProposedRating Red (tfeng)
 * @Pt.AcceptedRating Red (tfeng)
 */
public class PlaceAllButton extends Button {

	/**
	 * Construct a GUI property with the given name contained by the specified
	 * entity. The container argument must not be null, or a
	 * NullPointerException will be thrown. This attribute will use the
	 * workspace of the container for synchronization and version counts. If the
	 * name argument is null, then the name is set to the empty string.
	 * Increment the version of the workspace.
	 * 
	 * @param container
	 *            The container.
	 * @param name
	 *            The name of this attribute.
	 * @exception IllegalActionException
	 *                If the attribute is not of an acceptable class for the
	 *                container, or if the name contains a period.
	 * @exception NameDuplicationException
	 *                If the name coincides with an attribute already in the
	 *                container.
	 */
	public PlaceAllButton(NamedObj container, String name)
			throws IllegalActionException, NameDuplicationException {
		super(container, name);
	}

	/**
	 * Perform the layout action.
	 */
	public void perform() {
		// Get the frame and the current model here.
		JFrame frame = _action.getFrame();
		NamedObj model = _action.getModel();
		// check for supported type of editor
		if (frame instanceof ActorGraphFrame && model instanceof CompositeActor) {
			BasicGraphFrame graphFrame = (BasicGraphFrame) frame;

			// fetch everything needed to build the LayoutTarget
			GraphController graphController = graphFrame.getJGraph().getGraphPane().getGraphController();
			GraphModel graphModel = graphFrame.getJGraph().getGraphPane().getGraphController().getGraphModel();
			BasicLayoutTarget layoutTarget = new BasicLayoutTarget(graphController);
			
			// create Kieler layouter for this layout target
			KielerLayout layout = new KielerLayout(layoutTarget);
			layout.setModel((CompositeActor)_action.getModel());
			layout.setApplyEdgeLayout(false);
			layout.setBoxLayout(true);
			
			// perform layout
			try{
			layout.layout(graphModel.getRoot());
			} catch (Exception exception) {
				MessageHandler.error("Failed executing automatic KIELER layout: "
						+ exception.getMessage(), exception);
			}

			
		} else
			MessageHandler
					.error("For now only actor oriented graphs with ports are supported by KIELER layout.");
	}
}
