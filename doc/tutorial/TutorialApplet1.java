package doc.tutorial;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.gui.PtolemyApplet;
import ptolemy.actor.lib.Clock;
import ptolemy.actor.lib.gui.TimedPlotter;
import ptolemy.domains.de.kernel.DEDirector;

public class TutorialApplet extends PtolemyApplet {
    public void init() {
        super.init();
        TypedCompositeActor toplevel = new TypedCompositeActor(_workspace);
        _toplevel = toplevel;
        try {
            new DEDirector(toplevel, "DEDirector");
            Clock clock = new Clock(toplevel,"clock");
            TimedPlotter plotter = new TimedPlotter(toplevel,"plotter");
            toplevel.connect(clock.output, plotter.input);
        } catch (Exception exception) {
            System.out.println(exception);
        }
    }
}
