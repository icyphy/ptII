-- README for f14 centralized models
-- @version $Id: README.txt xx 2017-11-14  cardoso@isae.fr $
-- @Author: Janette Cardoso (janette_cardoso@isae-supaero.fr)

####################
## Purpose:

This directory contains the following centralized models of a f14 aircraft:
- f14Continuous.xml: continuous model of a f14
- f14ContinuousGforce.xml: same as f14Continuous.xml but with Pilot G force
- f14CentralizedControllerDE.xml: the top level model has a DE director and the 
  AutoPilot is a discretized model
- f14CentralizedControllerDEgForce.xml: same as f14CentralizedControllerDE.xml
  but with Pilot G force
- files elevComCentralized*.txt: the output "elevator command" using the above
models; microstepSampler is a parameter of the PeriodicSampler actor when the
top level director is DE.