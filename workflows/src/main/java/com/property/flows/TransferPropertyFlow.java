package com.property.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.property.contracts.PropertyContract;
import com.property.states.PropertyState;
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
public class TransferPropertyFlow extends FlowLogic<Void> {
    private final ProgressTracker progressTracker = new ProgressTracker();


    private final UniqueIdentifier linearId;
    private final Party owner;


    public TransferPropertyFlow(String inputID,  Party owner) {
        this.linearId = UniqueIdentifier.Companion.fromString(inputID);
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

        QueryCriteria queryCriteria = new QueryCriteria.LinearStateQueryCriteria(null,Arrays.asList(linearId.getId()));
        StateAndRef<PropertyState> inputState = getServiceHub().getVaultService().queryBy(PropertyState.class,queryCriteria).getStates().get(0);
        String address = inputState.getState().getData().getAddress();
        Boolean surveyorApproved = inputState.getState().getData().isSurveyorApproved();
        Party surveyor = inputState.getState().getData().getSurveyor();
        Party issuingAuthority = inputState.getState().getData().getIssuingAuthority();


        //Building Output State
        PropertyState outputState = new PropertyState(linearId, address, surveyorApproved, owner, issuingAuthority, surveyor);



        //Getting signers and command
        List<PublicKey> requiredSigners = Arrays.asList(getOurIdentity().getOwningKey(), owner.getOwningKey());
        Command command = new Command<>(new PropertyContract.Commands.Transfer(), requiredSigners);

        //Building Transactions
        TransactionBuilder txBuilder = new TransactionBuilder(notary);
        txBuilder.addCommand(command);
        txBuilder.addOutputState(outputState, PropertyContract.ID);
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
