/*
 @Copyright (c) 2003-2014 The Regents of the University of California.
 All rights reserved.

 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the
 above copyright notice and the following two paragraphs appear in all
 copies of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY



 */
package ptolemy.caltrop.ddi;

import ptolemy.actor.TypedAtomicActor;
import caltrop.interpreter.Context;
import caltrop.interpreter.ast.Actor;
import caltrop.interpreter.environment.Environment;
import caltrop.interpreter.util.PriorityUtil;

///////////////////////////////////////////////////////////////////
//// SDFJava

/**
 A plugin for the SDF domain. In SDF, a CAL actor is valid if:
 <p>
 <ol>
 <li> The rates of each action are statically computable, and these
 rates are the same for each action.
 <li> There is at least one unguarded action.
 </ol>

 This plugin also adds attributes containing rate information to the
 ports of the actor.

 @author Christopher Chang, Steve Neuendorffer
 @version $Id$
 @since Ptolemy II 4.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class SDF extends DataflowWithRates {
    /**
     * Create an new SDF DDI.
     * @param ptActor The instance of {@link ptolemy.actor.Actor
     * ptolemy.actor.Actor} that the plugin will be associated with.
     * @param actor The abstract syntax tree of the CAL source.
     * @param context The context that the plugin will use.
     * @param env The environment that the plugin will use.
     */
    public SDF(TypedAtomicActor ptActor, Actor actor, Context context,
            Environment env) {
        super(ptActor, actor, context, env);
    }

    /**
     * In SDF, an actor is legal if:
     * <ol>
     * <li> The rates of each action are statically computable, and
     * these rates are the same for each action.
     * <li> There is at least one unguarded action.
     * <li> The rates and guards of the initializers are statically computable.
     * </ol>
     * @return True if the actor associated with this <tt>DDI</tt> is
     * a legal SDF actor.
     */
    @Override
    public boolean isLegalActor() {
        try {
            if (!_atLeastOneUnguardedAction()) {
                throw new RuntimeException("No unguarded action!");
            }

            _actionRates = _computeActionRates(_actions);

            // Check that the rates are actually equal.
            if (!_allEqual(_actionRates)) {
                throw new RuntimeException("Action rates are not equal!");
            }

            _initializerRates = _computeActionRates(_actor.getInitializers());

            if (!_checkInitializerGuards()) {
                throw new RuntimeException("Initializers are guarded!");
            }

            if (PriorityUtil.hasPriorityOrder(_actor)) {
                // FIXME: Strictly speaking, we should allow priority in
                // SDF actors, under certain conditions (i.e the resulting
                // actor should remain an SDF actor, of course). So we
                // need to do the corresponding analysis.  Until then, we
                // simply disallow priority clauses in SDF actors.
                throw new RuntimeException("Has priorities!");
            }

            return true;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Get the name of this DDI.
     * @return "SDF".
     */
    @Override
    public String getName() {
        return "SDF";
    }

    /**
     * Setup the actor associated with this <tt>DDI</tt>. Assumes that
     * {@link #isLegalActor() isLegalActor()} is called first.
     * <p>
     * Setup involves attaching attributes with token
     * consumption/production rates to the input and output ports of
     * the actor associated with this <tt>DDI</tt>.
     */
    @Override
    public void setupActor() {
        // use the 0th element because the rates are all the same.
        _annotatePortsWithRates(_ptActor.inputPortList(),
                _actionRates[0].getInputRates(), "tokenConsumptionRate");
        _annotatePortsWithRates(_ptActor.outputPortList(),
                _actionRates[0].getOutputRates(), "tokenProductionRate");

        int i = _selectInitializer();

        if (i != -1) {
            _annotatePortsWithRates(_ptActor.outputPortList(),
                    _initializerRates[i].getOutputRates(),
                    "tokenInitProduction");
        }

        _ptActor.getDirector().invalidateSchedule();
    }

    ///////////////////////////////////////////////////////////////////
    ////                        private members                    ////
    private ActionRateSignature[] _actionRates;

    private ActionRateSignature[] _initializerRates;
}
