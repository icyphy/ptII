// From http://eclipsewiki.editme.com/AWTBridgeExample

package ptolemy.apps.eclipse.awt;

import java.awt.Color;
import java.awt.Frame;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

//SWT_AWT Bridge example.
//
// Omry Yadan.
//   Updated at 17/8/2005
public class AWTBridge {
    public static void main(String[] args) {

        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("Main");
        shell.setBounds(0, 0, 200, 250);

        Composite swt = new Composite(shell, SWT.NONE);
        Label swtLabel = new Label(swt, SWT.NONE);
        swtLabel.setText("swt label");
        swtLabel.setBounds(10, 10, 70, 20);
        swt.setBounds(0, 0, 200, 250);
        swt.setBackground(display.getSystemColor(SWT.COLOR_BLUE));

        Composite SWT_AWT_container = new Composite(swt, SWT.EMBEDDED);
        SWT_AWT_container.setBounds(0, 50, 200, 150);

        Frame awt = SWT_AWT.new_Frame(SWT_AWT_container);
        awt.setBackground(Color.red);
        awt.setBounds(0, 0, 200, 150);

        java.awt.Label awtLabel = new java.awt.Label("AWT Label");
        awtLabel.setBounds(0, 0, 70, 20);
        awt.add(awtLabel);
        shell.open();

        while (!shell.isDisposed()) {

            if (!display.readAndDispatch()) {

                display.sleep();

            }

        }

        display.dispose();

    }
}
