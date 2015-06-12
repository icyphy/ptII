/* Copyright 2011 - iSencia Belgium NV

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package org.ptolemy.testsupport;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.ExecutionListener;
import ptolemy.actor.Manager;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.IllegalActionException;

/**
 * A FlowManager offers services to work with flows:
 * <ul>
 * <li>read flows from a std Java Reader or an URL (in moml format)
 * <li>write flows to a std Java Writer (in moml format)
 * <li>execute flows in a blocking mode or in non-blocking mode
 * </ul>
 * 
 * @author erwin
 * 
 */
public class FlowManager {

  private static FlowManager defaultFlowManager;

  public static FlowManager getDefault() {
    if (defaultFlowManager == null) {
      defaultFlowManager = new FlowManager();
    }
    return defaultFlowManager;
  }
  
  private FlowManager() {
  }

  protected class ModelExecutionListener implements ExecutionListener {
    private Throwable throwable;

    private final CompositeActor flow;
    private ExecutionListener[] delegateListeners;

    ModelExecutionListener(final CompositeActor flow, ExecutionListener... delegateListeners) {
      this.flow = flow;
      this.delegateListeners = delegateListeners;
    }

    // IMPORTANT : inside the executionError impl we can not modify anything in the flow's composition
    // or this can lead to deadlock on ptolemy's Workspace internal locking!
    // e.g. flow.setManager(null) is absolutely forbidden!!
    public void executionError(ptolemy.actor.Manager manager, final Throwable throwable) {
      try {
        this.throwable = throwable;
        if (delegateListeners != null) {
          for (ExecutionListener delegateListener : delegateListeners) {
            if (delegateListener != null) {
              delegateListener.executionError(manager, throwable);
            }
          }
        }
      } finally {
        FlowManager.this.executionError(flow);
      }
    }

    public void executionFinished(final ptolemy.actor.Manager manager) {
      try {
        if (delegateListeners != null) {
          for (ExecutionListener delegateListener : delegateListeners) {
            if (delegateListener != null) {
              delegateListener.executionFinished(manager);
            }
          }
        }
      } finally {
        FlowManager.this.executionFinished(flow);
      }
    }

    public Throwable getThrowable() {
      return throwable;
    }

    public void illegalActionExceptionOccured() throws IllegalActionException {
      if (throwable != null) {
        Throwable t = throwable;
        if (t instanceof IllegalActionException) {
          throwable = null;
          throw (IllegalActionException) t;
        }
      }
    }

    public void managerStateChanged(ptolemy.actor.Manager manager) {
      if (delegateListeners != null) {
        for (ExecutionListener delegateListener : delegateListeners) {
          if (delegateListener != null) {
            delegateListener.managerStateChanged(manager);
          }
        }
      }
    }

    public void otherExceptionOccured() throws Throwable {
      if (throwable != null) {
        Throwable t = throwable;
        if (!(t instanceof IllegalActionException)) {
          throwable = null;
          throw t;
        }
      }
    }
  }

  // Maintains a mapping between locally executing flows and their managers
  private final Map<CompositeActor, Manager> flowExecutions = new HashMap<CompositeActor, Manager>();
  private final Map<CompositeActor, ExecutionListener> flowExecutionListeners = new HashMap<CompositeActor, ExecutionListener>();


  public static void applyParameterSettings(CompositeActor flow, Map<String, String> props) throws Exception {
    applyParameterSettings(flow, props, null);
  }

  public static void applyParameterSettings(CompositeActor flow, Map<String, String> props, Map<String, Object> contexts) throws Exception {
    Iterator<Entry<String, String>> propsItr = props.entrySet().iterator();
    while (propsItr.hasNext()) {
      Entry<String, String> element = propsItr.next();
      String propName = element.getKey();
      String propValue = element.getValue();
      String[] nameParts = propName.split("[\\.]");

      Entity e = flow;
      // set model parameters
      if (e instanceof CompositeActor) {
        if (!e.attributeList().isEmpty()) {
          try {
            final Parameter p = (Parameter) e.getAttribute(nameParts[nameParts.length - 1], Parameter.class);
            if (p != null) {
              p.setExpression(propValue);
              p.setPersistent(true);
            }
          } catch (final IllegalActionException e1) {
            e1.printStackTrace();
            // ignore
          }
        }
      }
      // parts[parts.length-1] is the parameter name
      // all the parts[] before that are part of the nested Parameter name
      for (int j = 0; j < nameParts.length - 1; j++) {
        if (e instanceof CompositeActor) {
          Entity test = ((CompositeActor) e).getEntity(nameParts[j]);
          if (test == null) {
            try {
              // maybe it is a director
              ptolemy.actor.Director d = ((CompositeActor) e).getDirector();
              if (d != null) {
                Parameter p = (Parameter) d.getAttribute(nameParts[nameParts.length - 1], Parameter.class);
                if (p != null) {
                  p.setExpression(propValue);
                  p.setPersistent(true);
                } else {
                  throw new IllegalArgumentException("Inconsistent parameter definition " + propName);
                }
              }
            } catch (IllegalActionException e1) {
              // ignore
            }
          } else {
            e = ((CompositeActor) e).getEntity(nameParts[j]);
            if (e != null) {
              try {
                Parameter p = (Parameter) e.getAttribute(nameParts[nameParts.length - 1], Parameter.class);
                if (p != null) {
                  p.setExpression(propValue);
                  p.setPersistent(true);
                }
              } catch (IllegalActionException e1) {
                e1.printStackTrace();
                // ignore
              }
            }
          }
        } else {
          break;
        }
      }
    }
  }


  /**
   * Executes the given flow and returns immediately, without waiting for the flow to finish its execution. <br>
   * The flow's actors' parameter values can be configured with the given properties. Parameter/property names are of
   * the format "actor_name.param_name", where the actor name itself can be nested. E.g. in the case of using composite
   * actors in a model... <br>
   * Parameter values should be passed as String values. For numerical parameter types, the conversion to the right
   * numerical type will be done internally.
   * 
   * @param flow
   * @param props
   * @param executionListeners
   * @throws Exception
   */
  public void execute(final CompositeActor flow, final Map<String, String> props, final ExecutionListener... executionListeners) throws Exception {
    if (props != null) {
      applyParameterSettings(flow, props);
    }
    ModelExecutionListener listener = this.new ModelExecutionListener(flow, executionListeners);
    flowExecutionListeners.put(flow, listener);
    final Manager manager = new Manager(flow.workspace(), flow.getName());
    manager.addExecutionListener(listener);

    flow.setManager(manager);
    flowExecutions.put(flow, manager);
    manager.startRun();
  }

  /**
   * Set the flow in standby state
   * 
   * @param flow
   */
  private synchronized void executionFinished(final CompositeActor flow) {
    flowExecutionListeners.remove(flow);
    final Manager mgr = flowExecutions.get(flow);
    if (mgr != null) {
      flowExecutions.remove(flow);
      try {
        flow.setManager(null);
      } catch (final IllegalActionException e) {
        // ignore
      }
    }
  }

  /**
   * Set the flow in standby state after an error
   * 
   * @param flow
   */
  private synchronized void executionError(final CompositeActor flow) {
    flowExecutionListeners.remove(flow);
      final Manager mgr = flowExecutions.get(flow);
      if (mgr != null) {
        flowExecutions.remove(flow);
        // this seems to lead to deadlocks inside Ptolemy!
//        try {
//          flow.setManager(null);
//        } catch (final IllegalActionException e) {
//          // ignore
//        }
      }
  }

}
