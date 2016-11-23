package org.talend.designer.esb.webservice.ws.wsdlinfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.wsdl.Fault;
import javax.wsdl.Input;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.Output;
import javax.wsdl.Part;

public class OperationInfo {

    /** The URL where the target object is located. */
    private String targetURL;

    /** The namespace URI used for this SOAP operation. */
    private String namespaceURI;

    /** The name used to when making an invocation. */
    private final String targetMethodName;

    /** The action URI value to use when making a invocation. */
    private String soapActionURI;

    private ParameterInfo input;

    private ParameterInfo output;

    private Collection<ParameterInfo> faults = new ArrayList<>();

    private String portName;

    public OperationInfo(Operation operation) {
        targetMethodName = operation.getName();

        Input inDef = operation.getInput();
        if (inDef != null) {
            Message inMsg = inDef.getMessage();
            if (inMsg != null) {
                input = getParameterFromMessage(inMsg);
            }
        }
        Output outDef = operation.getOutput();
        if (outDef != null) {
            Message outMsg = outDef.getMessage();
            if (outMsg != null) {
                output = getParameterFromMessage(outMsg);
            }
        }
        for (Fault fault : (Collection<Fault>) operation.getFaults().values()) {
            Message faultMsg = fault.getMessage();
            if (faultMsg != null) {
                faults.add(getParameterFromMessage(faultMsg));
            }
        }
    }

    public ParameterInfo getInput() {
        return input;
    }

    public ParameterInfo getOutput() {
        return output;
    }

    public Collection<ParameterInfo> getFaults() {
        return faults;
    }

    public void setTargetURL(String value) {
        targetURL = value;
    }

    public String getTargetURL() {
        return targetURL;
    }

    public void setNamespaceURI(String value) {
        namespaceURI = value;
    }

    public String getNamespaceURI() {
        return namespaceURI;
    }

    public String getTargetMethodName() {
        return targetMethodName;
    }

    public void setSoapActionURI(String value) {
        soapActionURI = value;
    }

    public String getSoapActionURI() {
        return soapActionURI;
    }

    public String getPortName() {
        return portName;
    }

    public void setPortName(String portName) {
        this.portName = portName;
    }

    private static ParameterInfo getParameterFromMessage(Message msg) {
        ParameterInfo parameterRoot = new ParameterInfo();
        List<Part> msgParts = msg.getOrderedParts(null);
        if (msgParts.size() > 1) {
            parameterRoot.setName(ParameterInfo.MULTIPART);
        } else if (msgParts.size() == 1) {
            Part part = msgParts.iterator().next();
            if (part.getElementName() != null) {
                parameterRoot.setName(part.getElementName());
            } else if (part.getTypeName() != null) {
                parameterRoot.setName(part.getTypeName());
            }
        }
        return parameterRoot;
    }
}
