/* The base actor of Design Flow Management Domain.

 Copyright (c) 1998-1999 The Regents of the University of California.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

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
 
                                        PT_COPYRIGHT_VERSION_2
                                        COPYRIGHTENDKEY
@ProposedRating Red (wbwu@eecs.berkeley.edu)

*/

package ptolemy.domains.dfm.kernel;

import ptolemy.domains.pn.kernel.*;
import ptolemy.domains.dfm.data.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.actor.*;
import java.util.*;

//////////////////////////////////////////////////////////////////////////
//// DFMActor
/** 
  The parent actor class provide basic function and fields for rest of DFM
  actors.  It provide storage elements for input/output data and parameters.
  Also this class will process the token's message tag, and create appropriate
  token tag on the output token. <p>

@author  William Wu (wbwu@eecs.berkeley.edu)
@version: $id$
@data: 11/28/98
*/

public abstract class DFMActor extends AtomicActor {

    /** Constructor.  
     * @param container This is the CompositeActor containing this actor
     * @param name This is the name of this actor.
     * @exception ptolemy.kernel.util.NameDuplicationException throws
     *            if there is already a same name. 
     */	
    public DFMActor(CompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
    }
 

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////


    // for demo purpose only, the drawer draws the state of the actor.
    public void addActorDrawer(DFMActorDrawer drawer){
         _drawer = drawer;
    }

    // for demo purpose only, the drawer draws the output from output ports.
    public void addPortDrawer(String portname, DFMPortDrawer drawer){
         _portDrawer.put(portname, drawer);
    }

    /** 
     * Read all the input and process the input tokens.  If all input
     * token has the tag that equals to "Previous Result Valid" then
     * the default result is   
     * @param parameter-name description
     * @param parameter-name description
     * @return description
     * @exception full-classname description
     */	
    public void fire() {

         DFMToken data = null; 
         while (true) {

if (_sourceActor){
if (!_getParamChanged()) System.out.println("paramed flag is not true");
}
              _newData = false;
              _noop = false;
              Enumeration enum = getPorts();
              if (_drawer != null) _drawer.draw(0); 

              // read and store all the inputs
              while (enum.hasMoreElements()) {

                  if (_drawer != null) _drawer.draw(1); 
System.out.println("actor: "+getName()+" reading data");
                  IOPort port = (IOPort)enum.nextElement();
                  if (port.isInput()){
                      try {
                          data = (DFMToken) port.get(0);
System.out.println("actor: "+getName()+" storing token from "+port.getName());
                          _inputTokens.put(port.getName(), data);

                          if (data.getTag().equals("New")){
                              _newData = true;
                              _dataCache.put(port.getName(), data.getData());
                          } else if (data.getTag().equals("Annotate")){
                              _annotate = true;
                              _dataCache.put(port.getName(), data.getData());
                          } else if (data.getTag().equals("No-Op")){
                              _noop = true;
                          }
                      } catch (IllegalActionException e) {}

                  } 
              }
System.out.println("actor: "+getName()+" got all data");

              if (_drawer != null) _drawer.draw(3);
       
              boolean need2run = _processInputData(); 

              if (need2run){

// need to delay some time here to show the user of the progress
Thread mythread = Thread.currentThread();
try{
     mythread.sleep(1000);     // wait for delay
} catch (InterruptedException e) {}

System.out.println("actor: "+getName()+" performing process");
                  _performProcess();
                  if (_annotate){
                      
System.out.println("actor: "+getName()+" performing annotation");
                      _performAnnotation();
                  } 
              } else{
                  if (_annotate){
System.out.println("actor: "+getName()+" performing annotation");
                      _performAnnotation();
                  } 
System.out.println("actor: "+getName()+"not performing process");
                  _notPerformProcess();
                  
              } 
  
System.out.println("actor: "+getName()+" done performing");

              // don't produce output when there is only annotate token and
              // no token is produced from _performProcess()
System.out.println("_annotate: "+_annotate+" need to run: "+need2run);

              if (!((_annotate) && (!need2run))){
System.out.println("actor: "+getName()+" producing output");
                  enum = _outputTokens.keys();
                  while (enum.hasMoreElements()){
                      String outportName = (String) enum.nextElement();
                      DFMToken outdata = (DFMToken) _outputTokens.get(outportName);
                      IOPort outport = (IOPort) getPort(outportName);
                      if (outport != null){
                          try{
                              outport.broadcast(outdata);
System.out.println("actor: "+getName()+" fire data: tag: "+ outdata.getTag()+" value: "+outdata.getData().toString());

Object tmp = _portDrawer.get(outportName);
if (tmp != null){
    DFMPortDrawer portdrawer = (DFMPortDrawer) tmp;
    Object value = outdata.getData();
    String val = new String("");
    if (value != null){ 
        val = value.toString();
    } 
    portdrawer.draw(outdata.getTag(), val);
}
 
                          } catch (IllegalActionException e) {}
                      }
                  }
              }
              _annotate = false;
System.out.println("actor: "+getName()+" done outputing");
  
              if (_sourceActor){
System.out.println("actor: "+getName()+" delaying...");
                    if (_drawer != null) _drawer.draw(2);
                  Director dir = getDirector();
                      try{
                          dir.fireAt(this, dir.getCurrentTime() + _delay);
                      } catch (IllegalActionException e) {}
           //       if (_drawer != null) _drawer.drawCurrentState(DELAYING);
System.out.println("actor: "+getName()+"done delaying...");
              }
               
         }
        
    }

