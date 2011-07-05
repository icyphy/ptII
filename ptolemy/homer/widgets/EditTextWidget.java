package ptolemy.homer.widgets;

import java.awt.Rectangle;

import javax.swing.JTextField;

import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.TextFieldInplaceEditor;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;

///////////////////////////////////////////////////////////////////
////EditTextWidget

/**
* TODO
* @author Ishwinder Singh
* @version $Id:$ 
* @since Ptolemy II 8.1
* @Pt.ProposedRating Red (ishwinde)
* @Pt.AcceptedRating Red (ishwinde)
*/
public class EditTextWidget extends Widget {

    private final JTextField editText = new JTextField();
    WidgetAction moveAction;
    WidgetAction resizeAction;
    WidgetAction renameAction;

    public EditTextWidget(Scene scene, LayerWidget mainLayer,
            LayerWidget interractionLayer) {
        super(scene);
        moveAction = ActionFactory.createAlignWithMoveAction(mainLayer,
                interractionLayer, null, false);
        resizeAction = ActionFactory.createAlignWithResizeAction(mainLayer,
                interractionLayer, null, false);
        renameAction = ActionFactory
                .createInplaceEditorAction(new RenameEditor());
        getActions().addAction(resizeAction);
        getActions().addAction(moveAction);
    }

    public JTextField getButton() {
        return editText;
    }

    protected Rectangle calculateClientArea() {
        return new Rectangle(editText.getPreferredSize());
    }

    protected void paintWidget() {
        editText.setSize(getBounds().getSize());
        editText.paint(getGraphics());
    }

    String getLabel() {
        return editText.getText();
    }

    void setLabel(String text) {
        editText.setText(text);
    }

    private static class RenameEditor implements TextFieldInplaceEditor {

        public boolean isEnabled(Widget widget) {
            return true;
        }

        public String getText(Widget widget) {
            return ((EditTextWidget) widget).getLabel();
        }

        public void setText(Widget widget, String text) {
            ((EditTextWidget) widget).setLabel(text);
        }

    }

}
