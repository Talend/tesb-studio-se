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
package org.talend.repository.services.action;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.wsdl.Binding;
import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.wsdl.Fault;
import javax.wsdl.Input;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.Output;
import javax.wsdl.Part;
import javax.xml.namespace.QName;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.utils.workbench.resources.ResourceUtils;
import org.talend.core.model.general.Project;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.XmlFileConnectionItem;
import org.talend.cwm.helper.ConnectionHelper;
import org.talend.designer.core.DesignerPlugin;
import org.talend.metadata.managment.ui.utils.XsdMetadataUtils;
import org.talend.repository.ProjectManager;
import org.talend.repository.services.ServicesPlugin;
import org.talend.repository.services.Messages;
import org.talend.repository.services.ui.RewriteSchemaDialog;
import org.talend.repository.services.ui.preferences.EsbSoapServicePreferencePage;
import org.talend.repository.services.utils.FolderNameUtil;
import org.talend.repository.services.utils.WSDLPopulationUtil;
import org.talend.repository.services.utils.WSDLUtils;
import org.talend.utils.wsdl.WSDLLoader;

public class PublishMetadataRunnable implements IRunnableWithProgress {

    private final Definition wsdlDefinition;

    private final Shell shell;

    private WSDLPopulationUtil populationUtil;

    private IProject fsProject;