    public boolean isFeedbackActor(){
         return _feedbackActor;
    }

    public boolean changeParameter(String name, Object arg){
         return true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    public final static int SLEEPING = 0;
    public final static int WAITING = 1;
    public final static int RUNNING = 2;
    public final static int DELAYING = 3;

    ////////////////////////////////////////////////////////////////////////
    ////                         protected methods                      ////

    protected void _copyToOutTags(String tag){
        Enumeration enum = getPorts();
        while (enum.hasMoreElements()){
            IOPort port = (IOPort) enum.nextElement();    
            if (!port.isInput()){
                // output port
                _outputTokenTags.put(port.getName(), new String(tag));
            }
        }
    }  

    /** 
     * @param parameter-name description
     * @param parameter-name description
     * @return description
     * @exception full-classname description
     */	
    protected boolean _processInputData() {
       String tag;
       boolean need2run;
       if (_noop){  
System.out.println("actor "+getName()+" got no op signal"); 
           // one on-op means all no op for this actor
           tag = new String("No-Op");  // copy the tag to output tags
           _copyToOutTags(tag);
           need2run = false;
       } else {
           if (_newData){              // if there is new data
System.out.println("actor "+getName()+" got new data"); 
               
               need2run = true;
               _setParamChanged(false);
           } else {
               if (_getParamChanged()) {   // no new data but param changed.
                                             // rerun the actor
System.out.println("actor "+getName()+" param changed"); 
                   need2run = true;
                   _setParamChanged(false);
               } else {
System.out.println("actor "+getName()+" previous result valid"); 
                   tag = new String("PreviousResultValid");
                   _copyToOutTags(tag);
                   need2run = false;
               }
           }
       }
       return need2run;
    }

    /**
     * This actor that does the operation defined for the specific actor.
     * The derived class should implement this class that transfer the input
     * from the input hashtable, and input tags to the results stored into
     * output data hashtable, and output tag.
     */ 
    protected abstract void _performProcess();

    // call this method during performProcess to get data for perform 
    // the actual operation.
    protected Object _getData(String name){
        DFMToken token = (DFMToken) _inputTokens.get(name);
        if ((token.getTag().equals("New"))||(token.getTag().equals("Annotate"))){
            return token.getData();
        } else if (token.getTag().equals("PreviousResultValid")){
            return _dataCache.get(name);
        } else {
            return null;
        }
    }

    // create void DFM token, and attached the outtags on them 
    protected void _notPerformProcess(){
         Enumeration enum = _outputTokenTags.keys();
         while (enum.hasMoreElements()){
             String outportname = (String) enum.nextElement();
             String tag = (String) _outputTokenTags.get(outportname);
             DFMToken token = new DFMToken(tag); 
             _outputTokens.put(outportname, token);
         }
    } 

    protected void _performAnnotation(){
         return;
    }

    protected void _setSource(){
         _sourceActor = true;
    }
 
    protected synchronized void _setParamChanged(boolean changed){
System.out.println(getName()+" change param flag is set to: "+changed);
         _param_changed = changed;
    }

    protected synchronized boolean _getParamChanged(){
System.out.println(getName()+" get param flag : "+_param_changed);
         return _param_changed;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** Storage for parameters */
    protected Hashtable _parameters = new Hashtable();

    /** Storage for all input data */
    protected Hashtable _inputTokens = new Hashtable();

    /** Storage for all output token tags */
    protected Hashtable _outputTokenTags = new Hashtable();

    /** Storage for all output token  */
    protected Hashtable _outputTokens = new Hashtable();

    protected Hashtable _savedoutputTokens = new Hashtable();

 
   /** Storage for all cached data */
    protected Hashtable _dataCache = new Hashtable();

    protected boolean _newData;
    protected boolean _noop;
    protected boolean _annotate;

   // protected DFMActorDrawer _drawer;
    protected double _delay = 1.0;

    protected Hashtable _portDrawer = new Hashtable();
    protected DFMActorDrawer _drawer;

    protected boolean _feedbackActor = false;

    ///////////////////////////////////////////////////////////////////
    ////                        private methods                    ////

    private boolean _param_changed = true;

    /** if this actor is a source actor */
    private boolean _sourceActor = false;

}
