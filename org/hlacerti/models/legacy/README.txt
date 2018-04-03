-- Author: Janette Cadoso - ISAE SUPAERO - janette.cardoso@isae-supaero.fr 
-- Date: updated on May, 9,2016; July, 25, 2017

This folder contains two set of demos:

- MonoInstance: models working up to revision 71843 where only one instance of each
object class could be discovered. This version was implemented by Gilles Lasnier.

- StaticMultiInstance: models working up to revision 71943, where multiple instances of
an object class could be discovered. An HlaSubscriber must be add for each foreseen
instance of a subscribed class. This version was implemented by David Come.

- DynamicMultiInstance: models in this folder work up to April, 2, 2018.

Co-simulation with Ptolemy federates and C++ federates can be found here:
- For r71943: /Users/j.cardoso/ptIIforCommit/ptII/org/hlacerti/demo/legacy/StaticMultiInstance/Billard
- For current revision: /Users/j.cardoso/ptIIforCommit/ptII/org/hlacerti/demo/Billard
