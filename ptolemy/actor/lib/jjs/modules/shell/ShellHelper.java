

package ptolemy.actor.lib.jjs.modules.shell;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.ProcessBuilder.Redirect;

import jdk.nashorn.api.scripting.ScriptObjectMirror;

///////////////////////////////////////////////////////////////////
////ShellHelper

/**
 A helper class for the shell accessor module. It provides functionality
 to invoke a command and control <i>stdin</i> and <i>stdout</i>. It forks 
 off a process that executes the specified command. A reader thread listens 
 to outputs asynchronously and forward out puts via the EventEmitter subsystem 
 to the <i>shell.js</i> module in Nashorn. 
 
 @author Armin Wasicek
 @since Ptolemy II 10.0
 */
public class ShellHelper  {

    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////

    /** Factory method to create a new shell.
     *  @param currentObj The JavaScript instance invoking the shell.
     *  @param String The commandto be executed.
     */
    public static ShellHelper createShell(ScriptObjectMirror currentObj, String cmd) {
        return new ShellHelper(currentObj, cmd);
    }
    
    /** Write to the process' stdin.
     *  @param s The data to be sent to the process.
     */
    public synchronized void write(String s) throws IOException  {
        if(out!=null && p.isAlive())  {
            out.write(s);
            out.newLine();  
            out.flush();
        }
    }   
    
    /** Starts the process and the reader thread. Call 
     *   this after initialization and all callbacks have
     *   been installed. 
     */
    public void start()  {
        startProcess();
        startReader();
    }
    
    /** Kills the process and the reader thread. */
    public void wrapup() throws IOException {
        in.close();
        out.close(); 
        if(th.isAlive())  {
            _reader_th_running=false;
        }
        _reader_th_running=false;
        try {
            th.join(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        th=null;
  
        if(p.isAlive())  {
            p.destroy();
        }
        p=null;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                     private constructors                  ////
    
    /** Private constructor for ShellHelper for calling a shell command
     *  @param currentObj The JavaScript instance of the script that this helps.
     *  @param cmd The command to be called.
     */
    private ShellHelper(ScriptObjectMirror currentObj, String cmd)  {
        this._currentObj = currentObj;
        this.cmd=cmd;
    }
    
    
    ///////////////////////////////////////////////////////////////////
    ////                     private methods                       ////   

    /** Builds and starts the command in a process.*/
    private void startProcess()  {
        pb = new ProcessBuilder(cmd);
        pb.redirectErrorStream(true);
        pb.redirectOutput(Redirect.PIPE);
        pb.redirectInput(Redirect.PIPE);
        try {
            p = pb.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        out = new BufferedWriter( new OutputStreamWriter(p.getOutputStream()) );
        in = new BufferedReader(new InputStreamReader(p.getInputStream()));
    }
    
    /** Starts the reader thread to read from the process' stdout asynchronously.*/
    private void startReader()  {
        th = new Thread( new Runnable() {
                @Override
                public void run() {
                    do {
                        String l = null;
                        try {
                            l = in.readLine();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if(l!=null)  {
                            _currentObj.callMember("emit", "message", l);    
                        }
                        else  {
                            _reader_th_running = false;
                        }
                    }
                    while(_reader_th_running);                        
                }
            });
        th.start();
    }
    
    

    ///////////////////////////////////////////////////////////////////
    ////                     private fields                        ////
        
    /** The current instance of the JavaScript module. */
    private ScriptObjectMirror _currentObj;
    
    /** The variable used to build the process. */
    private ProcessBuilder pb;
    
    /** The reader to connect to the process' stdout.*/
    private BufferedReader in;
    
    /** The reader to connect to the process' stdin.*/
    private BufferedWriter out;
    
    /** The instance of the invoked command.*/
    private Process p;
    
    /** A thread to read asynchronously from the process' stdout. */
    private Thread th;
    
    /** Controls the reader thread. */
    private boolean _reader_th_running = true;
    
    /** A copy of the command that's invoked. */
    private String cmd;
}