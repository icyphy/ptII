Update on July, 26, 2017, Janette Cardoso
These files were moved from demo/Billard and do the same simulation:
- Billard.xml: run with "make demo" or when opened with vergil
- 2Billes1Fed.xml: has instructions for a co-simulation with 
C++ billard balls federates.

You can also add other balls, e.g., open the federate ../2Billes2Fed/SingleBillardBall.xml before
launching any federate. Launch the ball federates in any order, but PoolTable2.xml
must be the last one to be launched (register of the synchronization point).

-------------------
These files are test models for the improved HLA/Certi cosimulation framework.

PoolTable2.xml is a federate with a free instance of class Bille
(see Test.fed file for the FOM description). It can discover new instances.
PoolTable3.xml also works and in fact is a most recent model than PoolTable2.xml.
PoolTable2.xml was used in earlier versions of dynamic multi-instance where it
was needed to add a parameter "fed" that was used in the HlaSubscriber.

TwoBilliardBalls.xml is a federate with 2 billiard balls (class Bille).

Notice that the balls does not change their directions if they hit each
other. See $PTII/org/hlacerti/demo/BillardHit 