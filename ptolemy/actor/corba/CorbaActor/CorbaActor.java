/*
 * File: ../../..//ptolemy/actor/corba/CorbaActor/CorbaActor.java
 * From: CorbaActor.idl
 * Date: Mon Jul 26 23:21:30 1999
 *   By: idltojava Java IDL 1.2 Aug 11 1998 02:00:18
 */

package ptolemy.actor.corba.CorbaActor;
public interface CorbaActor
    extends org.omg.CORBA.Object, org.omg.CORBA.portable.IDLEntity {
    void fire()
        throws ptolemy.actor.corba.CorbaActor.CorbaIllegalActionException;
    String getParameter(String paramName)
        throws ptolemy.actor.corba.CorbaActor.CorbaIllegalActionException, ptolemy.actor.corba.CorbaActor.CorbaUnknownParamException;
    void initialize()
        throws ptolemy.actor.corba.CorbaActor.CorbaIllegalActionException;
    boolean hasData(String portName, short portIndex)
        throws ptolemy.actor.corba.CorbaActor.CorbaIllegalActionException;
    boolean postfire()
        throws ptolemy.actor.corba.CorbaActor.CorbaIllegalActionException;
    boolean prefire()
        throws ptolemy.actor.corba.CorbaActor.CorbaIllegalActionException;
    void setParameter(String paramName, String paramValue)
        throws ptolemy.actor.corba.CorbaActor.CorbaIllegalActionException, ptolemy.actor.corba.CorbaActor.CorbaUnknownParamException, ptolemy.actor.corba.CorbaActor.CorbaIllegalValueException;
    void stopFire()
        throws ptolemy.actor.corba.CorbaActor.CorbaIllegalActionException;
    void terminate()
        throws ptolemy.actor.corba.CorbaActor.CorbaIllegalActionException;
    void transferInput(String portName, short portIndex, String tokenValue)
        throws ptolemy.actor.corba.CorbaActor.CorbaIllegalActionException, ptolemy.actor.corba.CorbaActor.CorbaUnknownPortException, ptolemy.actor.corba.CorbaActor.CorbaIndexOutofBoundException, ptolemy.actor.corba.CorbaActor.CorbaIllegalValueException;
    String transferOutput(String portName, short portIndex)
        throws ptolemy.actor.corba.CorbaActor.CorbaIllegalActionException, ptolemy.actor.corba.CorbaActor.CorbaUnknownPortException, ptolemy.actor.corba.CorbaActor.CorbaIndexOutofBoundException;
    void wrapup()
        throws ptolemy.actor.corba.CorbaActor.CorbaIllegalActionException;
}
