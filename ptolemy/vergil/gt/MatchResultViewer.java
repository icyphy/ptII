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
package ptolemy.vergil.gt;

import java.awt.Color;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;

import ptolemy.actor.AtomicActor;
import ptolemy.actor.gt.data.MatchResult;
import ptolemy.actor.gui.Tableau;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.LibraryAttribute;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.vergil.actor.ActorEditorGraphController;
import ptolemy.vergil.kernel.AnimationRenderer;
import ptolemy.vergil.toolbox.FigureAction;
import diva.canvas.Figure;
import diva.gui.GUIUtilities;

public class MatchResultViewer extends AbstractGTFrame {

    /** Construct a frame associated with the specified Ptolemy II model.
     *  After constructing this, it is necessary
     *  to call setVisible(true) to make the frame appear.
     *  This is typically done by calling show() on the controlling tableau.
     *  This constructor results in a graph frame that obtains its library
     *  either from the model (if it has one) or the default library defined
     *  in the configuration.
     *  @see Tableau#show()
     *  @param entity The model to put in this frame.
     *  @param tableau The tableau responsible for this frame.
     */
    public MatchResultViewer(CompositeEntity entity, Tableau tableau) {
        this(entity, tableau, null);
    }

    /** Construct a frame associated with the specified Ptolemy II model.
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
    public MatchResultViewer(CompositeEntity entity, Tableau tableau,
            LibraryAttribute defaultLibrary) {
        super(entity, tableau, defaultLibrary);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                _windowClosed();
            }
        });

        _checkContainingViewer();
        _enableOrDisable();
        highlightMatchedObjects();
    }

    public void dehighlightMatchedObject(NamedObj object) {
        Object location = object.getAttribute("_location");
        Figure figure = _getGraphController().getFigure(location);
        _decorator.renderDeselected(figure);
    }

    public void highlightMatchedObject(NamedObj object) {
        Object location = object.getAttribute("_location");
        Figure figure = _getGraphController().getFigure(location);
        _decorator.renderSelected(figure);
    }

    public void highlightMatchedObjects() {
        if (_results != null) {
            CompositeEntity matcher = _getCurrentContainer();
            Set<?> matchedHostObjects = _results.get(_currentPosition).values();
            for (Object child : matcher.entityList(AtomicActor.class)) {
                if (matchedHostObjects.contains(child)) {
                    highlightMatchedObject((NamedObj) child);
                } else {
                    dehighlightMatchedObject((NamedObj) child);
                }
            }
        }
    }

    public void setMatchResult(List<MatchResult> results) {
        _results = results;
        _currentPosition = 0;
        _enableOrDisable();
        highlightMatchedObjects();
    }

    /** React to a change in the state of the tabbed pane.
     *  @param event The event.
     */
    public void stateChanged(ChangeEvent event) {
        super.stateChanged(event);

        if (event.getSource() == _getTabbedPane()) {
            _asynchronousHighlight();
        }
    }

    /** Create the menus that are used by this frame.
     *  It is essential that _createGraphPane() be called before this.
     */
    protected void _addMenus() {
        super._addMenus();

        PreviousAction previousAction = new PreviousAction();
        NextAction nextAction = new NextAction();

        _viewMenu.addSeparator();
        _previousItem = GUIUtilities.addMenuItem(_viewMenu, previousAction);
        _nextItem = GUIUtilities.addMenuItem(_viewMenu, nextAction);

        _previousButton =
            GUIUtilities.addToolBarButton(_toolbar, previousAction);
        _nextButton = GUIUtilities.addToolBarButton(_toolbar, nextAction);

        _enableOrDisable();
    }

    protected ActorEditorGraphController _createController() {
        return new ActorEditorGraphController() {
            public void rerender() {
                super.rerender();

                // Repaint the graph panner after the decorators are rendered.
                _asynchronousHighlight();
            }
        };
    }

    protected static void _setTableauFactory(Object originator,
            CompositeEntity entity) {
        String momlTxt =
            "<property name=\"_tableauFactory\"" +
            " class=\"ptolemy.vergil.gt.MatchResultTableau$Factory\">" +
            "</property>";
        MoMLChangeRequest request =
            new MoMLChangeRequest(originator, entity, momlTxt);
        entity.requestChange(request);
        for (Object subentity : entity.entityList(CompositeEntity.class)) {
            _setTableauFactory(originator, (CompositeEntity) subentity);
        }
    }

    protected void _windowClosed() {
        if (_topFrame != null) {
            synchronized (_topFrame) {
                _topFrame._subviewers.remove(this);
            }
        }
    }

