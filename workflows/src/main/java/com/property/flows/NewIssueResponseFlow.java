package com.property.flows;

import co.paralleluniverse.fibers.Suspendable;
import net.corda.core.crypto.SecureHash;
import net.corda.core.flows.*;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.utilities.ProgressTracker;

import static net.corda.core.contracts.ContractsDSL.requireThat;

// ******************
// * Responder flow *
// ******************
@InitiatedBy(NewIssueFlow.class)
public class NewIssueResponseFlow extends FlowLogic<Void> {

    private static final ProgressTracker.Step NEW_ISSUE_RESPONDER_START = new ProgressTracker.Step("New Issue Responder - Starting");
    private static final ProgressTracker.Step NEW_ISSUE_RESPONDER_PRE_SUBFLOW = new ProgressTracker.Step("New Issue Responder - Reached finality Subflow");
    private static final ProgressTracker.Step NEW_ISSUE_RESPONDER_CROSSED_SUBFLOW = new ProgressTracker.Step("New Issue Responder - Crossed finality Subflow");

    private final ProgressTracker progressTracker = new ProgressTracker(NEW_ISSUE_RESPONDER_START,NEW_ISSUE_RESPONDER_PRE_SUBFLOW,NEW_ISSUE_RESPONDER_CROSSED_SUBFLOW);


    @Override
    public ProgressTracker getProgressTracker() {
        return progressTracker;
    }

    private FlowSession counterpartySession;

    public NewIssueResponseFlow(FlowSession counterpartySession) {
        this.counterpartySession = counterpartySession;
    }

    @Suspendable
    @Override
    public Void call() throws FlowException {

        progressTracker.setCurrentStep(NEW_ISSUE_RESPONDER_START);

        progressTracker.setCurrentStep(NEW_ISSUE_RESPONDER_PRE_SUBFLOW);
        subFlow(new ReceiveFinalityFlow(counterpartySession));
        progressTracker.setCurrentStep(NEW_ISSUE_RESPONDER_CROSSED_SUBFLOW);
        return null;
    }
}

/*
flow start Initiator propertyID: 1, address: "Happy Lane, Bellundur, Blr", owner: "GOI", surveyor: "Surveyor"

run vaultQuery contractStateType: com.template.states.PropertyState
 */