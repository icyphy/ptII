Update on July, 26, 2017, Janette Cardoso
These files were moved from demo/Billard and do the same simulation:
- Billard.xml: run with "make demo" or when opened with vergil
- 2Billes1Fed.xml: has instructions for a co-simulation with 
C++ billard balls federates.

-------------------
These files are test models for the improved HLA/Certi cosimulation framework.

PoolTable2.xml is a federate with a free instance of class Bille
(see Test.fed file for the FOM description). It can discover new instances.

TwoBilliardBalls.xml is a federate with 2 billiard balls (class Bille).

Notice that the balls does not change their directions if they hit each
other. See $PTII/org/hlacerti/demo/BillardHit 