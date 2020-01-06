package com.template.states;

import com.google.common.collect.ImmutableList;
import com.template.contracts.TemplateContract;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.ContractState;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;

import java.util.List;

// *********
// * State *
// *********
@BelongsToContract(TemplateContract.class)
public class TemplateState implements ContractState {

    private final int propertyID;
    private final String address;
    private final boolean surveyorApproved;
    private final Party owner;
    private final Party surveyor;
    private Party buyer;



    public TemplateState(int propertyID, String address, boolean surveyorApproved, Party owner, Party surveyor, Party buyer) {
        this.propertyID = propertyID;
        this.address = address;
        this.surveyorApproved = surveyorApproved;
        this.owner = owner;
        this.surveyor = surveyor;
        this.buyer = buyer;
    }

    public TemplateState(int propertyID, String address, boolean surveyorApproved, Party owner, Party surveyor) {
        this.propertyID = propertyID;
        this.address = address;
        this.surveyorApproved = surveyorApproved;
        this.owner = owner;
        this.surveyor = surveyor;
    }


    public int getPropertyID() {
        return propertyID;
    }

    public String getAddress() {
        return address;
    }

    public boolean isSurveyorApproved() {
        return surveyorApproved;
    }

    public Party getBuyer() {
        return buyer;
    }

    public Party getOwner() {
        return owner;
    }

    public Party getSurveyor() {
        return surveyor;
    }

    @Override
    public List<AbstractParty> getParticipants() {
        return ImmutableList.of(owner,surveyor);
    }
}