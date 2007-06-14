Elaine Cheong
June 6, 2007

NOTE: You must have the necessary tools for running Viptos before you
will be able to run the demos in the subdirectories.  For
instructions, see:
  $PTII/ptolemy/domains/ptinyos/doc/installation.htm

Instructions for creating a Ptalon version of a Viptos demo:

- Open an existing Viptos demo and save all unique nodes (or other composite
  actors) as submodels (make into MoML files).
- Edit each of the newly saved MoML files:
  - Replace top-level "entity" tags with "class" tag.  Don't forget to
    change the closing tag too!
  - Change the PtinyOSDirector output directory to the desired value.
  - Delete any lines containing the "timeResolution" or "nodeID" parameter
    from the PtinyOSDirector section of MoML file.
  - Temporary fix for Ptalon SharedParameter bug:
    - For PtinyOS nodes, create top-level parameters named
      "timeResolution" (=0.25E-6) and "nodeID" (=1), then set the
      value of the similarly named PtinyOSDirector parameters to the
      names of the new parameters.
    - For non-PtinyOS submodels, manually set the value of the
      "timeResolution" parameter in any instances of DE Director to
      0.25E-6.

- With a text editor, create a Ptalon file (.ptln) that instantiates
all of the submodels that you created above.  Make sure to set the
channel name correctly (see Ptalon SmallWorld demo for an example), or
you may get type resolution errors because it can't find channel
already named in the WirelessIOPort settings.

- In Viptos or VisualSense, create a new Wireless graph (contains a
  WirelessDirector).
  - Change timeResolution parameter in WirelessDirector to 0.25E-6
    (this is needed for TOSSIM.  The default value will execute too
    slowly).
  - Create a new PtalonActor (Graph --> Instantiate Entity -->
    ptolemy.actor.ptalon.PtalonActor).
  - Set the PtalonActor "ptalonCodeLocation" parameter to the Ptalon
    file created above.
  - Set values of all parameters specified in your Ptalon file.  If
    you used any of the Ptalon wireless demos (SmallWorld or ptinyos)
    as an example, make sure to set the channel name and number of
    nodes.

- Save everything before running!

- Hopefully it works!
