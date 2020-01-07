package com.template.contracts;

import com.template.states.TemplateState;
import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.CommandWithParties;
import net.corda.core.contracts.Contract;
import net.corda.core.identity.Party;
import net.corda.core.transactions.LedgerTransaction;

import static net.corda.core.contracts.ContractsDSL.requireSingleCommand;

// ************
// * Contract *
// ************
public class TemplateContract implements Contract {
    // This is used to identify our contract when building a transaction.
    public static final String ID = "com.template.contracts.TemplateContract";

    // A transaction is valid if the verify() function of the contract of all the transaction's input and output states
    // does not throw an exception.
    @Override
    public void verify(LedgerTransaction tx) {
        final CommandWithParties<TemplateContract.Issue> command = requireSingleCommand(tx.getCommands(), TemplateContract.Issue.class);

        if (command.getValue() instanceof Issue) {
            if (!tx.getInputs().isEmpty())
                throw new IllegalArgumentException("No inputs should be consumed when creating a new property.");
            if (!(tx.getOutputs().size() == 1))
                throw new IllegalArgumentException("There should be one output state.");

            final TemplateState outputState = tx.outputsOfType(TemplateState.class).get(0);
            Party owner = outputState.getOwner();
            Party surveyor = outputState.getSurveyor();

            boolean surveyorApproved = outputState.isSurveyorApproved();

 /*           if (!owner.getName().equals("GOI"))
                throw new IllegalArgumentException("Property cannot be issued to anyone other than GOI");*/
            if (!surveyorApproved == true)
                throw new IllegalArgumentException("Must be approved by surveyor");
        }

    }

    public static class Issue implements CommandData {
    }

    public static class Purchase implements CommandData {
    }

}