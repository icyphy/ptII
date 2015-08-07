These files are test models for the improved HLA/Certi cosimulation framework.


BilliardTable is a federate which can display up to 3 instances of class Bille
(see Test.fed file for the FOM description).
TwoBilliardBalls.xml is a federate with 2 billiard balls (class Bille) while  SingleBillardBall.xml has only one instance of class Bille.

For having a correct interaction between C++ and Ptolemy billiard federates, the patched version of billiard.cc must be used (/** pour recevoir la nouvelle position de la bille en 2 RAV avec un seul attribut par RAV */).

There should be no memory effect with BilliarTable when running several simulations in a row. But in case of doubt, I advice to close it and re-open.

For testing 
1- BilliardTable and SingleBillardBall.xml
* Start the simulation of SingleBillardBall.xml and then start BilliardTable (set
"Is synchronization point creator?" in HlaManager)
Don't launch the rtig, it is automatically started by Ptolemy.

2-  BilliardTable with N C++ federate (in BilliardTable, Make sure that you disable the synchronization point in the HlaManager)
* kill rtig if need and launch it back.
* launch the C++ billard first
* Launch BilliardTable and SingleBillardBall (or TwoBilliardBalls) and the N-1 C++ billard
* Get back to the first C++ billard and press ENTER

or : run FederationBillard.xml and follow the instructions

3-  BilliardTable with at least a C++ federate and  SingleBillardBall (or TwoBilliardBalls)
Make sure that you disable the synchronization point in the BilliardTable model  (double click on the HLAManager and untick the last box)

* kill rtig if need and launch it back.
* launch the C++ billard
* Launch BilliardTable  
* Launch SingleBillardBall (or TwoBilliardBalls)
* Get back C++ billard and press ENTER

Launch order does not matter between Ptolemy federates when using Billard.cc
because the C++ federate is the register of the synchronization point.
