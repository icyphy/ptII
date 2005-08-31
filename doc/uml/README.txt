ptII/doc/uml/README.txt
Version: $Id$

The Ptolemy II UML diagrams are in a separate cvs repository.
As of 8/05, these diagrams were in Visio 5, which can be read by
Visio 2000, but not really edited.

    Note that Visio 2000 will not read our [Visio 5] UML
    diagrams. Moreover, the UML editor in Visio 2000 is pretty close
    to useless in my view... For example, you cannot refer to any
    class you have not defined in the diagram, so every diagram has to
    be completely self-contained. In effect, to create a UML diagram
    for Vergil, you have to include a UML diagram for all of Ptolemy
    II... 

Another issue:

    Visio 2000 has a pretty incredible bug in it... If you save a file
    in Visio 5 format, it works. But then you can't close the file. It
    asks you if you want to save it. If you say yes, it asks you if
    you want to save it in Visio 5 format. If you say yes, then it
    saves it, but leaves it open. If you try to close it, it asks you
    if you want to save it... etc. If you click "no" in response to
    the question of whether you want to save it, it deletes the file
    !!!!!!!!!!!!!!
    Gone!!!!!!!!!!!! 

To check out the repository read only, do:

   cvs -d :pserver:anon@source.eecs.berkeley.edu:/home/cvs/cvsanon login
   # No password needed, hit enter
   cvs -d :pserver:anon@gigasource.eecs.berkeley.edu:/home/cvs/cvsanon ptIIdoc

To check it out read write, do

   cvs -d :ext:source.eecs.berkeley.edu:/home/cvs ptIIdoc

See http://chess.eecs.berkeley.edu/ptexternal for further information
about accessing Ptolemy via cvs.
