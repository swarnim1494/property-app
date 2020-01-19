package com.property.contracts;

import com.property.states.PropertyState;
import com.property.states.PropertySurveyorState;
import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.CommandWithParties;
import net.corda.core.contracts.Contract;
import net.corda.core.identity.Party;
import net.corda.core.transactions.LedgerTransaction;

import static net.corda.core.contracts.ContractsDSL.requireSingleCommand;

// ************
// * Contract *
// ************
public class PropertySurveyorContract implements Contract {
    // This is used to identify our contract when building a transaction.
    public static final String ID = "com.property.contracts.PropertySurveyorContract";

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
        }



        if (command.getValue() instanceof Commands.SurveyApproval) {
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

        public static class SurveyApproval extends Commands {
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