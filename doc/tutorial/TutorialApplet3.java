package doc.tutorial;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.gui.PtolemyApplet;
import ptolemy.actor.lib.Clock;
import ptolemy.actor.lib.gui.TimedPlotter;
import ptolemy.domains.de.kernel.DEDirector;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

public class TutorialApplet3 extends PtolemyApplet {
    public NamedObj _createModel(Workspace workspace) 
	throws Exception {
        TypedCompositeActor toplevel = new TypedCompositeActor(workspace);
        _toplevel = toplevel;

	// Create the director.
	DEDirector director = new DEDirector(toplevel, "director");
	String stopTime = getParameter("stopTime");
	if (stopTime == null) {
	    director.stopTime.setExpression("10.0");
	} else {
	    director.stopTime.setExpression(stopTime);
	}

	// Create two actors.
	Clock clock = new Clock(toplevel,"clock");
	TimedPlotter plotter = new TimedPlotter(toplevel,"plotter");

	// Connect them.
	toplevel.connect(clock.output, plotter.input);
	return toplevel;
    }
}
