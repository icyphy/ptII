package ptolemy.actor.gui.test;

import java.net.URL;
import java.util.Vector;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.Manager;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;
import ptolemy.moml.MoMLParser;


/** This test (from Rome AFRL) was causing a stack trace.
 */
public class RunMultipleModels {

    /** Create multiple models and execute them. */
    public synchronized void run() throws Exception {
        for(int i = 0; i < 100; i++) {
            Thread thread = new Thread(new Runnable() {
                public void run() {
                    try {
                        _activeCount++;
                        String threadName = Thread.currentThread().getName();
                        URL url = RunMultipleModels.class.getResource("RunMultipleModelsModel.xml");

                        Workspace workspace = new Workspace("Workspace"
                                + threadName);
                        MoMLParser parser = new MoMLParser(workspace);
                        parser.purgeModelRecord(url);
                        NamedObj model = parser.parse(null, url);

                        Manager manager = new Manager(workspace, "Manager"
                                + threadName);
                        ((CompositeActor)model).setManager(manager);
                        manager.execute();
                        _activeCount--;

                        if (_activeCount == 0) {
                            notifyAll();
                        }
                    } catch (IllegalMonitorStateException ex) {
                        Thread thread = Thread.currentThread();
                        System.out.println("Thread: " + thread.getName()
                                + " state: " + thread.getState());
                        ex.printStackTrace();
                    } catch (Exception ex) {
                        _lastException = ex;
                        ex.printStackTrace();
                        _activeCount--;

                        //if (_activeCount == 0) {
                        //    notifyAll();
                        //}
                    }
                }
            });

            System.out.print(" " + thread.getName());
            thread.start();
        }
        System.out.println("");
        System.out.println("Sleeping "
                + Thread.currentThread().getName()
                + " 30 seconds so that other threads may run.");
        Thread.sleep(30000);
        while (_activeCount > 0) {
            try {
                wait();
            } catch (InterruptedException ex) {
                break;
            }
        }
        if (_lastException != null) {
            throw _lastException;
        }
    }

    public static void main(String[] args) throws Exception {
        RunMultipleModels runMultipleModels= new RunMultipleModels();
        runMultipleModels.run();
    }


    // The count of currently executing runs.
    private int _activeCount = 0;

    // The last exception that was thrown.
    private Exception _lastException;
}
