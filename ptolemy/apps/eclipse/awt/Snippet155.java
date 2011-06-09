package ptolemy.apps.eclipse.awt;

// From http://www.java2s.com/Code/Java/SWT-JFace-Eclipse/DrawanXusingAWTGraphics.htm
/*
 * example snippet: draw an X using AWT Graphics
 *
 * For a list of all SWT example snippets see
 * http://dev.eclipse.org/viewcvs/index.cgi/%7Echeckout%7E/platform-swt-home/dev.html#snippets
 */
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class Snippet155 {

    public static void main(String[] args) {
        final Display display = new Display();
        final Shell shell = new Shell(display);
        shell.setLayout(new FillLayout());
        Composite composite = new Composite(shell, SWT.EMBEDDED);

        /* Draw an X using AWT */
        Frame frame = SWT_AWT.new_Frame(composite);
        Canvas canvas = new Canvas() {
            public void paint(Graphics g) {
                Dimension d = getSize();
                g.drawLine(0, 0, d.width, d.height);
                g.drawLine(d.width, 0, 0, d.height);
            }
        };
        frame.add(canvas);

        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        display.dispose();
    }
}
