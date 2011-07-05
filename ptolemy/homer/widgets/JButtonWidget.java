package ptolemy.homer.widgets;

import java.awt.Rectangle;

import javax.swing.JButton;

import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;

public class JButtonWidget extends Widget {

    private final JButton button = new JButton();

    public JButtonWidget(Scene scene) {
        super(scene);
    }

    public JButton getButton() {
        return button;
    }

    public void setLabel(String label) {
        button.setText(label);
        button.repaint();

    }

    public String getLabel() {
        return button.getText();
    }

    protected Rectangle calculateClientArea() {
        return new Rectangle(button.getPreferredSize());
    }

    protected void paintWidget() {
        button.setSize(getBounds().getSize());
        button.paint(getGraphics());
    }
}
