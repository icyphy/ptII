/*
@Copyright (c) 1998 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
SUCH DAMAGE.

THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
ENHANCEMENTS, OR MODIFICATIONS.

						PT_COPYRIGHT_VERSION 2
						COPYRIGHTENDKEY
*/
package ptolemy.domains.sdf.demo;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.*;
import ptolemy.actor.*;
import ptolemy.domains.sdf.kernel.*;
import ptolemy.domains.sdf.lib.*;
import java.util.Enumeration;

import ptolemy.domains.sdf.kernel.*;

/**
 * @version $Id$
 */
public class SDFdemo {

    private SDFRamp ramp;
    private SDFDelay delay;
    private SDFPrint print;
    private Manager m = new Manager();
    private CompositeActor c= new CompositeActor();
    private SDFDirector d= new SDFDirector();
    private SDFScheduler s=new SDFScheduler();
    private IORelation r;

    public static void main(String args[])            
            throws IllegalActionException, NameDuplicationException
         {
             DebugListener debugger = new DebugListener();
    
        Debug.register(debugger);
        SDFdemo demo=new SDFdemo();
        demo.execute();
    }
    public void execute()
            throws IllegalActionException, NameDuplicationException
        {

                c.setDirector(d);
                c.setManager(m);
                d.setScheduler(s);
                d.setScheduleValid(false);

                print=new SDFPrint(c,"print");
                ramp=new SDFRamp(c,"ramp");
                delay=new SDFDelay(c,"delay");

                r=(IORelation) c.connect(ramp.outputport,delay.inputport,"R1");
                r=(IORelation) c.connect(delay.outputport,print.inputport,"R2");
                             
               
                Parameter p = (Parameter) d.getAttribute("Iterations");
                p.setToken(new IntToken(6));
                m.run();
        }
}    
 
    


