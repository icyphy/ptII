$Id$
This directory contains a tabbed single window mode that
was developed at Thal=E8s Research & Technology by J. Blanc and
B. Masson.

To use the single window mode:


For the single window mode, the principle is to catch the frames created
on each Tableau and move its container into the main TabbedPane
    
In this mode, you can either create classic editors, or  navigable graph
editor where beside the actors library there is a browsable model tree
(warning : this doesn't work with any state machine actor, because there
is only a navigable ActorGraphFrame, and there should be other navigable
Frame for modal models, FSM and interface automation actors. This could
be solved by merging the NavigableActorGraphFrame features into the
BasicGraph Frame, but as always we try to develop without modifying the
original Ptolemy sources.)
    
There is a specific effigy for navigable models (single window mode). To
create this specific effigy, the MoML file should contain a property at
the top level named "_navigable" (<property name=3D"_navigable" />). It is
created automatically for each new navigable graph editor (new menu),
 and could be added on existing models.
    
    
There are also others changes already identified in the comments inside
the official release that were needed :
      - A pre-parsing module that delete empty MoML attribute to avoid
bugs (in diva, for example, if the size attribute is empty then the MoML
file loading generates a bug)
      - in NavigableActorGraphFrame, two major bugs have been fixed : one
concerns the copy/paste feature when there are connected relations to
copy : we ensure that the relation will not be paste as diamond ; the
other one fixes the _close method to not loose the changes made.
    
These changes are all commented with the THALES prefix.
    

