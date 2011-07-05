package ptolemy.homer;

import java.awt.Color;
import java.awt.Point;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.geom.Point2D;
import java.util.Iterator;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.border.TitledBorder;

import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Scene;

import ptolemy.homer.widgets.JButtonWidget;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.MessageHandler;
import ptolemy.vergil.toolbox.PtolemyTransferable;
import ptolemy.vergil.toolbox.SnapConstraint;

public class TabbedLayoutScene extends JTabbedPane {

    Scene scene;
    LayerWidget mainLayer;
    WidgetAction moveAction;

    public TabbedLayoutScene(int placement) {
        super(placement);

        setBorder(new TitledBorder(null, "User Interface Layout",
                TitledBorder.LEADING, TitledBorder.TOP, null, null));

        scene = new Scene();
        scene.getActions().addAction(ActionFactory.createZoomAction());

        mainLayer = new LayerWidget(scene);
        mainLayer.setBackground(Color.black);
        scene.addChild(mainLayer);
        LayerWidget interractionLayer = new LayerWidget(scene);
        scene.addChild(interractionLayer);
        interractionLayer.setBackground(Color.black);
        moveAction = ActionFactory.createAlignWithMoveAction(mainLayer,
                interractionLayer, null, false);

        JButtonWidget button = new JButtonWidget(scene);
        button.getButton().setText("My Button1");
        button.getActions().addAction(moveAction);
        mainLayer.addChild(button);
        JComponent sceneView = scene.createView();

        SceneDropTargetListener listener = new SceneDropTargetListener();
        new DropTarget(sceneView, listener);

        JScrollPane shapePane = new JScrollPane();
        shapePane.setBackground(Color.black);
        shapePane.setViewportView(sceneView);

        addTab("Home tab", null, shapePane, null);

        JPanel pnlTab = new JPanel();
        addTab("New tab", null, pnlTab, null);

    }

    class SceneDropTargetListener implements DropTargetListener {

        @Override
        public void dragEnter(DropTargetDragEvent dtde) {
            // TODO Auto-generated method stub

        }

        @Override
        public void dragOver(DropTargetDragEvent dtde) {
            // TODO Auto-generated method stub

        }

        @Override
        public void dropActionChanged(DropTargetDragEvent dtde) {
            // TODO Auto-generated method stub

        }

        @Override
        public void dragExit(DropTargetEvent dte) {
            // TODO Auto-generated method stub

        }

        @Override
        public void drop(DropTargetDropEvent dropEvent) {

            java.util.List dropObjects = null;
            Iterator iterator = null;
            if (dropEvent
                    .isDataFlavorSupported(PtolemyTransferable.namedObjFlavor)) {
                try {
                    dropEvent.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                    dropObjects = (java.util.List) dropEvent
                            .getTransferable()
                            .getTransferData(PtolemyTransferable.namedObjFlavor);

                    NamedObj dropObj = (NamedObj) dropObjects.get(0);
                    String name = dropObj.getName();
                    Point2D originalPoint = SnapConstraint
                            .constrainPoint(dropEvent.getLocation());

                    JButtonWidget button = new JButtonWidget(scene);
                    button.getButton().setText("Wigdet for Actor : " + name);
                    button.getActions().addAction(moveAction);
                    Point pointLocation = new Point();
                    pointLocation.setLocation(originalPoint);
                    button.setPreferredLocation(pointLocation);
                    mainLayer.addChild(button);
                    mainLayer.repaint();

                    iterator = dropObjects.iterator();
                } catch (Exception e) {
                    MessageHandler.error(
                            "Can't find a supported data flavor for drop in "
                                    + dropEvent, e);
                    return;
                }
            } else {
                dropEvent.rejectDrop();
            }

        }

    }

}
