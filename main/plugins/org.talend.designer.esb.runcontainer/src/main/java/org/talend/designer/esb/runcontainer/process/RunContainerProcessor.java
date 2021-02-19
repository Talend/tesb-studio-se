// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.esb.runcontainer.process;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.PlatformUI;
import org.talend.camel.designer.ui.wizards.actions.JavaCamelJobScriptsExportWSAction;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryObject;
import org.talend.core.model.repository.RepositoryObject;
import org.talend.core.repository.seeker.RepositorySeekerManager;
import org.talend.core.runtime.process.TalendProcessOptionConstants;
import org.talend.designer.esb.runcontainer.i18n.RunContainerMessages;
import org.talend.designer.esb.runcontainer.server.RuntimeServerController;
import org.talend.designer.esb.runcontainer.ui.progress.CheckingBundlesProgress;
import org.talend.designer.esb.runcontainer.ui.progress.StartRuntimeProgress;
import org.talend.designer.runprocess.IProcessMessageManager;
import org.talend.designer.runprocess.ProcessMessage;
import org.talend.designer.runprocess.ProcessMessage.MsgType;
import org.talend.designer.runprocess.ProcessorException;
import org.talend.designer.runprocess.maven.MavenJavaProcessor;

public class RunContainerProcessor extends MavenJavaProcessor {

    public RunContainerProcessor(IProcess process, Property property, boolean filenameFromLabel) {
        super(process, property, filenameFromLabel);
        this.windowsClasspath = "";
        this.unixClasspath = "";
    }

    @Override
    public Process run(int statisticsPort, int tracePort, String watchParam, String log4jLevel, IProgressMonitor monitor,
            IProcessMessageManager processMessageManager) throws ProcessorException {
    	processMessageManager.addDebugResultToConsole(new ProcessMessage(MsgType.LOG4J_WARN, RunContainerMessages.getString("StartRuntimeAction.Deprecated")));
        if (!RuntimeServerController.getInstance().isRunning()) {
            try {
                new StartRuntimeProgress(true).run(monitor);
                new CheckingBundlesProgress().run(monitor);
            } catch (Exception e) {
                throw new ProcessorException(RunContainerMessages.getString("StartRuntimeAction.ErrorStart"), e);
            }
        }
        if (RuntimeServerController.getInstance().isRunning()) {

            RunESBRuntimeProcess esbRunContainerProcess = new RunESBRuntimeProcess(process, statisticsPort, tracePort, monitor);
            esbRunContainerProcess.setMessageManager(processMessageManager);
            try {
                esbRunContainerProcess.start();
            } catch (Exception e) {
                throw new ProcessorException(e);
            }

            return esbRunContainerProcess;
        } else {
            throw new ProcessorException(RunContainerMessages.getString("StartRuntimeAction.ErrorStart"));
        }
    }

    @Override
    public String getProcessorType() {
        return "runtimeProcessor";
    }

    @Override
    protected boolean packagingAndAssembly() {
        return ERepositoryObjectType.PROCESS_ROUTE.equals(ERepositoryObjectType.getType(getProperty())) ? true : false;
    }

    @Override
    public void generatePom(int option) {
        super.generatePom(option);

        if (option == TalendProcessOptionConstants.GENERATE_IS_MAINJOB
                && ComponentCategory.CATEGORY_4_CAMEL.getName().equals(getProcess().getComponentsType())) {
            try {
                IRepositoryObject repositoryObject = new RepositoryObject(getProperty());

                // Fix TESB-22660: Avoide to operate repo viewer before it open
                if (PlatformUI.isWorkbenchRunning()) {
                    RepositorySeekerManager.getInstance().searchRepoViewNode(getProperty().getId(), false);
                }

                IRunnableWithProgress action = new JavaCamelJobScriptsExportWSAction(repositoryObject, getProperty().getVersion(),
                        "", false);
                action.run(new NullProgressMonitor());
            } catch (Exception e) {
                ExceptionHandler.process(e);
            }
        }
    }
}
