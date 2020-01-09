package com.property.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.property.contracts.PropertyContract;
import com.property.contracts.PropertySurveyorContract;
import com.property.states.PropertyState;
import com.property.states.PropertySurveyorState;
import net.corda.core.contracts.Command;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.node.services.vault.QueryCriteria;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;

import java.security.PublicKey;
import java.util.Arrays;
import java.util.List;

// ******************
// * Initiator flow *
// ******************
@InitiatingFlow
@StartableByRPC
public class SurveyorApprovalFlow extends FlowLogic<Void> {

    private static final ProgressTracker.Step SURVEYOR_APROVAL_START = new ProgressTracker.Step("Surveyor Approval - Starting");
    private static final ProgressTracker.Step SURVEYOR_APROVAL_PRE_FINALITY_SUBFLOW = new ProgressTracker.Step("Surveyor Approval-Pre Subflow");
    private static final ProgressTracker.Step SURVEYOR_APROVAL_CROSSED_FINALITY_SUBFLOW = new ProgressTracker.Step("Surveyor Approval-Crossed Finality Subflow");
    private static final ProgressTracker.Step SURVEYOR_APROVAL_CROSSED_NEW_ISSUE_SUBFLOW = new ProgressTracker.Step("Surveyor Approval-Crossed New Issue Subflow");
    private final ProgressTracker progressTracker = new ProgressTracker(SURVEYOR_APROVAL_START,SURVEYOR_APROVAL_PRE_FINALITY_SUBFLOW,SURVEYOR_APROVAL_CROSSED_FINALITY_SUBFLOW,SURVEYOR_APROVAL_CROSSED_NEW_ISSUE_SUBFLOW);

    @Override
    public ProgressTracker getProgressTracker() {
        return progressTracker;
    }


    private final UniqueIdentifier linearId;
    private final Boolean surveyorApproved;

    public SurveyorApprovalFlow(String inputID,Boolean surveyorApproved) {
        this.linearId = UniqueIdentifier.Companion.fromString(inputID);
        this.surveyorApproved=surveyorApproved;
    }



    @Suspendable
    @Override
    public Void call() throws FlowException {


        progressTracker.setCurrentStep(SURVEYOR_APROVAL_START);
        QueryCriteria queryCriteria = new QueryCriteria.LinearStateQueryCriteria(null,Arrays.asList(linearId.getId()));
        StateAndRef<PropertySurveyorState> inputState = getServiceHub().getVaultService().queryBy(PropertySurveyorState.class,queryCriteria).getStates().get(0);





        String address = inputState.getState().getData().getAddress();
        Party issuingAuthority = inputState.getState().getData().getIssuingAuthority();
        Party surveyor = inputState.getState().getData().getSurveyor();


        //Building Output State
        PropertySurveyorState outputState = new PropertySurveyorState(linearId, address, surveyorApproved, issuingAuthority, surveyor);


        //Getting notary
        Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);

        //Getting signers and command
        List<PublicKey> requiredSigners = Arrays.asList(getOurIdentity().getOwningKey(), issuingAuthority.getOwningKey());
        Command command = new Command<>(new PropertySurveyorContract.Commands.SurveyApproval(), requiredSigners);

        //Building Transactions
        TransactionBuilder txBuilder = new TransactionBuilder(notary);
        txBuilder.addCommand(command);
        txBuilder.addOutputState(outputState, PropertySurveyorContract.ID);
        txBuilder.addInputState(inputState);

        //Verifying transaction
        txBuilder.verify(getServiceHub());

        FlowSession otherPartySession = initiateFlow(issuingAuthority);

        //Signing Transactions
        SignedTransaction signedTx = getServiceHub().signInitialTransaction(txBuilder);

        //Gathering Counterparty Signatures
        SignedTransaction fullySignedTx = subFlow(new CollectSignaturesFlow(
                signedTx, Arrays.asList(otherPartySession), CollectSignaturesFlow.tracker()));


        //Committing the transaction to ledger

        progressTracker.setCurrentStep(SURVEYOR_APROVAL_PRE_FINALITY_SUBFLOW);
        subFlow(new FinalityFlow(fullySignedTx, otherPartySession));
        progressTracker.setCurrentStep(SURVEYOR_APROVAL_CROSSED_FINALITY_SUBFLOW);

        subFlow(new NewIssueFlow(outputState));
        progressTracker.setCurrentStep(SURVEYOR_APROVAL_CROSSED_NEW_ISSUE_SUBFLOW);


        return null;
    }




}
