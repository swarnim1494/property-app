package com.property.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.property.contracts.PropertyContract;
import com.property.states.PropertyState;
import com.property.states.PropertySurveyorState;
import net.corda.core.contracts.Command;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
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
public class NewIssueFlow extends FlowLogic<Void> {

    private static final ProgressTracker.Step NEW_ISSUE_START = new ProgressTracker.Step("New Issue - Starting");
    private static final ProgressTracker.Step NEW_ISSUE_PRE_SUBFLOW = new ProgressTracker.Step("New Issue - Pre finality Subflow");
    private static final ProgressTracker.Step NEW_ISSUE_CROSSED_SUBFLOW = new ProgressTracker.Step("New Issue - Crossed finality Subflow");

    private final ProgressTracker progressTracker = new ProgressTracker(NEW_ISSUE_START,NEW_ISSUE_PRE_SUBFLOW,NEW_ISSUE_CROSSED_SUBFLOW);

    @Override
    public ProgressTracker getProgressTracker() {
        return progressTracker;
    }

    private final PropertySurveyorState propertySurveyorState;

    public NewIssueFlow(PropertySurveyorState propertySurveyorState) {
        this.propertySurveyorState = propertySurveyorState;
    }




    @Suspendable
    @Override
    public Void call() throws FlowException {


        progressTracker.setCurrentStep(NEW_ISSUE_START);

        PropertySurveyorState otptState= propertySurveyorState;

        UniqueIdentifier linearID = otptState.getLinearId();
        String address = otptState.getAddress();
        Boolean surveyorApproved = otptState.isSurveyorApproved();
        Party issuingAuthority = otptState.getIssuingAuthority();
        Party owner = issuingAuthority;
        Party surveyor = otptState.getSurveyor();

        PropertyState newState = new PropertyState(linearID,address,surveyorApproved,owner,issuingAuthority,surveyor);

        List<PublicKey> requiredSigners = Arrays.asList(getOurIdentity().getOwningKey());
        Command command = new Command<>(new PropertyContract.Commands.Issue(), requiredSigners);

        Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);

        TransactionBuilder txBuilder = new TransactionBuilder(notary).addCommand(command).addOutputState(newState,PropertyContract.ID);

        SignedTransaction signedTx = getServiceHub().signInitialTransaction(txBuilder);

        FlowSession otherPartySession = initiateFlow(owner);

        progressTracker.setCurrentStep(NEW_ISSUE_PRE_SUBFLOW);
        subFlow(new FinalityFlow(signedTx,otherPartySession));
        progressTracker.setCurrentStep(NEW_ISSUE_CROSSED_SUBFLOW);

        return null;
    }




}
