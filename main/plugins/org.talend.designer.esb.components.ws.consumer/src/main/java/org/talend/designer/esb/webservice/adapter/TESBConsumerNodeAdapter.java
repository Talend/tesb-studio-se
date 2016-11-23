package org.talend.designer.esb.webservice.adapter;

import static org.talend.designer.esb.webservice.WebServiceConstants.ENDPOINT;
import static org.talend.designer.esb.webservice.WebServiceConstants.METHOD;
import static org.talend.designer.esb.webservice.WebServiceConstants.PORT_NAME;
import static org.talend.designer.esb.webservice.WebServiceConstants.SERVICE_NAME;
import static org.talend.designer.esb.webservice.WebServiceConstants.SERVICE_NS;

import javax.xml.namespace.QName;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.talend.core.model.utils.TalendTextUtils;
import org.talend.designer.esb.webservice.ServiceSetting;
import org.talend.designer.esb.webservice.WebServiceNode;
import org.talend.designer.esb.webservice.ws.wsdlinfo.Function;

class TESBConsumerNodeAdapter extends AbstractNodeAdapter {

    TESBConsumerNodeAdapter(WebServiceNode node) {
        super(node);
    }

    @Override
    public IStatus setNodeSetting(ServiceSetting setting) {
        node.setParamValue("ENDPOINT", setting.getWsdlLocation());
        try {
            String wsdlString = setting.getCompressedAndEncodedWSDL();
            node.setParamValue("WSDL_CONTENT", wsdlString);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to create wsdl content", e);
        }
        Function currentFunction = setting.getFunction();
        node.setParamValue(PORT_NAME, currentFunction.getPortName());
        node.setParamValue("METHOD_NS", currentFunction.getNameSpaceURI());
        node.setParamValue(METHOD, currentFunction.getName());
        node.setParamValue(SERVICE_NS, currentFunction.getServiceNameSpace());
        node.setParamValue(SERVICE_NAME, currentFunction.getServiceName());
        node.setParamValue("PORT_NS", currentFunction.getServiceNameSpace());
        node.setParamValue("SOAP_ACTION", currentFunction.getSoapAction());

        final String endpoint = node.getParamStringValue("ESB_ENDPOINT");
        if (endpoint == null || !endpoint.startsWith("context.")) {
            node.setParamValue("ESB_ENDPOINT", TalendTextUtils.addQuotes(currentFunction.getAddressLocation()));
        }

        node.setParamValue("COMMUNICATION_STYLE", currentFunction.getCommunicationStyle());
        return Status.OK_STATUS;
    }

    @Override
    public Function loadCurrentFunction() {
        String operationName = node.getParamStringValue(METHOD);
        if (operationName == null) {
            return null;
        }
        String portName = node.getParamStringValue(PORT_NAME);
        if (portName == null) {
            return null;
        }
        String serviceName = node.getParamStringValue(SERVICE_NAME);
        String targetNamespace = node.getParamStringValue(SERVICE_NS);
        if (serviceName == null || targetNamespace == null) {
            return null;
        }
        QName serviceQName = new QName(targetNamespace, serviceName);
        return new Function(operationName, portName, serviceQName);
    }

    @Override
    public String getInitialWsdlLocation() {
        return node.getParamStringValue(ENDPOINT);
    }

    @Override
    public boolean allowPopulateSchema() {
        return true;
    }
}