

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
     *  @param String The command to be executed.
     */
    public static ShellHelper createShell(ScriptObjectMirror currentObj, String command) {
        return new ShellHelper(currentObj, command);
    }
    
    /** Write to the process' stdin.
     *  @param s The data to be sent to the process.
     */
    public synchronized void write(String s) throws IOException  {
        if(out!=null && process.isAlive())  {
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
        if(startProcess())  {
            startReader();
        }
    }
    
    /** Kills the process and the reader thread. */
    public void wrapup() throws IOException {
        if(in!=null)  {
            in.close();
        }
        if(out!=null) {
            out.close();
        }
        if((readerThread!=null) && (readerThread.isAlive()))  {
            _readerThreadRunning=false;
        }
        _readerThreadRunning=false;
        try {
            if(readerThread!=null)  {
                readerThread.join(10);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        readerThread=null;
  
        if((process!=null) && (process.isAlive()))  {
            process.destroy();
        }
        process=null;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                     private constructors                  ////
    
    /** Private constructor for ShellHelper for calling a shell command
     *  @param currentObj The JavaScript instance of the script that this helps.
     *  @param cmd The command to be called.
     */
    private ShellHelper(ScriptObjectMirror currentObj, String command)  {
        this._currentObj = currentObj;
        this.command=command;
    }
    
    
    ///////////////////////////////////////////////////////////////////
    ////                     private methods                       ////   

    /** Builds and starts the command in a process.
     *  @return Returns true, if the process has started and the pipes 
     *  are initialized. 
     */
    private boolean startProcess()  {
        processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);
        processBuilder.redirectOutput(Redirect.PIPE);
        processBuilder.redirectInput(Redirect.PIPE);
        try {
            process = processBuilder.start();
        } catch (IOException e) {
            //this._currentObj.eval("exports.error('"+e.getMessage()+"');");
            e.printStackTrace();
            return false;
        }
        out = new BufferedWriter( new OutputStreamWriter(process.getOutputStream()) );
        in = new BufferedReader(new InputStreamReader(process.getInputStream()));
        return true;
    }
    
    /** Starts the reader thread to read from the process' stdout asynchronously.*/
    private void startReader()  {
        readerThread = new Thread( new Runnable() {
                @Override
                public void run() {
                    do {
                        String line = null;
                        try {
                            line = in.readLine();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if(line!=null)  {
                            _currentObj.callMember("emit", "message", line);    
                        }
                        else  {
                            _readerThreadRunning = false;
                        }
                    }
                    while(_readerThreadRunning);                        
                }
            });
        readerThread.start();
    }
    
    

    ///////////////////////////////////////////////////////////////////
    ////                     private fields                        ////
        
    /** The current instance of the JavaScript module. */
    private ScriptObjectMirror _currentObj;
    
    /** The variable used to build the process. */
    private ProcessBuilder processBuilder;
    
    /** The reader to connect to the process' stdout.*/
    private BufferedReader in;
    
    /** The reader to connect to the process' stdin.*/
    private BufferedWriter out;
    
    /** The instance of the invoked command.*/
    private Process process;
    
    /** A thread to read asynchronously from the process' stdout. */
    private Thread readerThread;
    
    /** Controls the reader thread. */
    private boolean _readerThreadRunning = true;
    
    /** A copy of the command that's invoked. */
    private String command;
}