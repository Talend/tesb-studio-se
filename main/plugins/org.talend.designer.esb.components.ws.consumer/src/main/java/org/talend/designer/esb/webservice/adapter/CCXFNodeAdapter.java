// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.esb.webservice.adapter;

import javax.xml.namespace.QName;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.talend.core.model.properties.Item;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.utils.TalendTextUtils;
import org.talend.core.utils.TalendQuoteUtils;
import org.talend.designer.esb.webservice.ServiceSetting;
import org.talend.designer.esb.webservice.WebServiceConstants;
import org.talend.designer.esb.webservice.WebServiceNode;
import org.talend.designer.esb.webservice.util.RouteResourcesHelper;
import org.talend.designer.esb.webservice.ws.wsdlinfo.Function;

class CCXFNodeAdapter extends AbstractNodeAdapter implements WebServiceConstants {

    CCXFNodeAdapter(WebServiceNode node) {
        super(node);
    }

    @Override
    public IStatus setNodeSetting(ServiceSetting setting) {
        Function currentFunction = setting.getFunction();
        node.setParamValue("WSDL_FILE", setting.getWsdlLocation());
        String operationName = currentFunction.getName();
        operationName = operationName.substring(0, operationName.indexOf('('));
        String fullOperationName = "{" + currentFunction.getNameSpaceURI() + "}" + operationName;
        node.setParamValue(OPERATION_NAME, TalendTextUtils.addQuotes(fullOperationName));

        String fullServiceName = "{" + currentFunction.getServiceNameSpace() + "}" + currentFunction.getServiceName();
        node.setParamValue(SERVICE_NAME, TalendTextUtils.addQuotes(fullServiceName));

        String fullPortName = "{" + currentFunction.getServiceNameSpace() + "}" + currentFunction.getPortName();
        node.setParamValue(PORT_NAME, TalendTextUtils.addQuotes(fullPortName));

        final String endpoint = node.getParamStringValue("ADDRESS");
        if (endpoint == null || !endpoint.startsWith("context.")) {
            node.setParamValue("ADDRESS", TalendTextUtils.addQuotes(currentFunction.getAddressLocation()));
        }

        IRepositoryViewObject resourceNode = setting.getResourceNode();
        if (resourceNode != null) {
            final Item item = resourceNode.getProperty().getItem();
            String id = item.getProperty().getId();
            node.setParamValue("WSDL_FILE_REPO:ROUTE_RESOURCE_TYPE_ID", id);
            node.setParamValue("WSDL_FILE_REPO:ROUTE_RESOURCE_TYPE_VERSION", item.getProperty().getVersion());

            String classpathUrl = RouteResourcesHelper.getClasspathUrl(item);
            if (classpathUrl != null) {
                node.setParamValue("WSDL_FILE_REPO:ROUTE_RESOURCE_TYPE_RES_URI", classpathUrl);
            }
        }
        return Status.OK_STATUS;
    }

    @Override
    public Function loadCurrentFunction() {
        String operationName = node.getParamStringValue(OPERATION_NAME);
        if (operationName == null) {
            return null;
        }
        operationName = getExpressionParamValue(operationName);
        String portName = node.getParamStringValue(PORT_NAME);
        if (portName == null) {
            return null;
        }
        portName = getExpressionParamValue(portName);
        portName = portName.substring(portName.indexOf('}') + 1);
        portName = TalendQuoteUtils.removeQuotes(portName);
        String serviceName = node.getParamStringValue(SERVICE_NAME);
        if (serviceName == null) {
            return null;
        }
        QName serviceQName = QName.valueOf(serviceName);
        return new Function(operationName, portName, serviceQName);
    }

    @Override
    public String getInitialWsdlLocation() {
        String wsdlType = node.getParamStringValue("WSDL_TYPE");
        if ("repo".equals(wsdlType)) {
            String routeResFileLocation = getRouteResourceFileLocation();
            if (routeResFileLocation != null) {
                return TalendTextUtils.addQuotes(routeResFileLocation);
            }
        }
        return node.getParamStringValue("WSDL_FILE");
    }

    private String getRouteResourceFileLocation() {
        String repoId = node.getParamStringValue("WSDL_FILE_REPO:ROUTE_RESOURCE_TYPE_ID");
        String repoVersion = node.getParamStringValue("WSDL_FILE_REPO:ROUTE_RESOURCE_TYPE_VERSION");
        return RouteResourcesHelper.getRouteResourcesLocation(repoId, repoVersion);
    }

    @Override
    public boolean isServiceOperationRequired() {
        // only required when acts as consumer
        return isConsumerNode();
    }

    public boolean isConsumerNode() {
        return node.getIncomingConnections().size() > 0;
    }

    @Override
    public boolean allowPopulateSchema() {
        return false;
    }

    @Override
    public boolean routeResourcesAvailable() {
        return true;
    }
}