    private void _asynchronousHighlight() {
        // Repaint the graph panner after the decorators are rendered.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                highlightMatchedObjects();
                if (_graphPanner != null) {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            _graphPanner.repaint();
                        }
                    });
                }
            }
        });
    }

    private void _checkContainingViewer() {
        NamedObj toplevel = getModel().toplevel();
        for (Frame frame : getFrames()) {
            if (frame != this && frame instanceof MatchResultViewer) {
                MatchResultViewer viewer = (MatchResultViewer) frame;
                if (viewer.getModel() == toplevel) {
                    synchronized (viewer) {
                        _results = viewer._results;
                        _currentPosition = viewer._currentPosition;
                        viewer._subviewers.add(this);
                        _topFrame = viewer;
                    }
                    break;
                }
            }
        }
        if (_topFrame == null) {
            _subviewers = new HashSet<MatchResultViewer>();
        }
    }

    private void _enableOrDisable() {
        if (_previousItem != null && _results != null) {
            _previousItem.setEnabled(_currentPosition > 0);
        }
        if (_previousButton != null && _results != null) {
            _previousButton.setEnabled(_currentPosition > 0);
        }
        if (_nextItem != null && _results != null) {
            _nextItem.setEnabled(_currentPosition < _results.size() - 1);
        }
        if (_nextButton != null && _results != null) {
            _nextButton.setEnabled(_currentPosition < _results.size() - 1);
        }
    }

    private void _next() {
        if (_currentPosition < _results.size() - 1) {
            _currentPosition++;
            _asynchronousHighlight();
            if (_topFrame == null) {
                for (MatchResultViewer viewer : _subviewers) {
                    viewer._next();
                }
            }
            _enableOrDisable();
        }
    }

    private void _previous() {
        if (_currentPosition > 0) {
            _currentPosition--;
            _asynchronousHighlight();
            if (_topFrame == null) {
                for (MatchResultViewer viewer : _subviewers) {
                    viewer._previous();
                }
            }
            _enableOrDisable();
        }
    }

    private int _currentPosition;

    private AnimationRenderer _decorator =
        new AnimationRenderer(new Color(255, 64, 64));

    private JButton _nextButton;

    private JMenuItem _nextItem;

    private JButton _previousButton;

    private JMenuItem _previousItem;

    private List<MatchResult> _results;

    private Set<MatchResultViewer> _subviewers;

    /** The top frame that shows the toplevel model, or <tt>null</tt> if the top
     *  frame is this frame itself.
     */
    private MatchResultViewer _topFrame;

    private static final long serialVersionUID = 2459501522934657116L;

    private class NextAction extends FigureAction {

        public NextAction() {
            super("Next");

            GUIUtilities.addIcons(this, new String[][] {
                    { "/ptolemy/vergil/gt/img/next.gif",
                            GUIUtilities.LARGE_ICON },
                    { "/ptolemy/vergil/gt/img/next_o.gif",
                            GUIUtilities.ROLLOVER_ICON },
                    { "/ptolemy/vergil/gt/img/next_ov.gif",
                            GUIUtilities.ROLLOVER_SELECTED_ICON },
                    { "/ptolemy/vergil/gt/img/next_on.gif",
                            GUIUtilities.SELECTED_ICON } });
        }

        public void actionPerformed(ActionEvent e) {
            super.actionPerformed(e);

            if (_topFrame != null) {
                _topFrame._next();
            } else {
                _next();
            }
        }

        private static final long serialVersionUID = -4164485619951340333L;
    }

    private class PreviousAction extends FigureAction {

        public PreviousAction() {
            super("Previous");

            GUIUtilities.addIcons(this, new String[][] {
                    { "/ptolemy/vergil/gt/img/previous.gif",
                            GUIUtilities.LARGE_ICON },
                    { "/ptolemy/vergil/gt/img/previous_o.gif",
                            GUIUtilities.ROLLOVER_ICON },
                    { "/ptolemy/vergil/gt/img/previous_ov.gif",
                            GUIUtilities.ROLLOVER_SELECTED_ICON },
                    { "/ptolemy/vergil/gt/img/previous_on.gif",
                            GUIUtilities.SELECTED_ICON } });
        }

        public void actionPerformed(ActionEvent e) {
            super.actionPerformed(e);

            if (_topFrame != null) {
                _topFrame._previous();
            } else {
                _previous();
            }
        }

        private static final long serialVersionUID = 6583982647474946917L;
    }

}
