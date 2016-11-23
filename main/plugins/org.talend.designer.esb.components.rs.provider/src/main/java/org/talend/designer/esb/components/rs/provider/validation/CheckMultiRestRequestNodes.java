package org.talend.designer.esb.components.rs.provider.validation;

import org.talend.core.model.process.IProcess;
import org.talend.core.model.process.Problem.ProblemStatus;
import org.talend.designer.core.ICheckNodesService;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.views.problems.Problems;

public class CheckMultiRestRequestNodes implements ICheckNodesService {

    private static String RESTREQUEST_COMPONENT = "tRESTRequest";

    private static String ESBPROVIDER_COMPONENT = "tESBProviderRequest";

    public CheckMultiRestRequestNodes() {
    }

    @Override
    public void checkNode(Node node) {
        String name = node.getComponent().getName();
        if (!RESTREQUEST_COMPONENT.equals(name)) {
            return;
        }
        IProcess process = node.getProcess();
        if (process.getNodesOfType(RESTREQUEST_COMPONENT).size() > 1) {
            Problems.add(ProblemStatus.ERROR, node, "Only one " + RESTREQUEST_COMPONENT + " component is allowed in a Job!");
        } else if (process.getNodesOfType(ESBPROVIDER_COMPONENT).size() > 0) {
            Problems.add(ProblemStatus.ERROR, node,
                    RESTREQUEST_COMPONENT + " and " + ESBPROVIDER_COMPONENT + " can't present in a same Job!");
        }
    }

}
