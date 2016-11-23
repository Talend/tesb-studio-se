package org.talend.designer.esb.webservice.ws.wsdlinfo;

import javax.xml.namespace.QName;

public class Function {

    private static final String ONE_WAY = "one-way"; //$NON-NLS-1$

    private static final String REQUEST_RESPONSE = "request-response"; //$NON-NLS-1$

    private String name;

    private String soapAction;

    private String nameSpaceURI;

    private String addressLocation;

    private QName serviceName;

    private String portName;

    private String communicationStyle;

    /**
     * Instantiates a new function. Only for display item, will not save to component node.
     * 
     * @param name the name
     * @param portName the port name
     * @param serviceName the service name
     */
    public Function(String name, String portName, QName serviceName) {
        this.name = name;
        this.portName = portName;
        this.serviceName = serviceName;
    }

    /**
     * Instantiates a new function. May use for save values to component node.
     * 
     * @param serviceInfo the service info
     * @param oper the operation info.
     */
    public Function(ServiceInfo serviceInfo, OperationInfo oper) {
        String operationName = oper.getTargetMethodName() + '(';
        this.serviceName = serviceInfo.getServerName();
        this.portName = oper.getPortName();
        this.soapAction = oper.getSoapActionURI();
        this.nameSpaceURI = oper.getNamespaceURI();
        this.addressLocation = oper.getTargetURL();

        // input parameters
        ParameterInfo input = oper.getInput();
        if (input != null) {
            operationName += input.getDisplayName();
        }
        operationName += "):"; //$NON-NLS-1$

        // output parameters
        ParameterInfo output = oper.getOutput();
        if (output != null) {
            communicationStyle = REQUEST_RESPONSE;
            operationName += output.getDisplayName();
        } else {
            communicationStyle = ONE_WAY;
        }

        Character sep = null;
        for (ParameterInfo fault : oper.getFaults()) {
            if (sep == null) {
                sep = ',';
                operationName += " throws "; //$NON-NLS-1$
            } else {
                operationName += sep;
            }
            operationName += fault.getDisplayName();
        }
        this.name = operationName;
    }

    public String getName() {
        return name;
    }

    /**
     * Gets the soap action.
     *
     * @return the soap action, may be <b>null</b> if no attribute <b>soapAction</b> for <b>&lt;soap:operation&gt;</b>
     * in wsdl
     */
    public String getSoapAction() {
        return soapAction;
    }

    public String getNameSpaceURI() {
        return nameSpaceURI;
    }

    public String getAddressLocation() {
        return this.addressLocation;
    }

    public String getServiceName() {
        return serviceName.getLocalPart();
    }

    public String getServiceNameSpace() {
        return serviceName.getNamespaceURI();
    }

    public String getPortName() {
        return this.portName;
    }

    /**
     * Gets the communication style.
     *
     * @return the communication style, only {@value #ONE_WAY} or {@value #REQUEST_RESPONSE}
     */
    public String getCommunicationStyle() {
        return communicationStyle;
    }

}
