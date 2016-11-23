package org.talend.designer.esb.webservice.ws.wsdlutil;

import java.util.ArrayList;
import java.util.Collection;

import javax.wsdl.Binding;
import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.extensions.soap.SOAPOperation;
import javax.wsdl.extensions.soap12.SOAP12Address;
import javax.wsdl.extensions.soap12.SOAP12Operation;

import org.talend.designer.esb.webservice.ws.wsdlinfo.OperationInfo;
import org.talend.designer.esb.webservice.ws.wsdlinfo.ServiceInfo;

@SuppressWarnings("unchecked")
public class ComponentBuilder {

    private final static String OPERATION_TYPE_RPC = "rpc";

    private ComponentBuilder() {
    }

    public static Collection<ServiceInfo> buildModel(final Definition def) {
        Collection<ServiceInfo> serviceInfos = new ArrayList<>();
        for (Service service : (Collection<Service>) def.getServices().values()) {
            serviceInfos.add(populateComponent(service));
        }
        return serviceInfos;
    }

    private static ServiceInfo populateComponent(Service service) {
        ServiceInfo serviceInfo = new ServiceInfo();
        serviceInfo.setServiceName(service.getQName());
        Collection<Port> ports = service.getPorts().values();
        for (Port port : ports) {
            String soapLocation = null;
            SOAPAddress soapAddress = findExtensibilityElement(port.getExtensibilityElements(), SOAPAddress.class);
            if (null != soapAddress) {
                soapLocation = soapAddress.getLocationURI();
            } else {
                SOAP12Address soap12Address = findExtensibilityElement(port.getExtensibilityElements(), SOAP12Address.class);
                if (null != soap12Address) {
                    soapLocation = soap12Address.getLocationURI();
                }
            }
            Binding binding = port.getBinding();
            for (BindingOperation operation : (Collection<BindingOperation>) binding.getBindingOperations()) {
                SOAPOperation soapOperation = findExtensibilityElement(operation.getExtensibilityElements(), SOAPOperation.class);

                if (null != soapOperation && OPERATION_TYPE_RPC.equalsIgnoreCase(soapOperation.getStyle())) {
                    // TESB-6151 disable display of unsupported RPC type.
                    serviceInfo.setHasRpcOperation(true);
                    continue;
                }
                OperationInfo operationInfo = new OperationInfo(operation.getOperation());
                operationInfo.setPortName(port.getName());
                operationInfo.setNamespaceURI(binding.getPortType().getQName().getNamespaceURI());
                if (soapOperation != null) {
                    operationInfo.setSoapActionURI(soapOperation.getSoapActionURI());
                } else {
                    SOAP12Operation soap12Operation = findExtensibilityElement(operation.getExtensibilityElements(),
                            SOAP12Operation.class);
                    if (soap12Operation != null) {
                        operationInfo.setSoapActionURI(soap12Operation.getSoapActionURI());
                    }
                }

                operationInfo.setTargetURL(soapLocation);
                serviceInfo.addOperation(operationInfo);
            }
        }
        return serviceInfo;
    }

    private static <T> T findExtensibilityElement(Collection<ExtensibilityElement> extensibilityElements, Class<T> clazz) {
        if (extensibilityElements != null) {
            for (ExtensibilityElement element : extensibilityElements) {
                if (clazz.isAssignableFrom(element.getClass())) {
                    return clazz.cast(element);
                }
            }
        }
        return null;
    }

}
