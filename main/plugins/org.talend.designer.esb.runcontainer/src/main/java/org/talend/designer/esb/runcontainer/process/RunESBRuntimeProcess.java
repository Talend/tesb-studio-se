// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.talend.camel.designer.ui.wizards.actions.JavaCamelJobScriptsExportWSAction;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.process.IContext;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.process.ProcessUtils;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.repository.constants.FileConstants;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.runtime.process.IBuildJobHandler;
import org.talend.core.runtime.repository.build.AbstractBuildProvider;
import org.talend.core.runtime.repository.build.BuildExportManager;
import org.talend.core.runtime.repository.build.IBuildJobParameters;
import org.talend.core.runtime.repository.build.IBuildParametes;
import org.talend.designer.esb.runcontainer.export.JobJavaScriptOSGIForESBRuntimeManager;
import org.talend.designer.esb.runcontainer.logs.FelixLogsModel;
import org.talend.designer.esb.runcontainer.logs.RuntimeLogHTTPAdapter;
import org.talend.designer.esb.runcontainer.logs.RuntimeLogHTTPMonitor;
import org.talend.designer.esb.runcontainer.util.JMXUtil;
import org.talend.designer.runprocess.IProcessMessageManager;
import org.talend.designer.runprocess.ui.ProcessManager;
import org.talend.repository.model.IRepositoryNode.ENodeType;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.services.model.services.ServiceItem;
import org.talend.repository.ui.wizards.exportjob.action.JobExportAction;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.JobScriptsManager;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.JobScriptsManager.ExportChoice;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.JobScriptsManagerFactory;
import org.talend.repository.utils.EmfModelUtils;

public class RunESBRuntimeProcess extends Process {

    public static final String INFO = "INFO";

    private static final String LINE_SEPARATOR = System.lineSeparator();

    private PipedOutputStream stdOutputStream;

    private PipedInputStream errInputStream;

    private PipedInputStream stdInputStream;

    private PipedOutputStream errOutputStream;

    private RuntimeLogHTTPAdapter logListener;

    private boolean startLogging;

    private IProcess process;

    private int statisticsPort;

    private int tracePort;

    private IProgressMonitor monitor;

    private ArtifactDeployManager artifactManager;

    private int exitValue = 0;

    @SuppressWarnings("unused")
    private IProcessMessageManager processMessageManager;

