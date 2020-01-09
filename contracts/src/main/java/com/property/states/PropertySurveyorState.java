package com.property.states;

import com.google.common.collect.ImmutableList;
import com.property.contracts.PropertyContract;
import com.property.contracts.PropertySurveyorContract;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.LinearState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import org.jetbrains.annotations.NotNull;

import java.util.List;

// *********
// * State *
// *********
@BelongsToContract(PropertySurveyorContract.class)
public class PropertySurveyorState implements LinearState {



    private final UniqueIdentifier linearId;
    private final String address;
    private final boolean surveyorApproved;
    private final Party issuingAuthority;
    private final Party surveyor;


    public PropertySurveyorState(UniqueIdentifier linearId, String address, boolean surveyorApproved, Party issuingAuthority, Party surveyor) {
        this.linearId = linearId;
        this.address = address;
        this.surveyorApproved = surveyorApproved;
        this.issuingAuthority = issuingAuthority;
        this.surveyor = surveyor;
    }


    public String getAddress() {
        return address;
    }

    public boolean isSurveyorApproved() {
        return surveyorApproved;
    }

    public Party getSurveyor() {
        return surveyor;
    }

    @NotNull
    @Override
    public UniqueIdentifier getLinearId() {
        return linearId;
    }

    public Party getIssuingAuthority() {
        return issuingAuthority;
    }

    @Override
    public List<AbstractParty> getParticipants() {
        return ImmutableList.of(issuingAuthority,surveyor);
    }


}