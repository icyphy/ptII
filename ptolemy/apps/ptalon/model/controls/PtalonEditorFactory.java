package ptolemy.apps.ptalon.model.controls;

import java.awt.Frame;

import ptolemy.actor.gui.EditorFactory;
import ptolemy.apps.ptalon.model.PtalonModel;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

public class PtalonEditorFactory extends EditorFactory {
    public PtalonEditorFactory(NamedObj container, String name,
            PtalonModel model) throws IllegalActionException,
            NameDuplicationException {
        super(container, name);
        _model = model;
    }

    private PtalonModel _model;

    @Override
    public void createEditor(NamedObj object, Frame parent) {
        new PtalonDialog(parent, "Configure Ptalon Model", _model);
    }

}
