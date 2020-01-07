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
public class Initiator extends FlowLogic<Void> {
    private final ProgressTracker progressTracker = new ProgressTracker();


    private final int propertyID;
    private final String address;
    private final Party owner;
    private final Party surveyor;


    public Initiator(int propertyID, String address, Party owner, Party surveyor) {
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
        // Initiator flow logic goes here.


        FlowSession otherPartySession = initiateFlow(surveyor);
        otherPartySession.send(address);
        Boolean surveyorApproved = otherPartySession.receive(Boolean.class).unwrap(bl -> bl);
        Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);
        TransactionBuilder txBuilder = new TransactionBuilder(notary);
        List<PublicKey> requiredSigners = Arrays.asList(owner.getOwningKey(),surveyor.getOwningKey());
        Command command = new Command<>(new TemplateContract.Issue(), requiredSigners);
        txBuilder.addCommand(command);
        TemplateState outputState = new TemplateState(propertyID, address, surveyorApproved, owner, surveyor);
        txBuilder.addOutputState(outputState,TemplateContract.ID);

        txBuilder.verify(getServiceHub());

        SignedTransaction signedTx = getServiceHub().signInitialTransaction(txBuilder);


        subFlow(new FinalityFlow(signedTx, otherPartySession));
        return null;
    }
}
