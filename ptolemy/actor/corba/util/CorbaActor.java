/*
 * File: ../../../ptolemy/actor/corba/util/CorbaActor.java
 * From: CorbaActor.idl
 * Date: Tue Jul 27 13:54:19 1999
 *   By: idltojava Java IDL 1.2 Aug 11 1998 02:00:18
 */

package ptolemy.actor.corba.util;
public interface CorbaActor
    extends org.omg.CORBA.Object, org.omg.CORBA.portable.IDLEntity {
    void fire()
        throws ptolemy.actor.corba.util.CorbaIllegalActionException;
    String getParameter(String paramName)
        throws ptolemy.actor.corba.util.CorbaIllegalActionException, ptolemy.actor.corba.util.CorbaUnknownParamException;
    void initialize()
        throws ptolemy.actor.corba.util.CorbaIllegalActionException;
    boolean hasData(String portName, short portIndex)
        throws ptolemy.actor.corba.util.CorbaIllegalActionException;
    boolean postfire()
        throws ptolemy.actor.corba.util.CorbaIllegalActionException;
    boolean prefire()
        throws ptolemy.actor.corba.util.CorbaIllegalActionException;
    void setParameter(String paramName, String paramValue)
        throws ptolemy.actor.corba.util.CorbaIllegalActionException, ptolemy.actor.corba.util.CorbaUnknownParamException, ptolemy.actor.corba.util.CorbaIllegalValueException;
    void stopFire()
        throws ptolemy.actor.corba.util.CorbaIllegalActionException;
    void terminate()
        throws ptolemy.actor.corba.util.CorbaIllegalActionException;
    void transferInput(String portName, short portIndex, String tokenValue)
        throws ptolemy.actor.corba.util.CorbaIllegalActionException, ptolemy.actor.corba.util.CorbaUnknownPortException, ptolemy.actor.corba.util.CorbaIndexOutofBoundException, ptolemy.actor.corba.util.CorbaIllegalValueException;
    String transferOutput(String portName, short portIndex)
        throws ptolemy.actor.corba.util.CorbaIllegalActionException, ptolemy.actor.corba.util.CorbaUnknownPortException, ptolemy.actor.corba.util.CorbaIndexOutofBoundException;
    void wrapup()
        throws ptolemy.actor.corba.util.CorbaIllegalActionException;
}
