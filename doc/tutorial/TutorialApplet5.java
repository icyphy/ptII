package doc.tutorial;

import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.gui.PtolemyApplet;
import ptolemy.actor.lib.Clock;
import ptolemy.actor.lib.gui.TimedPlotter;
import ptolemy.domains.de.kernel.DEDirector;
import ptolemy.gui.Query;
import ptolemy.gui.QueryListener;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

import java.awt.BorderLayout;

public class TutorialApplet5 extends PtolemyApplet implements QueryListener {
    public NamedObj _createModel(Workspace workspace) 
	throws Exception {
        TypedCompositeActor toplevel = new TypedCompositeActor(workspace);
        _toplevel = toplevel;

	// Create the director.
	DEDirector director = new DEDirector(toplevel, "director");
	director.stopTime.setExpression("30.0");

	// Create two actors.
	_clock = new Clock(toplevel,"clock");
	TimedPlotter plotter = new TimedPlotter(toplevel,"plotter");

	// Configure the plotter.
	plotter.place(getContentPane());
	plotter.plot.setBackground(getBackground());
	plotter.plot.setTitle("clock signal");
        plotter.plot.setXLabel("time");
        plotter.plot.setImpulses(true);
        plotter.plot.setConnected(false);
        plotter.plot.setMarksStyle("dots");

	// Connect them.
	toplevel.connect(_clock.output, plotter.input);
	return toplevel;
    }

    protected void _createView() {
	super._createView();
	_query.setBackground(getBackground());
	_query.addLine("period", "Period", "2.0");
	_query.addQueryListener(this);
	getContentPane().add( _query, BorderLayout.SOUTH );
    }

    public void changed(String name) {
        _clock.period.setExpression(_query.stringValue("period"));
        try {
            _go();
        } catch (Exception ex) {
            report("Error executing model.", ex);
        }
    }

    private Clock _clock;
    private Query _query = new Query();

}
