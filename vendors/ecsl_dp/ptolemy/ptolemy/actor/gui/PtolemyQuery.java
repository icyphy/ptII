package ptolemy.actor.gui;
import java.awt.Color;
import ptolemy.kernel.util.Settable;
public class PtolemyQuery {
    public void addChoice(
            String name,
            String label,
            String[] values,
            String defaultChoice) {
    }
    public void addChoice(
            String name,
            String label,
            String[] values,
            String defaultChoice,
            boolean editable) {
        addChoice(name, label, values, defaultChoice, editable,
                Color.white, Color.black);
    }
    public void addChoice(
            String name,
            String label,
            String[] values,
            String defaultChoice,
            boolean editable,
            final Color background,
            final Color foreground) {
    }

    public void attachParameter(Settable attribute, String entryName) {
    }
    public static Color preferredBackgroundColor(Object object) {
        Color background = Color.white;
        return background;
    }
    public static Color preferredForegroundColor(Object object) {
        Color background = Color.black;
        return background;
    }

}
