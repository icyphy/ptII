Elaine Cheong
June 6, 2007

Instructions for creating a Ptalon version of a Viptos demo:
- Save nodes as submodels (make into MoML files). Edit MoML files:
  - Replace top-level "entity" tags with "class" tag.
  - Fix the PtinyOSDirector output directory.
  - Delete any timeResolution or nodeID parameter values from
PtinyOSDirector section of MoML file.
  - Do above fix for SharedParameter:
    - For PtinyOS nodes, create timeResolution (=0.25E-6) and nodeID (=1)
parameters at the top level, then set the value of the PtinyOSDirector
parameters to the names of the new parameters.
    - For non-PtinyOS submodels, manually set the value of the
timeResolution parameter to 0.25E-6.

- Create a Ptalon file that instantiates all of the submodels that you
created above.  Make sure to set the channel name correctly (see Ptalon
SmallWorld demo for an example), or you may get type resolution errors
because it can't find channel already named in the WirelessIOPort
settings.

- Create a new Wireless graph (contains a WirelessDirector).
  - Change timeResolution parameter in WirelessDirector to 0.25E-6 (needed
for TOSSIM).
  - Create a new PtalonActor (Graph --> Instantiate Entity -->
ptolemy.actor.ptalon.PtalonActor).
  - Set the PtalonActor ptalonCodeLocation to the Ptalon file created
above.
  - Set values of all parameters specified in your Ptalon file, including
channel name and number of nodes (if using any of the Ptalon wireless
demos as an example).

- Save everything before running!

- Hopefully it works!