    public PublishMetadataRunnable(Definition wsdlDefinition, Shell shell) {
        this.wsdlDefinition = wsdlDefinition;
        this.shell = shell;

        Project project = ProjectManager.getInstance().getCurrentProject();
        try {
            fsProject = ResourceUtils.getProject(project);
        } catch (PersistenceException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        final IWorkspaceRunnable op = new IWorkspaceRunnable() {

            @Override
            public void run(IProgressMonitor monitor) throws CoreException {
                monitor.beginTask(Messages.PublishMetadataAction_Importing, 3);

                final Collection<XmlFileConnectionItem> xmlObjs;
                try {
                    xmlObjs = initFileConnection();
                } catch (Exception e) {
                    String message = (null != e.getMessage()) ? e.getMessage() : e.getClass().getName();
                    throw new CoreException(new Status(IStatus.ERROR, ServicesPlugin.getDefault().getBundle().getSymbolicName(),
                            "Can't retrieve schemas from metadata: " + message, e));
                }
                Collection<XmlFileConnectionItem> selectTables;
                if (xmlObjs.size() > 0) {
                    RewriteSchemaDialogRunnable runnable = new RewriteSchemaDialogRunnable(shell, xmlObjs);
                    Display.getDefault().syncExec(runnable);
                    selectTables = runnable.getSelectTables();
                    if (null == selectTables) {
                        return;
                    }
                } else {
                    selectTables = Collections.emptyList();
                }
                monitor.worked(1);
                if (monitor.isCanceled()) {
                    return;
                }

                boolean validateWsdl = ServicesPlugin.getDefault().getPreferenceStore()
                        .getBoolean(EsbSoapServicePreferencePage.ENABLE_WSDL_VALIDATION);
                if (validateWsdl) {
                    WSDLUtils.validateWsdl(wsdlDefinition.getDocumentBaseURI());
                }
                monitor.worked(1);
                if (monitor.isCanceled()) {
                    return;
                }

                try {
                    process(wsdlDefinition, selectTables);
                } catch (Exception e) {
                    throw new CoreException(new Status(IStatus.ERROR, ServicesPlugin.PLUGIN_ID, "Error during schema processing", e));
                }
                monitor.done();

            }

        };

        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        try {
            ISchedulingRule schedulingRule = workspace.getRoot();
            // the update the project files need to be done in the workspace runnable to avoid all
            // notification
            // of changes before the end of the modifications.
            workspace.run(op, schedulingRule, IWorkspace.AVOID_UPDATE, monitor);
        } catch (CoreException e) {
            throw new InvocationTargetException(e);
        }

    }

    private static class RewriteSchemaDialogRunnable implements Runnable {

        private final Shell shell;

        private final Collection<XmlFileConnectionItem> xmlObjs;

        private Collection<XmlFileConnectionItem> selectTables;

        public RewriteSchemaDialogRunnable(Shell shell, Collection<XmlFileConnectionItem> xmlObjs) {
            this.shell = shell;
            this.xmlObjs = xmlObjs;
        }

        @Override
        public void run() {
            RewriteSchemaDialog selectContextDialog = new RewriteSchemaDialog(shell, xmlObjs);
            if (selectContextDialog.open() == Window.OK) {
                selectTables = selectContextDialog.getSelectionTables();
            } else {
                return;
            }
        }

        public Collection<XmlFileConnectionItem> getSelectTables() {
            return selectTables;
        }

    }

    private Collection<XmlFileConnectionItem> initFileConnection() throws URISyntaxException, PersistenceException {
        Collection<String> paths = getAllPaths();
        Collection<XmlFileConnectionItem> connItems = new ArrayList<XmlFileConnectionItem>();

        for (ConnectionItem item : DesignerPlugin.getDefault().getProxyRepositoryFactory().getMetadataConnectionsItem()) {
            if (item instanceof XmlFileConnectionItem && paths.contains(item.getState().getPath())
                    && !ConnectionHelper.getTables(item.getConnection()).isEmpty()) {
                connItems.add((XmlFileConnectionItem) item);
            }
        }
        return connItems;
    }

    @SuppressWarnings("unchecked")
    private Collection<String> getAllPaths() throws URISyntaxException {
        final Set<String> paths = new HashSet<String>();
        final Set<QName> portTypes = new HashSet<QName>();
        final Set<QName> alreadyCreated = new HashSet<QName>();
        for (Binding binding : (Collection<Binding>) wsdlDefinition.getAllBindings().values()) {
            final QName portType = binding.getPortType().getQName();
            if (portTypes.add(portType)) {
                for (BindingOperation operation : (Collection<BindingOperation>) binding.getBindingOperations()) {
                    Operation oper = operation.getOperation();
                    Input inDef = oper.getInput();
                    if (inDef != null) {
                        Message inMsg = inDef.getMessage();
                        addParamsToPath(portType, oper, inMsg, paths, alreadyCreated);
                    }

                    Output outDef = oper.getOutput();
                    if (outDef != null) {
                        Message outMsg = outDef.getMessage();
                        addParamsToPath(portType, oper, outMsg, paths, alreadyCreated);
                    }
                    for (Fault fault : (Collection<Fault>) oper.getFaults().values()) {
                        Message faultMsg = fault.getMessage();
                        addParamsToPath(portType, oper, faultMsg, paths, alreadyCreated);
                    }
                }
            }
        }
        return paths;
    }

    private static void addParamsToPath(final QName portType, Operation oper, Message msg, final Set<String> paths,
            final Set<QName> alreadyCreated) throws URISyntaxException {
        if (msg != null) {
            List<QName> messageParts = getMessageParts(msg);
            if (messageParts.isEmpty()) {
                return;
            }
            for(QName messagePart : messageParts) {
                if (alreadyCreated.add(messagePart)) {
                    String folderPath = FolderNameUtil.getImportedXmlSchemaPath(messagePart.getNamespaceURI(),
                            portType.getLocalPart(), oper.getName());
                    paths.add(folderPath);
                }
            }
        }
    }

    private static List<QName> getMessageParts(Message msg) {
        List<QName> result = new ArrayList<QName>();
        @SuppressWarnings("unchecked")
        Collection<Part> values = msg.getParts().values();
        if (values == null || values.isEmpty()) {
            return result;
        }
        Iterator<Part> iterator = values.iterator();
        while (iterator.hasNext()) {
            Part part = iterator.next();
            if (part.getElementName() != null) {
                result.add(part.getElementName());
            } else if (part.getTypeName() != null) {
                result.add(part.getTypeName());
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private void process(Definition wsdlDefinition, Collection<XmlFileConnectionItem> selectTables) throws Exception,
            CoreException {
        List<IFile> tempFiles = new ArrayList<IFile>();
        try {
            File wsdlFile = null;
            String baseUri = wsdlDefinition.getDocumentBaseURI();
            URI uri = new URI(baseUri);
            if ("file".equals(uri.getScheme())) {
                wsdlFile = new File(uri.toURL().getFile());
            } else {
                Map<String, InputStream> load = new WSDLLoader().load(baseUri, "tempWsdl" + "%d.wsdl");
                InputStream inputStream = load.remove(WSDLLoader.DEFAULT_FILENAME);
                String name = File.createTempFile("tESBConsumer", ".wsdl").getName();
                IFile tempWsdlFile = createTempFile(name, inputStream);
                tempFiles.add(tempWsdlFile);
                wsdlFile = new File(tempWsdlFile.getLocation().toPortableString());

                // TESB-19040:save import wsdl files
                if (!load.isEmpty()) {
                    for (Map.Entry<String, InputStream> importWsdl : load.entrySet()) {
                        tempFiles.add(createTempFile(importWsdl.getKey(), importWsdl.getValue()));
                    }
                }
            }
            if (populationUtil == null) {
                populationUtil = new WSDLPopulationUtil();
                populationUtil.loadWSDL("file://" + wsdlFile.getAbsolutePath());
            }

            final Set<QName> portTypes = new HashSet<QName>();
            final Set<QName> alreadyCreated = new HashSet<QName>();
            for (Binding binding : (Collection<Binding>) wsdlDefinition.getAllBindings().values()) {
                final QName portType = binding.getPortType().getQName();
                if (portTypes.add(portType)) {
                    for (BindingOperation operation : (Collection<BindingOperation>) binding.getBindingOperations()) {
                        Operation oper = operation.getOperation();
                        Input inDef = oper.getInput();
                        if (inDef != null) {
                            Message inMsg = inDef.getMessage();
                            if (inMsg != null) {
                                // fix for TDI-20699
                                List<QName> messageParts = getMessageParts(inMsg);
                                if (messageParts.isEmpty()) {
                                    continue;
                                }
                                for (QName messagePart : messageParts) {
                                    if (alreadyCreated.add(messagePart)) {
                                        XsdMetadataUtils.createMetadataFromXSD(messagePart, portType.getLocalPart(),
                                                oper.getName(), selectTables, wsdlFile, populationUtil);
                                    }
                                }
                            }
                        }

                        Output outDef = oper.getOutput();
                        if (outDef != null) {
                            Message outMsg = outDef.getMessage();
                            if (outMsg != null) {
                                List<QName> messageParts = getMessageParts(outMsg);
                                if (messageParts.isEmpty()) {
                                    continue;
                                }
                                for (QName messagePart : messageParts) {
                                    if (alreadyCreated.add(messagePart)) {
                                        XsdMetadataUtils.createMetadataFromXSD(messagePart, portType.getLocalPart(),
                                                oper.getName(), selectTables, wsdlFile, populationUtil);
                                    }
                                }
                            }
                        }
                        for (Fault fault : (Collection<Fault>) oper.getFaults().values()) {
                            Message faultMsg = fault.getMessage();
                            if (faultMsg != null) {
                                List<QName> messageParts = getMessageParts(faultMsg);
                                if (messageParts.isEmpty()) {
                                    continue;
                                }
                                for (QName messagePart : messageParts) {
                                    if (alreadyCreated.add(messagePart)) {
                                        XsdMetadataUtils.createMetadataFromXSD(messagePart, portType.getLocalPart(),
                                                oper.getName(), selectTables, wsdlFile, populationUtil);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw e;
        } finally {
            for (IFile tempFile : tempFiles) {
                tempFile.delete(true, null);
            }
        }
    }

    private IFile createTempFile(String fileName, InputStream inputStream) throws PersistenceException, IOException,
            CoreException {
        IPath path = new Path("temp");
        path = path.append(fileName);
        IFile file = fsProject.getFile(path);
        file.create(inputStream, false, new NullProgressMonitor());
        return file;
    }

}
