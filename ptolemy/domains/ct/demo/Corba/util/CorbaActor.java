/*
 * File: ../../../../../PTOLEMY/DOMAINS/CT/DEMO/CORBA/UTIL/CORBAACTOR.JAVA
 * From: CORBAACTOR.IDL
 * Date: Thu Jul 29 14:22:20 1999
 *   By: idltojava Java IDL 1.2 Aug 18 1998 16:25:34
 */

package ptolemy.domains.ct.demo.Corba.util;
public interface CorbaActor
    extends org.omg.CORBA.Object, org.omg.CORBA.portable.IDLEntity {
    void fire()
        throws ptolemy.domains.ct.demo.Corba.util.CorbaIllegalActionException;
    String getParameter(String paramName)
        throws ptolemy.domains.ct.demo.Corba.util.CorbaIllegalActionException, ptolemy.domains.ct.demo.Corba.util.CorbaUnknownParamException;
    void initialize()
        throws ptolemy.domains.ct.demo.Corba.util.CorbaIllegalActionException;
    boolean hasData(String portName, short portIndex)
        throws ptolemy.domains.ct.demo.Corba.util.CorbaIllegalActionException, ptolemy.domains.ct.demo.Corba.util.CorbaIndexOutofBoundException, ptolemy.domains.ct.demo.Corba.util.CorbaUnknownPortException;
    boolean hasParameter(String paramName)
;
    boolean hasPort(String portName, boolean isInput, boolean isOutput, boolean isMultiport)
;
    void setPortWidth(String portName, short width)
        throws ptolemy.domains.ct.demo.Corba.util.CorbaIllegalActionException, ptolemy.domains.ct.demo.Corba.util.CorbaUnknownPortException;
    boolean postfire()
        throws ptolemy.domains.ct.demo.Corba.util.CorbaIllegalActionException;
    boolean prefire()
        throws ptolemy.domains.ct.demo.Corba.util.CorbaIllegalActionException;
    void setParameter(String paramName, String paramValue)
        throws ptolemy.domains.ct.demo.Corba.util.CorbaIllegalActionException, ptolemy.domains.ct.demo.Corba.util.CorbaUnknownParamException, ptolemy.domains.ct.demo.Corba.util.CorbaIllegalValueException;
    void stopFire()
        throws ptolemy.domains.ct.demo.Corba.util.CorbaIllegalActionException;
    void terminate()
        throws ptolemy.domains.ct.demo.Corba.util.CorbaIllegalActionException;
    void transferInput(String portName, short portIndex, String tokenValue)
        throws ptolemy.domains.ct.demo.Corba.util.CorbaIllegalActionException, ptolemy.domains.ct.demo.Corba.util.CorbaUnknownPortException, ptolemy.domains.ct.demo.Corba.util.CorbaIndexOutofBoundException, ptolemy.domains.ct.demo.Corba.util.CorbaIllegalValueException;
    String transferOutput(String portName, short portIndex)
        throws ptolemy.domains.ct.demo.Corba.util.CorbaIllegalActionException, ptolemy.domains.ct.demo.Corba.util.CorbaUnknownPortException, ptolemy.domains.ct.demo.Corba.util.CorbaIndexOutofBoundException;
    void wrapup()
        throws ptolemy.domains.ct.demo.Corba.util.CorbaIllegalActionException;
}
