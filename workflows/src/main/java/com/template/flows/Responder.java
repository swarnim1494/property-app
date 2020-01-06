package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import net.corda.core.flows.*;

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

        subFlow(new ReceiveFinalityFlow(counterpartySession));

        return null;
    }
}
