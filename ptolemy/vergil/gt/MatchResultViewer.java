package ptolemy.vergil.gt;

import java.awt.Color;
import java.awt.Frame;
import java.util.Set;

import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import ptolemy.actor.AtomicActor;
import ptolemy.actor.gt.SingleRuleTransformer;
import ptolemy.actor.gt.data.MatchResult;
import ptolemy.actor.gui.Tableau;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.LibraryAttribute;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.vergil.actor.ActorEditorGraphController;
import ptolemy.vergil.kernel.AnimationRenderer;
import diva.canvas.Figure;

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

        _checkContainingViewer();
        highlightMatchedObjects();
    }

    public void highlightMatchedObject(NamedObj object) {
        ActorEditorGraphController controller;
        JTabbedPane tabbedPane = _getTabbedPane();
        if (tabbedPane == null) {
            controller = (ActorEditorGraphController) _getGraphController();
        } else {
            int index = tabbedPane.getSelectedIndex();
            controller = (ActorEditorGraphController)
                    _getGraphs().get(index).getGraphPane().getGraphController();
        }
        Object location = object.getAttribute("_location");
        Figure figure = controller.getFigure(location);
        _decorator.renderSelected(figure);
    }

    public void highlightMatchedObjects() {
        if (_result != null) {
            CompositeEntity model = (CompositeEntity) getModel();
            if (model instanceof SingleRuleTransformer) {
                int index = _getTabbedPane().getSelectedIndex();
                if (index == 0) {
                    model = ((SingleRuleTransformer) model).getLeftHandSide();
                } else {
                    model = ((SingleRuleTransformer) model).getRightHandSide();
                }
            }
            Set<?> matchedHostObjects = _result.values();
            for (Object child : model.entityList(AtomicActor.class)) {
                if (matchedHostObjects.contains(child)) {
                    highlightMatchedObject((NamedObj) child);
                }
            }
        }
    }

    public void setMatchResult(MatchResult result) {
        _result = result;
        highlightMatchedObjects();
    }

    protected ActorEditorGraphController _createController() {
        return new ActorEditorGraphController() {
            public void rerender() {
                super.rerender();

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

    private void _checkContainingViewer() {
        NamedObj toplevel = getModel().toplevel();
        for (Frame frame : getFrames()) {
            if (frame != this && frame instanceof MatchResultViewer) {
                MatchResultViewer viewer = (MatchResultViewer) frame;
                if (viewer.getModel() == toplevel) {
                    _result = viewer._result;
                }
            }
        }
    }

    private AnimationRenderer _decorator =
        new AnimationRenderer(new Color(255, 64, 64));

    private MatchResult _result;

    private static final long serialVersionUID = 2459501522934657116L;

}
