package org.talend.designer.esb.webservice;

import java.io.IOException;

import javax.wsdl.Definition;
import javax.wsdl.WSDLException;

import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.designer.esb.webservice.ws.wsdlinfo.Function;
import org.talend.designer.esb.webservice.ws.wsdlutil.CompressAndEncodeTool;

public class ServiceSetting {

    private String wsdlLocation;

    private Function function;

    private Definition definition;

    private boolean hasRpcOperation;

    private String port;

    private IRepositoryViewObject resourceNode;

    public void setWsdlLocation(String wsdlLocation) {
        this.wsdlLocation = wsdlLocation;
    }

    public String getWsdlLocation() {
        return wsdlLocation;
    }

    public void setFunction(Function function) {
        this.function = function;
    }

    public Function getFunction() {
        return function;
    }

    public void setDefinition(Definition definition) {
        this.definition = definition;
    }

    public Definition getDefinition() {
        return definition;
    }

    /**
     * Checks if is updated. If user do nothing about the binding dialog, then is not updated.
     * 
     * @return true, if is updated
     */
    public boolean isUpdated() {
        return definition != null;
    }

    public String getCompressedAndEncodedWSDL() throws IOException, WSDLException {
        return CompressAndEncodeTool.compressAndEncode(definition);
    }

    public void setHasRcpOperation(boolean hasRpcOperation) {
        this.hasRpcOperation = hasRpcOperation;
    }

    public boolean hasRpcOperation() {
        return hasRpcOperation;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public void setResourceNode(IRepositoryViewObject resourceNode) {
        this.resourceNode = resourceNode;
    }

    public IRepositoryViewObject getResourceNode() {
        return resourceNode;
    }
}
