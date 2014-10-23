/* A simple graph view for Ptolemy models

 Copyright (c) 1998-2014 The Regents of the University of California.
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
 2
 */
package ptolemy.vergil.basic;

import java.awt.Color;
import java.util.List;

import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.PtolemyPreferences;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;
import diva.canvas.DamageRegion;
import diva.graph.GraphController;
import diva.graph.GraphModel;
import diva.graph.GraphPane;

///////////////////////////////////////////////////////////////////
//// BasicGraphPane

/**
 A simple graph pane that has an associated Ptolemy model and handles
 getting the background color from the preferences.

 @author  Edward A. Lee
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (eal)
 */
public class BasicGraphPane extends GraphPane {

    /** Create a pane that updates the background color on each repaint
     *  if there is a preference attribute.
     *  @param controller The controller.
     *  @param model The graph model.
     *  @param entity The Ptolemy II model being displayed.
     */
    public BasicGraphPane(GraphController controller, GraphModel model,
            NamedObj entity) {
        super(controller, model);
        _entity = entity;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the base class to set the background. */
    @Override
    public void repaint() {
        _setBackground();
        super.repaint();
    }

    /** Override the base class to set the background. */
    @Override
    public void repaint(DamageRegion damage) {
        _setBackground();
        super.repaint(damage);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** If the model contains local preferences, use that to set the background
     *  color. Otherwise, use the global preferences from the configuration.
     */
    private void _setBackground() {
        if (_entity != null) {
            // First, see whether there is a local preferences object
            // in the model.
            List list = _entity.attributeList(PtolemyPreferences.class);
            if (list.size() > 0) {
                // Use the last of the preferences if there is more than one.
                PtolemyPreferences preferences = (PtolemyPreferences) list
                        .get(list.size() - 1);
                getCanvas()
                .setBackground(preferences.backgroundColor.asColor());
                return;
            }
            // There is no local preferences. If we have previously
            // looked up a default color in the configuration, use that
            // color.
            if (_defaultColor != null) {
                getCanvas().setBackground(_defaultColor);
                return;
            }
            // Look for default preferences in the configuration.
            Effigy effigy = Configuration.findEffigy(_entity.toplevel());
            if (effigy == null) {
                // No effigy. Can't find a configuration.
                return;
            }
            Configuration configuration = (Configuration) effigy.toplevel();
            try {
                PtolemyPreferences preferences = PtolemyPreferences
                        .getPtolemyPreferencesWithinConfiguration(configuration);
                if (preferences != null) {
                    _defaultColor = preferences.backgroundColor.asColor();
                    getCanvas().setBackground(_defaultColor);
                    return;
                }
            } catch (IllegalActionException ex) {
                System.err
                .println("Warning, failed to find Ptolemy Preferences "
                        + "or set the background, using default.");
                ex.printStackTrace();
            }
            if (_backgroundWarningCount < 1) {
                _backgroundWarningCount++;
                // If there is no actor library, do not issue a warning.
                if (configuration.getEntity("actor library") != null) {
                    System.out
                    .println("Configuration does not contain a PtolemyPreferences object. "
                            + "Using default background color.");
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Counter to ensure that no more than one message is printed
     *  when we fail to find the default preferences in the configuration.
     */
    private static int _backgroundWarningCount = 0;

    /** The default color from the configuration. */
    private Color _defaultColor = null;

    /** The Ptolemy object being displayed. */
    private NamedObj _entity;
}
