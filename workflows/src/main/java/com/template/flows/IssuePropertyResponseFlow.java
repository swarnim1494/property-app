package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import net.corda.core.crypto.SecureHash;
import net.corda.core.flows.*;
import net.corda.core.transactions.SignedTransaction;

import static net.corda.core.contracts.ContractsDSL.requireThat;

// ******************
// * Responder flow *
// ******************
@InitiatedBy(IssuePropertyFlow.class)
public class IssuePropertyResponseFlow extends FlowLogic<Void> {
    private FlowSession counterpartySession;

    public IssuePropertyResponseFlow(FlowSession counterpartySession) {
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

        class SignTxFlow extends SignTransactionFlow {
            private SignTxFlow(FlowSession otherPartySession) {
                super(otherPartySession);
            }

            @Override
            protected void checkTransaction(SignedTransaction stx) {
                requireThat(require -> {
                    return null;
                });
            }
        }

        SecureHash expectedTxId = subFlow(new SignTxFlow(counterpartySession)).getId();

        subFlow(new ReceiveFinalityFlow(counterpartySession));

        return null;
    }
}

/*
flow start Initiator propertyID: 1, address: "Happy Lane, Bellundur, Blr", owner: "GOI", surveyor: "Surveyor"

run vaultQuery contractStateType: com.template.states.TemplateState
 */