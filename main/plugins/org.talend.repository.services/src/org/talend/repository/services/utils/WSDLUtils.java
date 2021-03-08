// ============================================================================
//
// Copyright (C) 2006-2021 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.repository.services.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.wsdl.Binding;
import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.PortType;
import javax.wsdl.Service;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.extensions.soap12.SOAP12Address;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.wst.wsdl.validation.internal.IValidationReport;
import org.eclipse.wst.wsdl.validation.internal.eclipse.WSDLValidator;
import org.talend.commons.runtime.model.repository.ERepositoryStatus;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.repository.ProjectManager;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.services.ServicesPlugin;
import org.talend.repository.services.Messages;
import org.talend.repository.services.model.services.ServiceConnection;
import org.talend.repository.services.model.services.ServiceItem;
import org.talend.repository.services.model.services.ServiceOperation;
import org.talend.repository.services.model.services.ServicePort;

/**
 * DOC ycbai class global comment. Detailled comment
 */
public class WSDLUtils {

    public static final String SERVICE_NAME = "SERVICE_NAME"; //$NON-NLS-1$

    public static final String SERVICE_NS = "SERVICE_NS"; //$NON-NLS-1$

    public static final String PORT_NAME = "PORT_NAME"; //$NON-NLS-1$

    public static final String PORT_NS = "PORT_NS"; //$NON-NLS-1$

    public static final String OPERATION_NAME = "OPERATION_NAME"; //$NON-NLS-1$

    public static final String OPERATION_NS = "OPERATION_NS"; //$NON-NLS-1$

    public static final String ENDPOINT_URI = "ENDPOINT_URI"; //$NON-NLS-1$

    public static final String WSDL_LOCATION = "WSDL_LOCATION"; //$NON-NLS-1$

    public static final String COMMUNICATION_STYLE = "COMMUNICATION_STYLE"; //$NON-NLS-1$

    public static final String FAULTS = "FAULTS"; //$NON-NLS-1$

    public static final String ONE_WAY = "one-way"; //$NON-NLS-1$

    public static final String REQUEST_RESPONSE = "request-response"; //$NON-NLS-1$

    public static Map<String, String> getServiceOperationParameters(IFile wsdlURI, String operationName, String portTypeName)
            throws CoreException {
        // NOTE: all below in assuming standalone (no another WSDL's imports) WS-I complaint WSDL !
        Map<String, String> map = new HashMap<String, String>();
        if (null == wsdlURI) { // no WSDL provided
            return map;
        }

        Definition wsdl = getDefinition(wsdlURI);

        for (Object serviceObject : wsdl.getServices().values()) {
            Service service = (Service) serviceObject;
            for (Object portObject : service.getPorts().values()) {
                Port port = (Port) portObject;
                try {
                    port.getBinding().getPortType().getQName().getLocalPart();
                } catch (NullPointerException npe) {
                    throw getCoreException(
                            "WSDL is not consistent. Can not find portType operation description for current service.", npe);
                }
                if (portTypeName.equals(port.getBinding().getPortType().getQName().getLocalPart())) {
                    final String targetNs = wsdl.getTargetNamespace();
                    map.put(SERVICE_NAME, service.getQName().getLocalPart());
                    map.put(SERVICE_NS, targetNs);
                    map.put(PORT_NAME, port.getName());
                    map.put(PORT_NS, targetNs);
                    map.put(OPERATION_NAME, operationName);
                    map.put(WSDL_LOCATION, wsdlURI.getLocation().toPortableString());

                    BindingOperation bindingOperation = port.getBinding().getBindingOperation(operationName, null, null);
                    if (null == bindingOperation) {
                        throw getCoreException("Operation '" + operationName + "' not found in binding", null);
                    }
                    map.put(COMMUNICATION_STYLE, null == bindingOperation.getBindingOutput() && bindingOperation
                            .getBindingFaults().isEmpty() ? ONE_WAY : REQUEST_RESPONSE);

                    String faults = null;
                    for (Object fault : bindingOperation.getBindingFaults().keySet()) {
                        if (faults == null) {
                            faults = (String) fault;
                        } else {
                            faults += ',' + (String) fault;
                        }
                    }
                    map.put(FAULTS, faults);

                    // map.put(OPERATION_NS, targetNs);
                    map.put(ENDPOINT_URI, getPortAddress(port));

                    break;
                }
            }
        }

        return map;
    }

