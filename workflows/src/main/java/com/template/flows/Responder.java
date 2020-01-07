package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.template.states.TemplateState;
import net.corda.core.contracts.ContractState;
import net.corda.core.crypto.SecureHash;
import net.corda.core.flows.*;
import net.corda.core.transactions.SignedTransaction;
import org.jetbrains.annotations.NotNull;

// ******************
// * Responder flow *
// ******************
@InitiatedBy(Initiator.class)
public class Responder extends FlowLogic<Void> {
    private FlowSession counterpartySession;

    public Responder(FlowSession counterpartySession) {
        this.counterpartySession = counterpartySession;
    }

    @Suspendable
    @Override
    public Void call() throws FlowException {
        // Responder flow logic goes here.
        String address = counterpartySession.receive(String.class).unwrap(st -> {

            return st;
        });

        if (address.contains("Blr")) {
            counterpartySession.send(true);
        } else {
            counterpartySession.send(false);
        }

        subFlow(new ReceiveFinalityFlow(counterpartySession));

        return null;
    }
}

/*
flow start Initiator propertyID: 1, address: "Happy Lane, Bellundur, Blr", surveyorApproved: false, owner: "GOI", surveyor: "Surveyor", buyer: null

run vaultQuery contractStateType: com.template.states.TemplateState
 */