package com.property.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.property.contracts.PropertyContract;
import com.property.contracts.PropertySurveyorContract;
import com.property.states.PropertyState;
import com.property.states.PropertySurveyorState;
import net.corda.core.contracts.Command;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.flows.*;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;

import java.security.PublicKey;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

// ******************
// * Initiator flow *
// ******************
@InitiatingFlow
@StartableByRPC
public class IssuePropertyFlow extends FlowLogic<Void> {
    private final ProgressTracker progressTracker = new ProgressTracker();



    private final String address;




    public IssuePropertyFlow(String address) {

        this.address = address;

    }

    @Override
    public ProgressTracker getProgressTracker() {
        return progressTracker;
    }

    @Suspendable
    @Override
    public Void call() throws FlowException {
        // Starting FlowSession with Issuing Authority

        //GETTING NOTARY
        Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);

        //GETTING DATA FOR OUTPUT STATE: PropertySurveyorState

        //linearID
        UniqueIdentifier linearId = new UniqueIdentifier(null, UUID.randomUUID());

        //surveyorApproved
        Boolean surveyorApproved = false;
        //Boolean surveyorApproved = otherPartySession.receive(Boolean.class).unwrap(bl -> bl);
        //otherPartySession.send(address);

        //issuing authority
        Party issuingAuthority = getOurIdentity();

        //surveyor
        CordaX500Name surveyorName = new CordaX500Name("Surveyor","Bangalore","IN");
        Party surveyor = getServiceHub().getNetworkMapCache().getPeerByLegalName(surveyorName);


        //Building Output State
        PropertySurveyorState outputState = new PropertySurveyorState(linearId, address, surveyorApproved, issuingAuthority, surveyor);

        //Getting signers and command
        List<PublicKey> requiredSigners = Arrays.asList(getOurIdentity().getOwningKey(), surveyor.getOwningKey());
        Command command = new Command<>(new PropertySurveyorContract.Commands.Issue(), requiredSigners);


        //Building Transactions
        TransactionBuilder txBuilder = new TransactionBuilder(notary);
        txBuilder.addCommand(command);
        txBuilder.addOutputState(outputState,PropertySurveyorContract.ID);

        //Verifying transaction
        txBuilder.verify(getServiceHub());

        //Signing Transactions
        SignedTransaction signedTx = getServiceHub().signInitialTransaction(txBuilder);

        //Starting session at Surveyor's end
        FlowSession otherPartySession = initiateFlow(surveyor);

        //Gathering Counterparty Signatures
        SignedTransaction fullySignedTx = subFlow(new CollectSignaturesFlow(
                signedTx, Arrays.asList(otherPartySession), CollectSignaturesFlow.tracker()));

        //Committing the transaction to ledger
        subFlow(new FinalityFlow(fullySignedTx, otherPartySession));
        return null;
    }




}