    public RunESBRuntimeProcess(IProcess process, int statisticsPort, int tracePort, IProgressMonitor monitor) {
        this.process = process;
        this.statisticsPort = statisticsPort;
        this.tracePort = tracePort;
        this.monitor = monitor;

        stdInputStream = new PipedInputStream();
        errInputStream = new PipedInputStream();
        // runtimeLogQueue = new LinkedBlockingQueue<FelixLogsModel>(10);
        try {
            stdOutputStream = new PipedOutputStream(stdInputStream);
            errOutputStream = new PipedOutputStream(errInputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        logListener = new RuntimeLogHTTPAdapter() {

            @Override
            public synchronized void logReceived(FelixLogsModel log) {
                if (startLogging) {
                    try {
                        if (INFO.equals(log.getLevel())) {
                            stdOutputStream.write(log.toString().getBytes());
                            stdOutputStream.write(LINE_SEPARATOR.getBytes());
                            stdOutputStream.flush();
                        } else {
                            errOutputStream.write(log.toString().getBytes());
                            errOutputStream.write(LINE_SEPARATOR.getBytes());
                            errOutputStream.flush();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void logReceived(String logs, boolean isError) {
                try {
                    if (isError) {
                        errOutputStream.write(logs.getBytes());
                        errOutputStream.write(LINE_SEPARATOR.getBytes());
                        errOutputStream.flush();
                    } else {
                        stdOutputStream.write(logs.getBytes());
                        stdOutputStream.write(LINE_SEPARATOR.getBytes());
                        stdOutputStream.flush();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        artifactManager = new ArtifactDeployManager();
    }

    public void setMessageManager(IProcessMessageManager processMessageManager) {
        this.processMessageManager = processMessageManager;
    }

    /**
     * Start tracing server logs
     * 
     * @throws Exception
     */
    public void start() throws Exception {
        RuntimeLogHTTPMonitor logMonitor = RuntimeLogHTTPMonitor.createRuntimeLogHTTPMonitor();
        logMonitor.startLogging();
        logMonitor.addLogLictener(logListener);
        startLogging = true;

        artifactManager.deploy();

    }

    public void stopLogging() {
        RuntimeLogHTTPMonitor.createRuntimeLogHTTPMonitor().removeLogLictener(logListener);
        startLogging = false;
    }

    @Override
    public OutputStream getOutputStream() {
        return stdOutputStream;
    }

    @Override
    public InputStream getInputStream() {
        return stdInputStream;
    }

    @Override
    public InputStream getErrorStream() {
        return errInputStream;
    }

    public PipedOutputStream getErrOutputStream() {
        return errOutputStream;
    }

    @Override
    public int waitFor() throws InterruptedException {
        return 0;
    }

    @Override
    public int exitValue() {
        return exitValue;
    }

    @Override
    public void destroy() {
        stopLogging();
        try {
            artifactManager.unDeploy();
        } catch (Exception e) {
            e.printStackTrace();
            exitValue = 0;
        }
        try {
            stdInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            stdOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    final class ArtifactDeployManager {

        protected String[] kars;

        protected long[] bundles;

        public void deploy() throws Exception {
            File target = null;
            IRepositoryViewObject viewObject = findJob(process.getId());
            RepositoryNode node = new RepositoryNode(viewObject, null, ENodeType.REPOSITORY_ELEMENT);
            ProcessItem processItem = (ProcessItem) node.getObject().getProperty().getItem();
            String configID = node.getObject().getLabel();

            monitor.setTaskName("Deploy articact into Runimte server");
            if (ComponentCategory.CATEGORY_4_DI.getName().equals(process.getComponentsType())) {
                // publish service
                if (EmfModelUtils.getComponentByName(processItem, "tESBProviderRequest") != null) {
                    List<Item> items = new ArrayList<Item>(1);
                    items.add(processItem);
                    Collection<IRepositoryViewObject> allDependencies =
                            ProcessUtils.getProcessDependencies(ERepositoryObjectType.METADATA, items, false);
                    // check service
                    if (!allDependencies.isEmpty()) {
                        for (IRepositoryViewObject object : allDependencies) {
                            if (object.getProperty().getItem() != null
                                    && object.getProperty().getItem() instanceof ServiceItem) {
                                ServiceItem serviceItem = (ServiceItem) object.getProperty().getItem();

                                Map<ExportChoice, Object> exportChoiceMap =
                                        new EnumMap<ExportChoice, Object>(ExportChoice.class);

                                exportChoiceMap.put(ExportChoice.needLauncher, true);
                                exportChoiceMap.put(ExportChoice.needSystemRoutine, true);
                                exportChoiceMap.put(ExportChoice.needUserRoutine, true);
                                exportChoiceMap.put(ExportChoice.needTalendLibraries, true);
                                exportChoiceMap.put(ExportChoice.needJobItem, true);
                                exportChoiceMap.put(ExportChoice.needJobScript, true);
                                exportChoiceMap.put(ExportChoice.doNotCompileCode, false);
                                exportChoiceMap.put(ExportChoice.needDependencies, false);
                                exportChoiceMap.put(ExportChoice.addStatistics, false);
                                exportChoiceMap.put(ExportChoice.addTracs, false);
                                exportChoiceMap.put(ExportChoice.needAntScript, false);
                                exportChoiceMap.put(ExportChoice.needMavenScript, false);
                                exportChoiceMap.put(ExportChoice.applyToChildren, false);
                                exportChoiceMap.put(ExportChoice.needContext, true);
                                exportChoiceMap.put(ExportChoice.binaries, true);
                                exportChoiceMap.put(ExportChoice.needSourceCode, false);
                                exportChoiceMap.put(ExportChoice.executeTests, false);
                                exportChoiceMap.put(ExportChoice.includeTestSource, false);
                                exportChoiceMap.put(ExportChoice.includeLibs, true);
                                exportChoiceMap.put(ExportChoice.needLog4jLevel, false);
                                exportChoiceMap.put(ExportChoice.needAssembly, true);

                                Map<String, Object> parameters = new HashMap<String, Object>();
                                parameters.put(IBuildParametes.ITEM, serviceItem);
                                parameters.put(IBuildParametes.VERSION, serviceItem.getProperty().getVersion());
                                parameters.put(IBuildJobParameters.CONTEXT_GROUP, IContext.DEFAULT);
                                parameters.put(IBuildJobParameters.CHOICE_OPTION, exportChoiceMap);

                                AbstractBuildProvider buildProvider =
                                        BuildExportManager.getInstance().getBuildProvider("Service", parameters);

                                IBuildJobHandler buildServiceHandler =
                                        (IBuildJobHandler) buildProvider.createBuildExportHandler(parameters);
                                buildServiceHandler.prepare(monitor, parameters);
                                buildServiceHandler.build(monitor);
                                applyContextConfiguration(configID);
                                kars = JMXUtil
                                        .installKar(buildServiceHandler.getJobTargetFile().getLocation().toFile());

                            }
                        }
                    }
                } else {
                    // publish job
                    target = File.createTempFile("job", FileConstants.JAR_FILE_SUFFIX, null);
                    JobScriptsManager jobScriptsManager = new JobJavaScriptOSGIForESBRuntimeManager(
                            JobScriptsManagerFactory.getDefaultExportChoiceMap(),
                            processItem.getProcess().getDefaultContext(), JobScriptsManager.LAUNCHER_ALL,
                            statisticsPort, tracePort);
                    // generate
                    jobScriptsManager.setDestinationPath(target.getAbsolutePath());
                    JobExportAction jobAction = new JobExportAction(Collections.singletonList(node),
                            node.getObject().getProperty().getVersion(), node.getObject().getProperty().getVersion(),
                            jobScriptsManager, System.getProperty("java.io.tmpdir"));

                    jobAction.run(monitor);
                    if (jobAction.isBuildSuccessful()) {
                        applyContextConfiguration(configID);
                        bundles = JMXUtil.installBundle(target);
                        // writeLog(processMessageManager, new ProcessMessage(MsgType.STD_OUT,
                        // "Install bundle, return value: "
                        // + Arrays.toString(bundles) + ".\n"));
                    }
                }
            } else if (ComponentCategory.CATEGORY_4_CAMEL.getName().equals(process.getComponentsType())) {
                // publish route
                target = File.createTempFile("route", FileConstants.KAR_FILE_SUFFIX, null);
                JavaCamelJobScriptsExportWSAction camelAction = new JavaCamelJobScriptsExportWSAction(node,
                        process.getVersion(), target.getAbsolutePath(), true, statisticsPort, tracePort);
                camelAction.setBuildProject(true);
                camelAction.run(monitor);
                applyContextConfiguration(configID);
                kars = JMXUtil.installKar(target);
                // writeLog(processMessageManager,
                // new ProcessMessage(MsgType.STD_OUT, "Install kar, return value: " + Arrays.toString(kars) + ".\n"));
            }
            if (target != null && target.exists()) {
                target.delete();
            }
        }

        public void unDeploy() throws Exception {
            if (bundles != null && bundles.length > 0) {
                for (long bundle : bundles) {
                    JMXUtil.uninstallBundle(bundle);
                }
            }
            if (kars != null && kars.length > 0) {
                for (String kar : kars) {
                    JMXUtil.uninstallKar(kar);
                }
            }
        }

        private IRepositoryViewObject findJob(String jobID) throws PersistenceException {
            ProxyRepositoryFactory proxyRepositoryFactory = ProxyRepositoryFactory.getInstance();
            return proxyRepositoryFactory.getLastVersion(jobID);
        }
    }

    private void applyContextConfiguration(String configID) throws Exception {
        ProcessManager processManager = ProcessManager.getInstance();
        IContext context = processManager.getSelectContext();
        String contextName = context.getName();
        JMXUtil.deleteConfigProperties(configID);
        JMXUtil.setConfigProperty(configID, "context", contextName);
        /*
         * // The following code is only required if context parameters are // to be modified dynamically at deployment
         * into local runtime. // Such functionaliy is currently not implemented in Talend Studio, // but contexts are
         * applied as generated. List<org.talend.core.model.process.IContextParameter> params =
         * context.getContextParameterList(); if (params != null) { for (org.talend.core.model.process.IContextParameter
         * param : params) { String name = param.getName(); if ("context".equals(name)) { continue; }
         * JMXUtil.setConfigProperty(configID, name, param.getValue()); } }
         */
    }
}
