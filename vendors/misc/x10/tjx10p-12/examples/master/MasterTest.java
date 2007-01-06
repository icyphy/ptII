import x10.*;
import x10.awt.*;
import x10.net.*;
import java.awt.event.*;
import java.awt.Frame;
import java.net.URL;
import java.io.File;
import java.awt.Panel;
import java.awt.Label;

public class MasterTest extends Frame implements WindowListener //UnitListener
{
    private Controller controller;
    
    public MasterTest(String host) throws Exception
    {
        controller = new SocketController(host, 2400);
        addWindowListener(this);
        add(new MasterUnitPanel(controller, "E1"));
        //add(new ApplianceUnitPanel(controller, "E1"));
        //add(new LightUnitPanel(controller, "E2"));
        pack();
        setVisible(true);
    }
    
    public void windowOpened(WindowEvent event)
    {
    }
    
    public void windowClosed(WindowEvent event)
    {
    }
    
    public void windowActivated(WindowEvent event)
    {
    }
    
    public void windowDeactivated(WindowEvent event)
    {
    }
    
    public void windowIconified(WindowEvent event)
    {
    }
    
    public void windowDeiconified(WindowEvent event)
    {
    }
    
    public void windowClosing(WindowEvent event)
    {
        System.exit(0);
    }
    
    public static void main(String[] args) throws Exception
    {
        MasterTest x10App = new MasterTest(args[0]);
    }
}