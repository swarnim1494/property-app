package com.property.webserver;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonAppend;
import com.google.gson.Gson;
import com.property.flows.IssuePropertyFlow;
import com.property.flows.SurveyorApprovalFlow;
import com.property.flows.TransferPropertyFlow;
import com.property.flows.UpdatePropertyFlow;
import com.property.states.PropertyState;
import com.property.states.PropertyState;
import com.property.states.PropertySurveyorState;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.core.messaging.CordaRPCOps;
import net.corda.core.messaging.FlowHandle;
import net.corda.core.messaging.FlowProgressHandle;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.management.Query;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * Define your API endpoints here.
 */


@RestController
@RequestMapping("/") // The paths for HTTP requests are relative to this base path.
public class Controller {
    private final CordaRPCOps proxy;
    private final static Logger logger = LoggerFactory.getLogger(Controller.class);

    public Controller(NodeRPCConnection rpc) {
        this.proxy = rpc.proxy;
    }

    @GetMapping(value = "/templateendpoint", produces = "text/plain")
    private String templateendpoint() {
        return "Test";
    }

    @GetMapping(value = "/mynode", produces = "text/plain")
    private String myNode() {
        return "Test";
    }

    @GetMapping(value = "/status", produces = "text/plain")
    private String status() {
        return "200";
    }

    @GetMapping(value = "/identities", produces = "text/plain")
    private String identities() {
        return proxy.nodeInfo().getLegalIdentities().toString();
    }

    @GetMapping(value = "/peers", produces = "text/plain")
    private String peers() {
        return proxy.networkMapSnapshot().toString();
    }

    @GetMapping(value = "/flows", produces = "text/plain")
    private String flows() {
        return proxy.registeredFlows().toString();
    }

    @GetMapping(value = "/get_prop_surveyor_states", produces = "text/plain")
    private String get_prop_surveyor_states() {
        JSONArray jsonArray =new JSONArray();
        proxy.vaultQuery(PropertySurveyorState.class).getStates().forEach(state->{
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("ID",state.getState().getData().getLinearId().getId());
            jsonObject.put("Address",state.getState().getData().getAddress());
            jsonObject.put("IssuingAuthority",state.getState().getData().getIssuingAuthority());
            jsonObject.put("Surveyor",state.getState().getData().getSurveyor());
            jsonObject.put("SurveyorApproved",state.getState().getData().isSurveyorApproved());

            jsonArray.add(jsonObject);

        });
        return jsonArray.toJSONString();
    }

    @GetMapping(value = "/get_prop_states", produces = "text/plain")
    private String get_prop_states() {
        JSONArray jsonArray =new JSONArray();
        proxy.vaultQuery(PropertyState.class).getStates().forEach(state->{
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("ID",state.getState().getData().getLinearId().getId());
            jsonObject.put("Address",state.getState().getData().getAddress());
            jsonObject.put("IssuingAuthority",state.getState().getData().getIssuingAuthority());
            jsonObject.put("Surveyor",state.getState().getData().getSurveyor());
            jsonObject.put("SurveyorApproved",state.getState().getData().isSurveyorApproved());
            jsonObject.put("owner",state.getState().getData().getOwner());
            jsonArray.add(jsonObject);

        });
        return jsonArray.toJSONString();
    }

    @GetMapping(value = "/issue_prop", produces = "text/plain")
    private String issue_prop(@RequestParam(value = "address", defaultValue = "World") String address) {
        try{
            //String id = proxy.startFlowDynamic(IssuePropertyFlow.class, address).getId().toString();
            String sid = proxy.startFlowDynamic(IssuePropertyFlow.class,address).getId().toString();

            return "Property created with Transaction ID: " + sid;
        }catch(Exception err){
            return err.getMessage();
        }

    }


    @GetMapping(value = "/sureveyor_approval", produces = "text/plain")
    private String issue_prop(@RequestParam(value = "id", defaultValue = "id") String id,@RequestParam(value = "approval", defaultValue = "false") boolean approval) {
        try{
            String sid = proxy.startFlowDynamic(SurveyorApprovalFlow.class,id,approval).getId().toString();
            return "Surveyor Approval updated with Transaction ID: " + sid;
        }
        catch (Exception err){
            return  err.getMessage().toString();
        }
    }

    @GetMapping(value = "/transfer_prop", produces = "text/plain")
    private String transfer_prop(@RequestParam(value = "id", defaultValue = "id") String id,@RequestParam(value = "party", defaultValue = "Party A") String party) {
        try{

            CordaX500Name partyX500Name = CordaX500Name.parse(party);
            Party partyName = proxy.wellKnownPartyFromX500Name(partyX500Name);
            String sid = proxy.startFlowDynamic(TransferPropertyFlow.class,id,partyName).getId().toString();
            return "Property transferred to " + party + " with Transaction ID: " + sid;
        }
        catch (Exception err){
            return  err.getMessage().toString();
        }
    }

    @GetMapping(value = "/update_prop", produces = "text/plain")
    private String update_prop(@RequestParam(value = "id", defaultValue = "id") String id,@RequestParam(value = "address", defaultValue = "address") String address) {
        try{
            String sid = proxy.startFlowDynamic(UpdatePropertyFlow.class,id,address).getId().toString();
            return "Property updated to address " + address + " with Transaction ID: " + sid;
        }
        catch (Exception err){
            return  err.getMessage().toString();
        }
    }


}

