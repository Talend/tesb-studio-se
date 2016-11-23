package org.talend.designer.esb.webservice.ui;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.wsdl.Definition;
import javax.wsdl.WSDLException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.talend.commons.ui.swt.extended.table.ExtendedTableModel;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.utils.TalendQuoteUtils;
import org.talend.designer.esb.webservice.SchemaTool;
import org.talend.designer.esb.webservice.ServiceSetting;
import org.talend.designer.esb.webservice.WebServiceComponentPlugin;
import org.talend.designer.esb.webservice.WebServiceNode;
import org.talend.designer.esb.webservice.adapter.AbstractNodeAdapter;
import org.talend.designer.esb.webservice.ws.wsdlinfo.Function;
import org.talend.designer.esb.webservice.ws.wsdlinfo.OperationInfo;
import org.talend.designer.esb.webservice.ws.wsdlinfo.ServiceInfo;
import org.talend.designer.esb.webservice.ws.wsdlutil.ComponentBuilder;

public class WebServiceUIPresenter implements WsdlFieldListener, ServicePortSelectionListener, FunctionSelectionListener {

    private WebServiceUI webServiceUI;

    private ServiceSetting currentSetting;

    private AbstractNodeAdapter nodeAdapter;

    private ExtendedTableModel<Function> functionTableModel;

    private ExtendedTableModel<String> portTableModel;

    private Map<String, List<Function>> portFunctionsMap;

    WebServiceUIPresenter(WebServiceUI webServiceUI, WebServiceNode webServiceNode) {
        this.webServiceUI = webServiceUI;
        this.currentSetting = new ServiceSetting();
        nodeAdapter = AbstractNodeAdapter.getAdapter(webServiceNode);

        List<Function> functionList = new ArrayList<>();
        functionTableModel = new ExtendedTableModel<>(null, functionList);
        portTableModel = new ExtendedTableModel<>(null, new ArrayList<String>());
    }

    void initWithCurrentSetting() {
        Function function = nodeAdapter.loadCurrentFunction();
        if (function != null) {
            String port = function.getPortName();
            currentSetting.setPort(port);
            currentSetting.setFunction(function);
            functionTableModel.add(function);
            portTableModel.add(port);
            portFunctionsMap = Collections.singletonMap(port, Collections.singletonList(function));
        }
    }

    public void retrieveData(String wsdlLocation) throws WSDLException, InvocationTargetException {
        String wsdlLocationTemp = TalendQuoteUtils.removeQuotesIfExist(wsdlLocation);
        Definition definition = nodeAdapter.generateDefinition(wsdlLocationTemp);
        currentSetting.setWsdlLocation(TalendQuoteUtils.addQuotes(wsdlLocationTemp));
        currentSetting.setDefinition(definition);
        List<Function> functionsAvailable = new ArrayList<>();
        boolean hasRpcOperation = false;
        for (ServiceInfo serviceInfo : ComponentBuilder.buildModel(definition)) {
            if (serviceInfo.isHasRpcOperation()) {
                hasRpcOperation = true;
            }
            for (OperationInfo oper : serviceInfo.getOperations()) {
                Function f = new Function(serviceInfo, oper);
                functionsAvailable.add(f);
            }
        }
        currentSetting.setHasRcpOperation(hasRpcOperation);

        portFunctionsMap = new LinkedHashMap<>();
        for (Function f : functionsAvailable) {
            List<Function> functions = portFunctionsMap.get(f.getPortName());
            if (functions == null) {
                functions = new ArrayList<>();
                portFunctionsMap.put(f.getPortName(), functions);
            }
            functions.add(f);
        }
    }

    @Override
    public ExtendedTableModel<String> getPortTableModel() {
        return portTableModel;
    }

    @Override
    public ExtendedTableModel<Function> getFunctionTableModel() {
        return functionTableModel;
    }

    @Override
    public void portSelected(String portName) {
        currentSetting.setPort(portName);
        functionTableModel.removeAll();
        functionTableModel.addAll(portFunctionsMap.get(portName));
        webServiceUI.selectFirstFunction();
    }

    @Override
    public void refreshPageByWsdl(final String wsdlLocation) {
        webServiceUI.setErrorMessage(null);
        portTableModel.removeAll();
        functionTableModel.removeAll();

        IRunnableWithProgress retrieveData = new IRunnableWithProgress() {

            @Override
            public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                monitor.beginTask("Retrieve WSDL parameter from net.", IProgressMonitor.UNKNOWN);
                try {
                    retrieveData(wsdlLocation);
                } catch (Exception e) {
                    throw new InvocationTargetException(e);
                } finally {
                    monitor.done();
                }
            }
        };
        try {
            webServiceUI.runWithProgress(retrieveData);
            if (currentSetting.hasRpcOperation()) {
                webServiceUI.setErrorMessage(Messages.getString("WebServiceUI.NotSupportRpc"));
            }

            final List<String> ports = new ArrayList<>(portFunctionsMap.keySet());
            portTableModel.addAll(ports);
            if (!ports.isEmpty()) {
                functionTableModel.addAll(portFunctionsMap.get(ports.get(0)));
                webServiceUI.selectFirstFunction();
            }
        } catch (InvocationTargetException e) {
            webServiceUI.setErrorMessage("Error getting service description: " + e.getCause().getMessage());
            webServiceUI.setPageComplete(false);
        } catch (InterruptedException e) {
            // ignore e.
        }

    }

    public IStatus performFinishWithFunction(Function function) {
        if (function == null) {
            // select default function to get other port infos from it.
            function = functionTableModel.getBeansList().get(0);
        }
        currentSetting.setFunction(function);
        if (!currentSetting.isUpdated()) {
            return Status.OK_STATUS;
        }
        IStatus status = nodeAdapter.setNodeSetting(currentSetting);
        if (status.isOK() && webServiceUI.needPopulateSchema()) {
            status = populateSchema();
        }
        return status;
    }

    private IStatus populateSchema() {
        if (currentSetting.isUpdated()) {
            return SchemaTool.populateSchema(webServiceUI, currentSetting.getDefinition());
        }
        return Status.OK_STATUS;
    }

    public String getInitialWsdlLocation() {
        return nodeAdapter.getInitialWsdlLocation();
    }

    public boolean isFunctionRequired() {
        return nodeAdapter.isServiceOperationRequired();
    }

    public boolean allowPopulateSchema() {
        if (WebServiceComponentPlugin.hasRepositoryServices()) {
            return nodeAdapter.allowPopulateSchema();
        }
        return false;
    }

    public boolean showResourcesButton() {
        return nodeAdapter.routeResourcesAvailable();
    }

    public void resourceNodeSelected(IRepositoryViewObject resourceNode) {
        currentSetting.setResourceNode(resourceNode);
    }

    @Override
    public void functionSelected(Function function) {
        webServiceUI.setPageComplete(true);
    }
}