    public static boolean isValidService(ServiceItem serviceItem) {
        boolean isValid = false;
        List<ServicePort> listPort = ((ServiceConnection) serviceItem.getConnection()).getServicePort();
        for (ServicePort port : listPort) {
            List<ServiceOperation> listOperation = port.getServiceOperation();
            for (ServiceOperation operation : listOperation) {
                if (operation.getReferenceJobId() != null) {
                    isValid = true;
                    break;
                }
            }
        }

        return isValid;
    }

    public static IFile getWsdlFile(IRepositoryNode repositoryNode) {
        return getWsdlFile(repositoryNode.getObject());
    }

    private static IFile getWsdlFile(IRepositoryViewObject serviceViewObject) {
        return getWsdlFile((ServiceItem) serviceViewObject.getProperty().getItem());
    }

    public static IFile getWsdlFile(ServiceItem serviceItem) {
        IProject currentProject = ProjectManager.getInstance().getResourceProject(serviceItem);
        String foldPath = serviceItem.getState().getPath();
        String folder = "services"; //$NON-NLS-1$
        if (!"".equals(foldPath)) { //$NON-NLS-1$
            folder += '/' + foldPath;
        }
        IFile file = currentProject.getFolder(folder).getFile(
                serviceItem.getProperty().getLabel() + '_' + serviceItem.getProperty().getVersion() + ".wsdl"); //$NON-NLS-1$
        // if (!file.exists()) {
        // // copy file to item
        // IFile fileTemp = null;
        // try {
        // folder = "";
        // if (!foldPath.equals("")) {
        // folder = "/" + foldPath;
        // }
        // fileTemp = currentProject.getFolder("services" + folder).getFile(
        // serviceItem.getProperty().getLabel() + "_" + serviceItem.getProperty().getVersion() + ".wsdl");
        // ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(new byte[0]);
        // if (!fileTemp.exists()) {
        // fileTemp.create(byteArrayInputStream, true, null);
        // } else {
        // fileTemp.delete(true, null);
        // fileTemp.create(byteArrayInputStream, true, null);
        // }
        // } catch (CoreException e) {
        // ExceptionHandler.process(e);
        // }
        // //
        // ReferenceFileItem referenceFileItem = PropertiesFactory.eINSTANCE.createReferenceFileItem();
        // ByteArray byteArray = PropertiesFactory.eINSTANCE.createByteArray();
        // referenceFileItem.setContent(byteArray);
        // referenceFileItem.setExtension("wsdl");
        // serviceItem.getReferenceResources().add(referenceFileItem);
        // referenceFileItem.getContent().setInnerContent(new byte[0]);
        // IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
        // try {
        // factory.save(serviceItem);
        // } catch (PersistenceException e) {
        // ExceptionHandler.process(e);
        // }
        // }
        return file;
    }

    public static Definition getDefinition(ServiceItem serviceItem) throws CoreException {
        return getDefinition(getWsdlFile(serviceItem));
    }

    // public static Definition getDefinition(String pathToWsdl) throws CoreException {
    // try {
    // WSDLFactory wsdlFactory = WSDLFactory.newInstance();
    // WSDLReader newWSDLReader = wsdlFactory.newWSDLReader();
    //
    // newWSDLReader.setExtensionRegistry(wsdlFactory.newPopulatedExtensionRegistry());
    // newWSDLReader.setFeature(com.ibm.wsdl.Constants.FEATURE_VERBOSE, false);
    // return newWSDLReader.readWSDL(pathToWsdl);
    // } catch (WSDLException e) {
    // throw new CoreException(StatusUtil.newStatus(IStatus.ERROR, e.getLocalizedMessage(), e));
    // }
    // }

    public static Definition getDefinition(IFile pathToWsdl) throws CoreException {
        try {
            WSDLFactory wsdlFactory = WSDLFactory.newInstance();
            WSDLReader newWSDLReader = wsdlFactory.newWSDLReader();

            newWSDLReader.setExtensionRegistry(wsdlFactory.newPopulatedExtensionRegistry());
            newWSDLReader.setFeature(com.ibm.wsdl.Constants.FEATURE_VERBOSE, false);
            return newWSDLReader.readWSDL(pathToWsdl.getLocationURI().toString());
        } catch (WSDLException e) {
            throw getCoreException(null, e);
        }
    }

