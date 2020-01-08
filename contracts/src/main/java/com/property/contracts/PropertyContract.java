package com.property.contracts;

import com.property.states.PropertyState;
import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.CommandWithParties;
import net.corda.core.contracts.Contract;
import net.corda.core.identity.Party;
import net.corda.core.transactions.LedgerTransaction;


import static net.corda.core.contracts.ContractsDSL.requireSingleCommand;

// ************
// * Contract *
// ************
public class PropertyContract implements Contract {
    // This is used to identify our contract when building a transaction.
    public static final String ID = "com.property.contracts.PropertyContract";

    // A transaction is valid if the verify() function of the contract of all the transaction's input and output states
    // does not throw an exception.
    @Override
    public void verify(LedgerTransaction tx) {
        final CommandWithParties<Commands> command = requireSingleCommand(tx.getCommands(), Commands.class);

        if (command.getValue() instanceof Commands.Issue) {
            if (!tx.getInputs().isEmpty())
                throw new IllegalArgumentException("No inputs should be consumed when creating a new property.");
            if (!(tx.getOutputs().size() == 1))
                throw new IllegalArgumentException("There should be one output state.");

            final PropertyState outputState = tx.outputsOfType(PropertyState.class).get(0);
            Party owner = outputState.getOwner();
            Party surveyor = outputState.getSurveyor();

            boolean surveyorApproved = outputState.isSurveyorApproved();

 /*           if (!owner.getName().equals("GOI"))
                throw new IllegalArgumentException("Property cannot be issued to anyone other than GOI");*/
            if (!surveyorApproved == true)
                throw new IllegalArgumentException("Must be approved by surveyor");
        }


        if (command.getValue() instanceof Commands.Update) {
            //TODO Purchase Contract Logic
        }

        if (command.getValue() instanceof Commands.Transfer) {
            //TODO Purchase Contract Logic
        }

    }


    public static class Commands implements CommandData {

        public static class Issue extends Commands {
            @Override
            public boolean equals(Object obj) {
                return obj instanceof Issue;
            }
        }

        public static class Update extends Commands {
            @Override
            public boolean equals(Object obj) {
                return obj instanceof Issue;
            }
        }

        public static class Transfer extends Commands {
            @Override
            public boolean equals(Object obj) {
                return obj instanceof Transfer;
            }
        }
    }
}