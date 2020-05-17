package com.sequenceiq.cloudbreak.orchestrator.salt.poller.checker;

import java.util.Set;

import com.sequenceiq.cloudbreak.orchestrator.model.Node;
import com.sequenceiq.cloudbreak.orchestrator.salt.client.SaltConnector;
import com.sequenceiq.cloudbreak.orchestrator.salt.client.target.HostList;
import com.sequenceiq.cloudbreak.orchestrator.salt.poller.BaseSaltJobRunner;
import com.sequenceiq.cloudbreak.orchestrator.salt.states.SaltStates;

public class StateRunner extends BaseSaltJobRunner {

    private final String state;

    public StateRunner(Set<String> targetHostnames, Set<Node> allNode, String state) {
        super(targetHostnames, allNode);
        this.state = state;
    }

    @Override
    public String submit(SaltConnector saltConnector) throws SaltJobFailedException {
        HostList targets = new HostList(getTargetHostnames());
        return SaltStates.applyState(saltConnector, state, targets).getJid();
    }

    @Override
    public String toString() {
        return "StateAllRunner{" + super.toString() + ", state: " + this.state + "}'";
    }
}
