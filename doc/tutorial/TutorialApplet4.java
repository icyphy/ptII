package doc.tutorial;
import ptolemy.domains.de.gui.DEApplet;
import ptolemy.actor.lib.Clock;
import ptolemy.actor.lib.gui.TimedPlotter;
import java.awt.Panel;
import java.awt.Dimension;

public class TutorialApplet extends DEApplet {
    public void init() {
        super.init();
        try {
            Clock clock = new Clock(_toplevel,"clock");
            TimedPlotter plotter = new TimedPlotter(_toplevel,"plotter");
            plotter.setPanel(this);
            Dimension size = getSize();
            plotter.plot.setSize(size.width, size.height - 50);
            _toplevel.connect(clock.output, plotter.input);
            add(_createRunControls(2));
        } catch (Exception ex) {
            report("Error constructing model.", ex);
        }
    }
}
