package doc.tutorial;
import ptolemy.domains.de.gui.DEApplet;
import ptolemy.actor.lib.Clock;
import ptolemy.actor.lib.gui.TimedPlotter;
import ptolemy.gui.Query;
import ptolemy.gui.QueryListener;
import java.awt.Panel;
import java.awt.Dimension;

public class TutorialApplet extends DEApplet implements QueryListener {
    private Query _query = new Query();
    private Clock _clock;
    public void init() {
        super.init();
        try {
            _clock = new Clock(_toplevel,"clock");
            TimedPlotter plotter = new TimedPlotter(_toplevel,"plotter");
            plotter.place(getContentPane());
            plotter.plot.setTitle("clock signal");
            plotter.plot.setXLabel("time");
            plotter.plot.setImpulses(true);
            plotter.plot.setConnected(false);
            plotter.plot.setMarksStyle("dots");
            Dimension size = getSize();
            plotter.plot.setSize(700, 250);
            _toplevel.connect(_clock.output, plotter.input);
            add(_createRunControls(2));
            _query.setBackground(getBackground());
            _query.addLine("period", "Period", "2.0");
            _query.addQueryListener(this);
            add(_query);
        } catch (Exception ex) {
            report("Error constructing model.", ex);
        }
    }
    public void changed(String name) {
        _clock.period.setExpression(_query.stringValue("period"));
        try {
            _go();
        } catch (Exception ex) {
            report("Error executing model.", ex);
        }
    }
}
