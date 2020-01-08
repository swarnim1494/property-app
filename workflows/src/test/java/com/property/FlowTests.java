package com.property;

import com.google.common.collect.ImmutableList;
import com.property.flows.IssuePropertyResponseFlow;
import net.corda.testing.node.MockNetwork;
import net.corda.testing.node.MockNetworkParameters;
import net.corda.testing.node.StartedMockNode;
import net.corda.testing.node.TestCordapp;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class FlowTests {
    private final MockNetwork network = new MockNetwork(new MockNetworkParameters(ImmutableList.of(
        TestCordapp.findCordapp("com.template.contracts"),
        TestCordapp.findCordapp("com.template.flows")
    )));
    private final StartedMockNode a = network.createNode();
    private final StartedMockNode b = network.createNode();

    public FlowTests() {
        a.registerInitiatedFlow(IssuePropertyResponseFlow.class);
        b.registerInitiatedFlow(IssuePropertyResponseFlow.class);
    }

    @Before
    public void setup() {
        network.runNetwork();
    }

    @After
    public void tearDown() {
        network.stopNodes();
    }

    @Test
    public void dummyTest() {

    }
}
