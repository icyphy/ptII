package doc.tutorial;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;

import javax.swing.JTable;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.Placeable;
import ptolemy.actor.gui.Tableau;
import ptolemy.actor.gui.TableauFrame;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.Location;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.vergil.actor.ActorEditorGraphController;
import ptolemy.vergil.actor.ActorGraphModel;
import ptolemy.vergil.kernel.attributes.EllipseAttribute;
import diva.graph.GraphPane;
import diva.graph.JGraph;

public class LEDDisplay extends TypedAtomicActor implements Placeable {
    private JTable _table;
    private TableauFrame _frame;
    private Tableau _tableau;
    private CompositeEntity _entity;
    private EllipseAttribute[][] _leds;

    public LEDDisplay(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _entity = new CompositeEntity(workspace());
    }
    
    public void initialize() throws IllegalActionException {
        super.initialize();
        if (_table == null) {
            Effigy containerEffigy = Configuration.findEffigy(toplevel());
            try {
                _tableau = new Tableau(containerEffigy, "tableau");
            } catch (NameDuplicationException e) {
                throw new IllegalActionException(this, e, "Failed to create tableau.");
            }
            _frame = new TableauFrame(_tableau);
            _tableau.setFrame(_frame);
            place(_frame.getContentPane());
            _frame.pack();
        }
        if (_frame != null) {
            _frame.show();
            _frame.toFront();
        }
    }

    public void place(Container container) {
        if (container == null) {
            if (_frame != null) {
                _frame.dispose();
            }
            _frame = null;
            _table = null;
            try {
                _tableau.setContainer(null);
            } catch (Exception e) {
                throw new InternalErrorException(e);
            }
            _tableau = null;
        } else {
            ActorEditorGraphController controller = new ActorEditorGraphController();
            ActorGraphModel graphModel = new ActorGraphModel(_entity);
            GraphPane graphPane = new GraphPane(controller, graphModel);
            JGraph jgraph = new JGraph(graphPane);
            
            jgraph.setMinimumSize(new Dimension(200, 200));
            jgraph.setMaximumSize(new Dimension(200, 200));
            jgraph.setPreferredSize(new Dimension(200, 200));
            jgraph.setSize(200, 200);
            jgraph.setBackground(Color.white);

            if (_leds == null) {
                _leds = new EllipseAttribute[10][10];
                for (int i = 0; i < 10; i++) {
                    for (int j = 0; j < 10; j++) {
                        try {
                            EllipseAttribute led = new EllipseAttribute(_entity, "LED_" + i + "_" + j);
                            led.fillColor.setExpression("{1.0, 0.0, 0.0, 0.5}");
                            led.width.setExpression("10");
                            led.height.setExpression("10");
                            Location location = new Location(led, "_location");
                            double position[] = new double[2];
                            position[0] = i * 20;
                            position[1] = j * 20;
                            location.setLocation(position);
                            _leds[i][j] = led;
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }
            }
            try {
                _entity.validateSettables();
            } catch (IllegalActionException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            ChangeRequest request = new ChangeRequest(this, "dummy") {
                protected void _execute() {
                };
            };
            _entity.requestChange(request);
            
            container.add(jgraph);
        }
    }
}
