package ptolemy.homer.widgets;

import java.awt.Color;

import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Scene;

public class TextViewWidget extends LabelWidget {

    WidgetAction moveAction;
    WidgetAction resizeAction;

    public TextViewWidget(Scene scene, LayerWidget mainLayer,
            LayerWidget interractionLayer) {
        super(scene);

        setOpaque(true);
        setBackground(Color.gray);
        setCheckClipping(true);
        setAlignment(LabelWidget.Alignment.CENTER);
        setVerticalAlignment(LabelWidget.VerticalAlignment.CENTER);
        moveAction = ActionFactory.createAlignWithMoveAction(mainLayer,
                interractionLayer, null, false);
        resizeAction = ActionFactory.createAlignWithResizeAction(mainLayer,
                interractionLayer, null, false);
        getActions().addAction(resizeAction);
        getActions().addAction(moveAction);
    }

}
