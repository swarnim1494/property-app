package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.template.contracts.TemplateContract;
import com.template.states.TemplateState;
import net.corda.core.contracts.Command;
import net.corda.core.flows.*;
import net.corda.core.identity.CordaX500Name;
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
@StartableByRPC
public class IssuePropertyFlow extends FlowLogic<Void> {
    private final ProgressTracker progressTracker = new ProgressTracker();


    private final int propertyID;
    private final String address;
    private final Party owner;
    private final Party surveyor;


    public IssuePropertyFlow(int propertyID, String address, Party owner, Party surveyor) {
        this.propertyID = propertyID;
        this.address = address;
        this.owner = owner;
        this.surveyor = surveyor;
    }

    @Override
    public ProgressTracker getProgressTracker() {
        return progressTracker;
    }

    @Suspendable
    @Override
    public Void call() throws FlowException {
        // Starting FlowSession with Surveyor
        FlowSession otherPartySession = initiateFlow(surveyor);
        otherPartySession.send(address);
        //Checking if surveyor approved the property
        Boolean surveyorApproved = otherPartySession.receive(Boolean.class).unwrap(bl -> bl);

        //Getting notary
        Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);

        //Building Output State
        TemplateState outputState = new TemplateState(propertyID, address, surveyorApproved, owner, surveyor);

        //Getting signers and command
        List<PublicKey> requiredSigners = Arrays.asList(getOurIdentity().getOwningKey(), surveyor.getOwningKey());
        Command command = new Command<>(new TemplateContract.Issue(), requiredSigners);

        //Building Transactions
        TransactionBuilder txBuilder = new TransactionBuilder(notary);
        txBuilder.addCommand(command);
        txBuilder.addOutputState(outputState, TemplateContract.ID);

        //Verifying transaction
        txBuilder.verify(getServiceHub());

        //Signing Transactions
        SignedTransaction signedTx = getServiceHub().signInitialTransaction(txBuilder);

        //Gathering Counterparty Signatures
        SignedTransaction fullySignedTx = subFlow(new CollectSignaturesFlow(
                signedTx, Arrays.asList(otherPartySession), CollectSignaturesFlow.tracker()));

        //Committing the transaction to ledger
        subFlow(new FinalityFlow(fullySignedTx, otherPartySession));
        return null;
    }

    Boolean surveyorApproval(FlowSession otherPartySession, String address) throws FlowException {
        otherPartySession.send(address);
        return otherPartySession.receive(Boolean.class).unwrap(bl -> bl);
    }


}
