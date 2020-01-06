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
        String address = counterpartySession.receive(String.class).unwrap(st->{

            return st;
        });

        if(address.contains("Blr")){
            counterpartySession.send(true);
        }else{
            counterpartySession.send(false);
        }

        class SignTxnFlow extends SignTransactionFlow{

            public SignTxnFlow(@NotNull FlowSession otherSideSession) {
                super(otherSideSession);
            }

            @Override
            protected void checkTransaction(@NotNull SignedTransaction stx) throws FlowException {

                ContractState output = stx.getTx().getOutputs().get(0).getData();
                TemplateState out = (TemplateState) output;
                String address = out.getAddress();
                if (!address.contains("Blr"))
                    throw new IllegalArgumentException("Not a valid address");


            }
        }
        SecureHash expectedTxnID = subFlow(new SignTxnFlow(counterpartySession)).getId();
        subFlow(new ReceiveFinalityFlow(counterpartySession,expectedTxnID));

        return null;
    }
}