    // public static Definition getWsdlDefinition(RepositoryNode repositoryNode) throws CoreException {
    // return getDefinition(getWsdlFile(repositoryNode).getLocation().toOSString());
    // }

    // /**
    // * Validate WSDL file.
    // *
    // * @param node
    // * @throws CoreException
    // */
    // public static void validateWsdl(RepositoryNode node) throws CoreException {
    // IFile wsdlFile = getWsdlFile(node);
    // if (null == wsdlFile) {
    // throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
    // Messages.PublishMetadata_Exception_wsdl_not_found));
    // }
    // String wsdlPath = wsdlFile.getLocationURI().toString();
    // validateWsdl(wsdlPath);
    // }

    /**
     * Validate WSDL file.
     *
     * @param wsdlUri
     * @throws CoreException
     */
    public static void validateWsdl(String wsdlUri) throws CoreException {
        WSDLValidator wsdlValidator = WSDLValidator.getInstance();
        // wsdlValidator.addURIResolver(new URIResolverWrapper());
        IValidationReport validationReport = wsdlValidator.validate(wsdlUri);
        if (!validationReport.isWSDLValid()) {
            throw getCoreException(Messages.PublishMetadata_Exception_wsdl_not_valid, null);
        }
    }

    public static String getPortAddress(final Port port) {
        final Collection<?> extensibilityElements = port.getExtensibilityElements();
        SOAPAddress soapAddress = findExtensibilityElement(extensibilityElements, SOAPAddress.class);
        if (null != soapAddress) {
            return soapAddress.getLocationURI();
        }
        SOAP12Address soap12Address = findExtensibilityElement(extensibilityElements, SOAP12Address.class);
        if (null != soap12Address) {
            return soap12Address.getLocationURI();
        }
        return null;
    }

    private static <T> T findExtensibilityElement(final Collection<?> extensibilityElements, // ExtensibilityElement
            Class<T> clazz) {
        if (extensibilityElements != null) {
            for (Object element : extensibilityElements) {
                if (clazz.isAssignableFrom(element.getClass())) {
                    return clazz.cast(element);
                }
            }
        }
        return null;
    }

    public static <T> Collection<T> findExtensibilityElements(final Collection<?> extensibilityElements, // ExtensibilityElement
            Class<T> clazz) {
        Collection<T> elements = new ArrayList<T>();
        if (extensibilityElements != null) {
            for (Object element : extensibilityElements) {
                if (clazz.isAssignableFrom(element.getClass())) {
                    elements.add(clazz.cast(element));
                }
            }
        }
        return elements;
    }

    private static CoreException getCoreException(final String message, final Throwable e) {
        String msg = message != null ? message : (e.getMessage() != null) ? e.getMessage() : e.getClass().getName();
        return new CoreException(new Status(IStatus.ERROR, ServicesPlugin.getDefault().getBundle().getSymbolicName(), msg, e));
    }

    public static boolean isOperationInBinding(Definition definition, String portTypeName, String operationName)
            throws CoreException {
        Collection<?> services = definition.getServices().values();
        for (Object s : services) {
            Service service = (Service) s;
            Collection<?> ports = service.getPorts().values();
            for (Object p : ports) {
                Port port = (Port) p;
                Binding binding = port.getBinding();
                if (binding == null) {
                    continue;
                }
                PortType portType = binding.getPortType();
                if (portType == null || !portTypeName.equals(portType.getQName().getLocalPart())) {
                    continue;
                }
                List<?> bindingOperations = binding.getBindingOperations();
                for (Object o : bindingOperations) {
                    BindingOperation bo = (BindingOperation) o;
                    if (operationName.equals(bo.getName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean isOperationInBinding(ServiceItem serviceItem, String portTypeName, String operationName)
            throws CoreException {
        return isOperationInBinding(getDefinition(serviceItem), portTypeName, operationName);
    }

    public static boolean isOperationInBinding(IRepositoryNode operationNode) {
        assert operationNode != null;
        IRepositoryViewObject object = operationNode.getObject();
        if (object == null || !(object instanceof OperationRepositoryObject)) {
            return false;
        }
        return ERepositoryStatus.ERROR != ((OperationRepositoryObject) object).getInformationStatus();
    }

}
