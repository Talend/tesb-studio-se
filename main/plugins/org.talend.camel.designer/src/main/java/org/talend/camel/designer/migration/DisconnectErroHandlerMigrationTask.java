package org.talend.camel.designer.migration;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.common.util.EList;
import org.talend.camel.core.model.camelProperties.CamelProcessItem;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.designer.core.model.utils.emf.talendfile.ConnectionType;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;

/**
 * According to the bug fix of camel https://issues.apache.org/jira/browse/CAMEL-5032 now, cErrorHandler is not allow to
 * connect to a From node this migration task used to disconnect them if exist when importing an old item.
 */
public class DisconnectErroHandlerMigrationTask extends AbstractRouteItemMigrationTask {

    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2012, 10, 8, 14, 27, 00);
        return gc.getTime();
    }

    @Override
    public ExecutionResult execute(CamelProcessItem item) {
        try {
            disconnectErrorHandler(item);
            return ExecutionResult.SUCCESS_NO_ALERT;
        } catch (Exception e) {
            ExceptionHandler.process(e);
            return ExecutionResult.FAILURE;
        }

    }

    private void disconnectErrorHandler(CamelProcessItem item) throws PersistenceException {
        ProcessType processType = item.getProcess();

        /*
         * find all cErrorHandler components first and if none, then return
         */
        List<String> errorHandlerIds = findAllErrorHandlerIds(processType);
        if (errorHandlerIds == null || errorHandlerIds.size() == 0) {
            return;
        }

        /*
         * if a cErrorHandler has output connection but no input connection, then the output connection should be
         * removed.
         */

        /*
         * store all connections whose target is a cErrorHandler
         */
        Map<String, ConnectionType> inputMap = new HashMap<>();
        /*
         * store all connections whose source is a cErrorHandler
         */
        Map<String, ConnectionType> outputMap = new HashMap<>();

        EList connection = processType.getConnection();
        for (Object o : connection) {
            if (!(o instanceof ConnectionType)) {
                continue;
            }
            ConnectionType ct = (ConnectionType) o;
            String source = ct.getSource();
            String target = ct.getTarget();
            for (String id : errorHandlerIds) {
                /*
                 * if source of connection is a cErrorHandler
                 */
                if (id.equals(source)) {
                    ConnectionType connectionType = inputMap.get(id);
                    /*
                     * if the cErrorHandler doesn't find an Incoming connection yet then store it.
                     */
                    if (connectionType == null) {
                        outputMap.put(id, ct);
                    }
                    /*
                     * if find an Incoming connection then remove it from input map
                     */
                    else {
                        inputMap.remove(id);
                    }
                }
                /*
                 * if target of connection is a cErrorHandler
                 */
                else if (id.equals(target)) {
                    ConnectionType connectionType = outputMap.get(id);
                    /*
                     * if the cErrorHandler doesn't find an Outgoing connection yet then store it.
                     */
                    if (connectionType == null) {
                        inputMap.put(id, ct);
                    } else {
                        /*
                         * if find an outgoing connection then remove it from output map
                         */
                        outputMap.remove(id);
                    }
                }
            }
        }

        /*
         * if outgoing map is not empty, then the connection should be removed
         */
        if (!outputMap.isEmpty()) {
            Set<String> keySet = outputMap.keySet();
            Iterator<String> iterator = keySet.iterator();
            while (iterator.hasNext()) {
                String next = iterator.next();
                ConnectionType connectionType = outputMap.get(next);
                connection.remove(connectionType);
            }
            FACTORY.save(item, true);
        }

    }

    private List<String> findAllErrorHandlerIds(ProcessType pt) {
        List<String> errorHandlers = new ArrayList<>();
        for (Object o : pt.getNode()) {
            if (!(o instanceof NodeType)) {
                continue;
            }
            NodeType nt = (NodeType) o;
            if (!"cErrorHandler".equals(nt.getComponentName())) {
                continue;
            }
            EList elementParameter = nt.getElementParameter();
            for (Object e : elementParameter) {
                if (!(e instanceof ElementParameterType)) {
                    continue;
                }
                ElementParameterType ept = (ElementParameterType) e;
                if ("UNIQUE_NAME".equals(ept.getName())) {
                    errorHandlers.add(ept.getValue());
                }
            }
        }
        return errorHandlers;
    }
}
