package doc.tutorial;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.gui.PtolemyApplet;
import ptolemy.actor.lib.Clock;
import ptolemy.actor.lib.gui.TimedPlotter;
import ptolemy.domains.de.kernel.DEDirector;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

public class PtolemyAppletModel extends TypedCompositeActor {
    public PtolemyAppletModel(Workspace workspace)
	throws IllegalActionException, NameDuplicationException {
	super(workspace);

	// Create the director
	DEDirector director = new DEDirector(this, "director");
	setDirector(director);
	//director.stopTime.setExpression("10");

	// Create two actors.
	Clock clock = new Clock(this,"clock");
	TimedPlotter plotter = new TimedPlotter(this,"plotter");

	// Connect them.
	connect(clock.output, plotter.input);
    }
}

