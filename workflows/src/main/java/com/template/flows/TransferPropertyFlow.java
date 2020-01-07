package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.template.contracts.TemplateContract;
import com.template.states.TemplateState;
import net.corda.core.contracts.Command;
import net.corda.core.contracts.StateAndRef;
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
@StartableByRPC
public class TransferPropertyFlow extends FlowLogic<Void> {
    private final ProgressTracker progressTracker = new ProgressTracker();


    private final int propertyID;
    private final Party owner;


    public TransferPropertyFlow(int propertyID, Party owner) {
        this.propertyID = propertyID;
        this.owner = owner;

    }

    @Override
    public ProgressTracker getProgressTracker() {
        return progressTracker;
    }

    @Suspendable
    @Override
    public Void call() throws FlowException {




        //Getting notary
        Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);

        StateAndRef<TemplateState> inputState = getServiceHub().getVaultService().queryBy(TemplateState.class).getStates().get(0);
        String address = inputState.getState().getData().getAddress();
        Boolean surveyorApproved = inputState.getState().getData().isSurveyorApproved();
        Party surveyor = inputState.getState().getData().getSurveyor();


        //Building Output State
        TemplateState outputState = new TemplateState(propertyID, address, surveyorApproved, owner, surveyor);



        //Getting signers and command
        List<PublicKey> requiredSigners = Arrays.asList(getOurIdentity().getOwningKey(), owner.getOwningKey());
        Command command = new Command<>(new TemplateContract.Commands.Transfer(), requiredSigners);

        //Building Transactions
        TransactionBuilder txBuilder = new TransactionBuilder(notary);
        txBuilder.addCommand(command);
        txBuilder.addOutputState(outputState, TemplateContract.ID);
        txBuilder.addInputState(inputState);

        //Verifying transaction
        txBuilder.verify(getServiceHub());

        FlowSession otherPartySession = initiateFlow(owner);

        //Signing Transactions
        SignedTransaction signedTx = getServiceHub().signInitialTransaction(txBuilder);

        //Gathering Counterparty Signatures
        SignedTransaction fullySignedTx = subFlow(new CollectSignaturesFlow(
                signedTx, Arrays.asList(otherPartySession), CollectSignaturesFlow.tracker()));

        //Committing the transaction to ledger
        subFlow(new FinalityFlow(fullySignedTx, otherPartySession));
        return null;
    }




}
