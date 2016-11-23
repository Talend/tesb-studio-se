package org.talend.camel.designer.check;

import java.text.MessageFormat;
import java.util.List;

import org.talend.camel.designer.i18n.CamelDesignerMessages;
import org.talend.camel.designer.ui.editor.RouteProcess;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.process.Problem.ProblemStatus;
import org.talend.designer.core.ICheckNodesService;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.views.problems.Problems;

public class CheckRouteBuilderNodeService implements ICheckNodesService {

    @Override
    public void checkNode(Node node) {
        // TESB-7698
        IProcess process = node.getProcess();
        if (process == null || !(process instanceof RouteProcess)) {
            return;
        }
        // End TESB-7698

        checkIncomingConnections(node);
        checkSpecialComponent(node);
    }

    private void checkIncomingConnections(Node node) {
        // all route component can have only one incoming connection
        List<? extends IConnection> incomingConnections = node.getIncomingConnections();
        if (incomingConnections.size() > 1) {
            Problems.add(ProblemStatus.ERROR, node,
                    CamelDesignerMessages.getString("CheckRouteBuilderNodeService_incomingConnectionsError")); //$NON-NLS-1$
        }
    }

    private void checkSpecialComponent(Node node) {
        String componentName = node.getComponent().getName();
        if ("cErrorHandler".equals(componentName)) {
            checkErroHandler(node);
        } else if ("cIntercept".equals(componentName)) {
            checkIntercept(node);
        } else if ("cHttp".equals(componentName)) {
            checkHttp(node);
        } else if ("cAggregate".equals(componentName)) {
            checkAggregate(node);
        }
    }

    private void checkAggregate(Node node) {
        IElementParameter aggregationStrategy = node.getElementParameter("AGGREGATION_STRATEGY");
        IElementParameter groupExchanges = node.getElementParameter("GROUP_EXCHANGES");
        if ("true".equals(groupExchanges.getValue().toString())) {
            return;
        }
        Object value = aggregationStrategy.getValue();
        if (value == null || "".equals(value.toString().trim())) {
            Problems.add(ProblemStatus.ERROR, node, CamelDesignerMessages
                    .getString("CheckRouteBuilderNodeService_aggregationStrategyError", aggregationStrategy.getDisplayName())); //$NON-NLS-1$
        }
    }

    private void checkHttp(Node node) {
        /*
         * for cHttp, only SERVER case can be worked as a start node
         */
        List<? extends IConnection> incomingConnections = node.getIncomingConnections();
        IElementParameter elementParameter = node.getElementParameter("CLIENT");
        if ("true".equals(elementParameter.getValue().toString()) && incomingConnections.size() <= 0) {
            Problems.add(ProblemStatus.ERROR, node,
                    CamelDesignerMessages.getString("CheckRouteBuilderNodeService_htttIncomingError")); //$NON-NLS-1$
        }
    }

    private void checkIntercept(Node node) {
        /*
         * for intercept, the ROUTE and ROUTE_WHEN connections can't be exist at the same time.
         */
        List<? extends IConnection> outgoingConnections = node.getOutgoingConnections();
        if (outgoingConnections != null && outgoingConnections.size() > 1) {
            Problems.add(ProblemStatus.ERROR, node,
                    CamelDesignerMessages.getString("CheckRouteBuilderNodeService_interceptOutgoingError")); //$NON-NLS-1$
        }
    }

    private void checkErroHandler(Node node) {
        List<? extends IConnection> outgoingConnections = node.getOutgoingConnections();
        // if no output connection, then it's ok
        if (outgoingConnections == null || outgoingConnections.size() == 0) {
            return;
        }
        // else if no input connection, it's error
        List<? extends IConnection> incomingConnections = node.getIncomingConnections();
        if (incomingConnections == null || incomingConnections.size() == 0) {
            Problems.add(ProblemStatus.ERROR, node,
                    MessageFormat.format(CamelDesignerMessages.getString("CheckRouteBuilderNodeService_errorHandlerCantBeStart"), //$NON-NLS-1$
                            node.getComponent().getName()));
        }
    }

}
