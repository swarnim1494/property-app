package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.template.contracts.TemplateContract;
import com.template.states.TemplateState;
import net.corda.core.contracts.Command;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.node.services.vault.IQueryCriteriaParser;
import net.corda.core.node.services.vault.QueryCriteria;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;
import org.jetbrains.annotations.NotNull;

import javax.persistence.criteria.Predicate;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;



// ******************
// * Initiator flow *
// ******************
@InitiatingFlow
@StartableByRPC
public class UpdatePropertyFlow extends FlowLogic<Void> {
    private final ProgressTracker progressTracker = new ProgressTracker();


    private final int propertyID;
    private final String address;



    public UpdatePropertyFlow(int propertyID, String address) {
        this.propertyID = propertyID;
        this.address = address;
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

        //InputState
        StateAndRef<TemplateState> inputState = getServiceHub().getVaultService().queryBy(TemplateState.class).getStates().get(0);
        Party owner = inputState.getState().getData().getOwner();
        Party surveyor =inputState.getState().getData().getSurveyor();
        // Starting FlowSession with Surveyor
        FlowSession otherPartySession = initiateFlow(surveyor);
        otherPartySession.send(address);
        //Checking if surveyor approved the property
        Boolean surveyorApproved = otherPartySession.receive(Boolean.class).unwrap(bl -> bl);

        //Building Output State
        TemplateState outputState = new TemplateState(propertyID, address, surveyorApproved, owner, surveyor);



        //Getting signers and command
        List<PublicKey> requiredSigners = Arrays.asList(getOurIdentity().getOwningKey(), surveyor.getOwningKey());
        Command command = new Command<>(new TemplateContract.Commands.Update(), requiredSigners);

        //Building Transactions
        TransactionBuilder txBuilder = new TransactionBuilder(notary);
        txBuilder.addCommand(command);
        txBuilder.addOutputState(outputState, TemplateContract.ID);
        txBuilder.addInputState(inputState);

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




}
