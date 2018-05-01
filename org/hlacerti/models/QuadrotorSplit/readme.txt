This federation is similar to TwoQuadrotors federation, but the quadrotor UAV1
is now split into two federates, one simulating the control and the other the
physical part.

Notice that the FOM was also "split": there is a new class Control. The class
quadrotor kept the name for being compatible with the federates from 
TwoQuadrotors federation. So federate Quad2s is the same as the one in 
TwoQuadrotors federation, with two small changes: the name of the new FOM,
and the name of the federation.

