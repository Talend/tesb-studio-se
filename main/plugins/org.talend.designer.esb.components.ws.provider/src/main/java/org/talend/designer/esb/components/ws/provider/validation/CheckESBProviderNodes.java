package org.talend.designer.esb.components.ws.provider.validation;

import org.talend.core.model.process.IProcess;
import org.talend.core.model.process.Problem.ProblemStatus;
import org.talend.designer.core.ICheckNodesService;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.views.problems.Problems;

public class CheckESBProviderNodes implements ICheckNodesService {

    private static String RESTREQUEST_COMPONENT = "tRESTRequest";

    private static String ESBPROVIDER_COMPONENT = "tESBProviderRequest";

    public CheckESBProviderNodes() {
    }

    @Override
    public void checkNode(Node node) {
        String name = node.getComponent().getName();
        if (!ESBPROVIDER_COMPONENT.equals(name)) {
            return;
        }
        IProcess process = node.getProcess();
        if (process.getNodesOfType(ESBPROVIDER_COMPONENT).size() > 1) {
            Problems.add(ProblemStatus.ERROR, node, "Only one " + ESBPROVIDER_COMPONENT + " component is allowed in a Job!");
        } else if (process.getNodesOfType(RESTREQUEST_COMPONENT).size() > 0) {
            Problems.add(ProblemStatus.ERROR, node,
                    ESBPROVIDER_COMPONENT + " and " + RESTREQUEST_COMPONENT + " can't present in a same Job!");
        }
    }

}
