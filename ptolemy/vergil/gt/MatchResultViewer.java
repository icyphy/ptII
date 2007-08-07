package ptolemy.vergil.gt;

import java.awt.Color;
import java.awt.Frame;
import java.util.Set;

import javax.swing.SwingUtilities;

import ptolemy.actor.AtomicActor;
import ptolemy.actor.gt.data.MatchResult;
import ptolemy.actor.gui.Tableau;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.LibraryAttribute;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.vergil.actor.ActorEditorGraphController;
import ptolemy.vergil.actor.ActorGraphModel;
import ptolemy.vergil.basic.ExtendedGraphFrame;
import ptolemy.vergil.kernel.AnimationRenderer;
import diva.canvas.Figure;
import diva.graph.GraphPane;

public class MatchResultViewer extends ExtendedGraphFrame {

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
        Object location = object.getAttribute("_location");
        Figure figure = _controller.getFigure(location);
        _decorator.renderSelected(figure);
    }

    public void highlightMatchedObjects() {
        if (_result != null) {
            CompositeEntity model = (CompositeEntity) getModel();
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

    protected GraphPane _createGraphPane(NamedObj entity) {
        _controller = new ActorEditorGraphController() {
            public void rerender() {
                super.rerender();
                highlightMatchedObjects();

                // Repaint the graph panner after the decorators are rendered.
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        if (_graphPanner != null) {
                            _graphPanner.repaint();
                        }
                    }
                });
            }
        };
        _controller.setConfiguration(getConfiguration());
        _controller.setFrame(this);

        // The cast is safe because the constructor only accepts
        // CompositeEntity.
        final ActorGraphModel graphModel = new ActorGraphModel(entity);
        return new GraphPane(_controller, graphModel);
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

    /** The graph controller.  This is created in _createGraphPane(). */
    protected ActorEditorGraphController _controller;

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
