package tutorial;
import ptolemy.domains.de.gui.DEApplet;
import ptolemy.actor.lib.Clock;
import ptolemy.actor.lib.gui.TimedPlotter;

public class TutorialApplet extends DEApplet {
    public void init() {
        super.init();
        try {
            Clock clock = new Clock(_toplevel,"clock");
            TimedPlotter plot = new TimedPlotter(_toplevel,"plot");
            _toplevel.connect(clock.output, plot.input);
        } catch (Exception ex) {}
    }
}
