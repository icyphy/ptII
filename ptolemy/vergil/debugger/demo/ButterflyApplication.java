package ptolemy.vergil.debugger.demo;

import java.util.*;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.actor.*;
import ptolemy.actor.gui.*;
import ptolemy.actor.util.*;
import ptolemy.actor.lib.*;
import ptolemy.actor.lib.conversions.*;
import ptolemy.domains.sdf.lib.*;
import ptolemy.plot.*;

import ptolemy.vergil.debugger.*;
import ptolemy.vergil.debugger.domains.sdf.*;


//////////////////////////////////////////////////////////////////////////
//// ButterflyApplication
/**
 Standalone Butterfly Application, useful for profiling

@author Christopher Hylands
@version : ptmkmodel,v 1.7 1999/07/16 01:17:49 cxh Exp ButterflyApplet.java.java,v 1.1 1999/05/06 20:14:28 cxh Exp $
*/
public class ButterflyApplication extends JFrame {

    public Butterfly butterfly;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create a Butterfly Application
     */
    public ButterflyApplication()
            throws IllegalActionException, NameDuplicationException {
	super("Butterfly");

        setSize(400, 400);

        // Handle window closing by exiting the application.
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
	Pdb pdb = new Pdb();
        Workspace w = new Workspace("w");
        TypedCompositeActor toplevel = new TypedCompositeActor(w);
        toplevel.setName("toplevel");
        SDFDbgDirector director = new SDFDbgDirector(toplevel, "director");
	director.setPdb(pdb);
        Manager manager = new Manager(w, "manager");
        toplevel.setManager(manager);
	
        try {
	    butterfly = new Butterfly(toplevel, getContentPane(),
                    getContentPane().getSize());
	    (director.breakpoints).addBreakpoint(butterfly.ramp, "prefire"); 
	    ActorWatcher watcher = new ActorWatcher(butterfly.polarToRect1);
	    NamedList att = new NamedList();
	    Enumeration e = butterfly.polarToRect1.getAttributes();
	    while (e.hasMoreElements()) {
		att.append((Nameable)e.nextElement());
	    }
	    watcher.edit(att);
	    pdb.getDbgController().actorWatcher.append(watcher);
	    director.iterations.setToken(new IntToken(12));   //Default : 1200
	    manager.run();
        } catch (Exception ex) {
            System.err.println("Error constructing model." + ex);
        }

        // Map to the screen.
	show();
    }

    /** Create a new window with the Butterfly plot in it and map it
	to the screen.
    */
    public static void main(String arg[])
            throws IllegalActionException , NameDuplicationException {

        ButterflyApplication butterflyApplication = new ButterflyApplication();
    }
}
