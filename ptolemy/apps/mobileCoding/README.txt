-------------------------README--------------------------
mobilemodelSnd.xml use the PushSupplier actor to send
messages to the PushConsumer actor in mobilemodelRec.xml
or crazyboardRec.xml. The PushSupplier and PushConsumer
actor are two CORBA actors.

To execute the model:

1. Compile all the files on all the machines that are involved.
2. Select a port for the name service, say 1050
3. Start the transient name server from some machine, say
   xyz.eecs.berkeley.edu

   xyz> tnameserv -ORBInitialPort 1050
4. Possibly from another machine, say abc.eecs.berkeley.edu, open
   mobilemodelRec.xml or crazyboardRec.xml, specify the
   <i>ORBInitProperties<i>paremerter of the PushConsumer actor,
   for example:
   
   "-ORBInitialHost xyz.eecs.berkeley.edu -ORBInitialPort 1050"
   
   and specify the name of the consumer, say: "modelReceiver".

   start the receiver part first.

5. Possibly from a third machine, say def.eecs.berkeley.edu
   open mobilemodelSnd.xml, and specify the <i>ORBInitProperties<i>
   paremerter of the PushSupplier actor, for example:
   
   "-ORBInitialHost xyz.eecs.berkeley.edu -ORBInitialPort 1050"
   
   and specify the name of the consumer, say: "modelReceiver".
   
   select a model file to read from in the FileReader actor. 
   
   Run mobilemodelSnd.xml will send the moml string to the 
   consumer. It only run for one iteration. But you can choose
   some other files and run it again and again if you want.
   Notice: the model you send need to be compatable with the
   MobileModel actor.

Note: if you run all these parts on one machine, then just ignore
"-ORBInitialHost xyz.eecs.berkeley.edu" and use "-ORBInitialPort 1050" .

---------------------------------------------------------------------
