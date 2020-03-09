FIXME: check why the original FOM SISO_SpaceFOM_entity.xml.orig. raises an exception.

As for the moment (current commit, after commit b3830017b) the objectClass name 
must be ObjectRoot instead of HLAobjectRoot in SISO_SpaceFOM_entity.xml.
The original SISO FDD file is SISO_SpaceFOM_entity.xml.orig.
However, HLAobjectRoot is used in sdse_hla1516mod.xml, and it works well in the model SDSEfom.xml. 

See:
https://www.sisostds.org/stdsdev/tracking_final/ViewCommentInfo.cfm?Doc_ID=105&Comment_ID=5

RTIG/Federation_fom.cc : 
   Debug(D, pdDebug) << "  Check consistency" << std::endl;
            if(is_mim) {
                auto is_compliant = Mom::isAvailableInRootObjectAndCompliant(temporary_root_object);
                if (!is_compliant) {
                    throw ErrorReadingFED("4.5.5.e : Invalid MIM.");
                }
            }
            else {
                auto is_valid = temporary_root_object.canBeAddedTo(*my_root_object);
                if (!is_valid) {
                    throw ErrorReadingFED("4.5.5.b : Invalid FOM module.");
                }
            }

Exception raised in Ptolemy:
ptolemy.kernel.util.IllegalActionException: Error reading FED file.
  in .ReceiveSpace.HlaManager
Because:
4.5.5.b : Invalid FOM module.
	at org.hlacerti.lib.HlaManager1516e.initialize(HlaManager1516e.java:791)
	at org.hlacerti.lib.HlaManager.initialize(HlaManager.java:900)
	at ptolemy.actor.CompositeActor.initialize(CompositeActor.java:912)
	at ptolemy.actor.Manager.initialize(Manager.java:724)
	at ptolemy.actor.Manager.execute(Manager.java:356)
	at ptolemy.actor.Manager.run(Manager.java:1263)
	at ptolemy.actor.Manager$PtolemyRunThread.run(Manager.java:1933)
Caused by: hla.rti1516e.exceptions.ErrorReadingFDD: 4.5.5.b : Invalid FOM module.
	at certi.rti1516e.impl.CertiRtiAmbassador.translateException(CertiRtiAmbassador.java:4054)
	at certi.rti1516e.impl.CertiRtiAmbassador.processRequest(CertiRtiAmbassador.java:3948)
	at certi.rti1516e.impl.CertiRtiAmbassador.createFederationExecution(CertiRtiAmbassador.java:424)
	at org.hlacerti.lib.HlaManager1516e.initialize(HlaManager1516e.java:751)
	... 6 more
Caused by: hla.rti1516e.exceptions.ErrorReadingFDD: 4.5.5.b : Invalid FOM module.
	at certi.rti1516e.impl.CertiRtiAmbassador.translateException(CertiRtiAmbassador.java:4054)
	at certi.rti1516e.impl.CertiRtiAmbassador.processRequest(CertiRtiAmbassador.java:3948)
	at certi.rti1516e.impl.CertiRtiAmbassador.createFederationExecution(CertiRtiAmbassador.java:424)
	at org.hlacerti.lib.HlaManager1516e.initialize(HlaManager1516e.java:751)
	at org.hlacerti.lib.HlaManager.initialize(HlaManager.java:900)
	at ptolemy.actor.CompositeActor.initialize(CompositeActor.java:912)
	at ptolemy.actor.Manager.initialize(Manager.java:724)
	at ptolemy.actor.Manager.execute(Manager.java:356)
	at ptolemy.actor.Manager.run(Manager.java:1263)
	at ptolemy.actor.Manager$PtolemyRunThread.run(Manager.java:1933)

