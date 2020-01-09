package com.property.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.property.contracts.PropertyContract;
import com.property.contracts.PropertySurveyorContract;
import com.property.states.PropertyState;
import com.property.states.PropertySurveyorState;
import net.corda.core.contracts.Command;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.crypto.SecureHash;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.node.services.vault.QueryCriteria;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;

import java.security.PublicKey;
import java.util.Arrays;
import java.util.List;

import static net.corda.core.contracts.ContractsDSL.requireThat;

// ******************
// * Responder flow *
// ******************
@InitiatingFlow
@InitiatedBy(SurveyorApprovalFlow.class)
public class SurveyorApprovalResponseFlow extends FlowLogic<Void> {

    private static final ProgressTracker.Step SURVEYOR_APROVAL_RESPONDER_START = new ProgressTracker.Step("Surveyor Approval Responder-Starting");
    private static final ProgressTracker.Step SURVEYOR_APROVAL_RESPONDER_PRE_FINALITY_SUBFLOW = new ProgressTracker.Step("Surveyor Approval Responder-Pre Subflow");
    private static final ProgressTracker.Step SURVEYOR_APROVAL_RESPONDER_CROSSED_FINALITY_SUBFLOW = new ProgressTracker.Step("Surveyor Approval Responder-Crossed Finality Subflow");
    private final ProgressTracker progressTracker = new ProgressTracker(SURVEYOR_APROVAL_RESPONDER_START,SURVEYOR_APROVAL_RESPONDER_PRE_FINALITY_SUBFLOW,SURVEYOR_APROVAL_RESPONDER_CROSSED_FINALITY_SUBFLOW);

    @Override
    public ProgressTracker getProgressTracker() {
        return progressTracker;
    }

    private FlowSession counterpartySession;


    public SurveyorApprovalResponseFlow(FlowSession counterpartySession) {
        this.counterpartySession = counterpartySession;
    }

    @Suspendable
    @Override
    public Void call() throws FlowException {
        // Responder flow logic goes here.



        class SignTxFlow extends SignTransactionFlow {


            private SignTxFlow(FlowSession otherPartySession) {
                super(otherPartySession);
            }

            @Override
            protected void checkTransaction(SignedTransaction stx) throws FlowException {


                requireThat(require -> {
                    return null;
                });
            }
        }



        SecureHash expectedTxId = subFlow(new SignTxFlow(counterpartySession)).getId();
        subFlow(new ReceiveFinalityFlow(counterpartySession,expectedTxId));



        return null;
    }
}

/*
flow start Initiator propertyID: 1, address: "Happy Lane, Bellundur, Blr", owner: "GOI", surveyor: "Surveyor"

run vaultQuery contractStateType: com.template.states.PropertyState
 */