package doc.tutorial;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.lib.Clock;
import ptolemy.actor.lib.gui.TimedPlotter;
import ptolemy.data.expr.Parameter;
import ptolemy.domains.de.kernel.DEDirector;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

public class TutorialApplet3 extends TypedCompositeActor {
    public TutorialApplet3(Workspace workspace)
            throws IllegalActionException, NameDuplicationException {
        super(workspace);

        // Create model parameters
        Parameter stopTime = new Parameter(this, "stopTime");
        Parameter clockPeriod = new Parameter(this, "clockPeriod");

        // Give the model parameters default values.
        stopTime.setExpression("10.0");
        clockPeriod.setExpression("2.0");

        // Create the director.
        DEDirector director = new DEDirector(this, "director");
        setDirector(director);

        // Create two actors.
        Clock clock = new Clock(this,"clock");
        TimedPlotter plotter = new TimedPlotter(this,"plotter");

        // Set the user controlled parameters.
        director.stopTime.setExpression("stopTime");
        clock.period.setExpression("clockPeriod");

        // Connect them.
        connect(clock.output, plotter.input);
    }
}
