package doc.tutorial;
import ptolemy.domains.de.gui.DEApplet;
import ptolemy.actor.lib.Clock;
import ptolemy.actor.lib.gui.TimedPlotter;

public class TutorialApplet extends DEApplet {
    public void init() {
        super.init();
        try {
            Clock clock = new Clock(_toplevel,"clock");
            TimedPlotter plotter = new TimedPlotter(_toplevel,"plotter");
            plotter.setPanel(this);
            plotter.plot.setSize(700,300);
            _toplevel.connect(clock.output, plotter.input);
        } catch (Exception ex) {
            report("Error constructing model.", ex);
        }
    }
}
