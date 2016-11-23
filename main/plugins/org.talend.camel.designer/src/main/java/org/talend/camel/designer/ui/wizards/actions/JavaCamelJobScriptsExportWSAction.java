// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.camel.designer.ui.wizards.actions;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.talend.camel.designer.ui.wizards.export.RouteJavaScriptOSGIForESBManager;
import org.talend.camel.designer.util.CamelFeatureUtil;
import org.talend.camel.model.CamelRepositoryNodeType;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.utils.io.FilesUtils;
import org.talend.core.model.general.Project;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.relationship.RelationshipItemBuilder;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.repository.constants.FileConstants;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.publish.core.models.BundleModel;
import org.talend.designer.publish.core.models.FeaturesModel;
import org.talend.designer.publish.core.utils.ZipModel;
import org.talend.designer.runprocess.IProcessor;
import org.talend.repository.ProjectManager;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.IRepositoryNode.ENodeType;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.ui.wizards.exportjob.action.JobExportAction;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.JobScriptsManager;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.JobScriptsManager.ExportChoice;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.esb.JobJavaScriptOSGIForESBManager;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.esb.RouteUsedJobManager;
import org.talend.repository.utils.EmfModelUtils;
import org.talend.repository.utils.JobContextUtils;

public class JavaCamelJobScriptsExportWSAction implements IRunnableWithProgress {

    private IProgressMonitor monitor;

    protected final IRepositoryNode routeNode;

    protected final String version;

    protected String bundleVersion;

    protected String destinationKar;

    private final JobScriptsManager manager;

    private FeaturesModel featuresModel;

    private final boolean addStatisticsCode;

    public JavaCamelJobScriptsExportWSAction(IRepositoryNode routeNode, String version, String destinationKar,
            boolean addStatisticsCode) {
        this.routeNode = routeNode;
        this.version = version;
        this.bundleVersion = version;
        this.destinationKar = destinationKar;
        this.addStatisticsCode = addStatisticsCode;

        manager = new JobJavaScriptOSGIForESBManager(getExportChoice(), null, null, IProcessor.NO_STATISTICS,
                IProcessor.NO_TRACES);
        manager.setBundleVersion(version);
    }

    public JavaCamelJobScriptsExportWSAction(Map<ExportChoice, Object> exportChoiceMap, IRepositoryNode routeNode, String version,
            String destinationKar, boolean addStatisticsCode) {
        this.routeNode = routeNode;
        this.version = version;
        this.bundleVersion = version;
        this.destinationKar = destinationKar;
        this.addStatisticsCode = addStatisticsCode;

        exportChoiceMap.putAll(getExportChoice());
        manager = new JobJavaScriptOSGIForESBManager(exportChoiceMap, null, null, IProcessor.NO_STATISTICS, IProcessor.NO_TRACES);
        manager.setBundleVersion(version);
    }

    public JavaCamelJobScriptsExportWSAction(IRepositoryNode routeNode, String version, String bundleVersion) {
        this(routeNode, version, null, false);
        this.bundleVersion = bundleVersion;
    }

    private Map<ExportChoice, Object> getExportChoice() {
        Map<ExportChoice, Object> exportChoiceMap = new EnumMap<>(ExportChoice.class);
        exportChoiceMap.put(ExportChoice.needJobItem, false);
        exportChoiceMap.put(ExportChoice.needSourceCode, false);
        exportChoiceMap.put(ExportChoice.needMetaInfo, true);
        exportChoiceMap.put(ExportChoice.needContext, true);
        exportChoiceMap.put(ExportChoice.addStatistics, addStatisticsCode);
        return exportChoiceMap;
    }

    public JobScriptsManager getManager() {
        return manager;
    }

    protected String getGroupId() {
        return CamelFeatureUtil.getMavenGroupId(routeNode.getObject().getProperty().getItem());
    }

    protected String getArtifactId() {
        return routeNode.getObject().getProperty().getDisplayName();
    }

    protected String getArtifactVersion() {
        return version;
    }

    @Override
    public final void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        this.monitor = monitor;

        featuresModel = new FeaturesModel(getGroupId(), getArtifactId(), getArtifactVersion());
        try {
            // generated bundle jar first
            String routeName = routeNode.getObject().getProperty().getDisplayName();
            File routeFile;
            try {
                routeFile = File.createTempFile("route", FileConstants.JAR_FILE_SUFFIX, new File(getTempDir())); //$NON-NLS-1$
            } catch (IOException e) {
                throw new InvocationTargetException(e);
            }

            BundleModel routeModel = new BundleModel(getGroupId(), routeName + "-bundle", getArtifactVersion(), routeFile);
            if (featuresModel.addBundle(routeModel)) {
                final ProcessItem routeProcess = (ProcessItem) routeNode.getObject().getProperty().getItem();
                CamelFeatureUtil.addFeatureAndBundles(routeProcess, featuresModel);
                featuresModel.setConfigName(routeNode.getObject().getLabel());
                featuresModel.setContexts(JobContextUtils.getContextsMap(routeProcess));

                exportAllReferenceJobs(routeProcess);
                final Collection<String> routelets = new HashSet<>();
                exportAllReferenceRoutelets(routeProcess, routelets);

                exportRouteBundle(routeNode, routeFile, version, bundleVersion, routelets, null);
            }

            processResults(featuresModel, monitor);

        } finally {
            // remove generated files
            FilesUtils.removeFolder(getTempDir(), true);
        }
    }

    protected void processResults(FeaturesModel featuresModel, IProgressMonitor monitor)
            throws InvocationTargetException, InterruptedException {
        // create kar file
        try {
            new ZipModel(featuresModel, new File(destinationKar));
        } catch (IOException e) {
            throw new InvocationTargetException(e);
        }
    }

    private void exportAllReferenceJobs(ProcessItem routeProcess) throws InvocationTargetException, InterruptedException {
        for (NodeType cTalendJob : EmfModelUtils.getComponentsByName(routeProcess, "cTalendJob")) { //$NON-NLS-1$
            String jobId = null;
            String jobVersion = null;
            String jobContext = null;
            for (Object o : cTalendJob.getElementParameter()) {
                if (!(o instanceof ElementParameterType)) {
                    continue;
                }
                ElementParameterType ept = (ElementParameterType) o;
                String eptName = ept.getName();
                if ("FROM_EXTERNAL_JAR".equals(eptName) && "true".equals(ept.getValue())) { //$NON-NLS-1$
                    break;
                }
                if ("SELECTED_JOB_NAME:PROCESS_TYPE_PROCESS".equals(eptName)) { //$NON-NLS-1$
                    jobId = ept.getValue();
                } else if ("SELECTED_JOB_NAME:PROCESS_TYPE_VERSION".equals(eptName)) { //$NON-NLS-1$
                    jobVersion = ept.getValue();
                } else if ("SELECTED_JOB_NAME:PROCESS_TYPE_CONTEXT".equals(eptName)) { //$NON-NLS-1$
                    jobContext = ept.getValue();
                }
            }

            if (jobId == null || jobVersion == null) {
                continue;
            }
            IRepositoryNode referencedJobNode;
            try {
                referencedJobNode = getJobRepositoryNode(jobId, ERepositoryObjectType.PROCESS);
            } catch (PersistenceException e) {
                throw new InvocationTargetException(e);
            }
            if (RelationshipItemBuilder.LATEST_VERSION.equals(jobVersion)) {
                jobVersion = referencedJobNode.getObject().getVersion();
            }

            String jobName = referencedJobNode.getObject().getProperty().getDisplayName();
            File jobFile;
            try {
                jobFile = File.createTempFile("job", FileConstants.JAR_FILE_SUFFIX, new File(getTempDir())); //$NON-NLS-1$
            } catch (IOException e) {
                throw new InvocationTargetException(e);
            }
            if (getArtifactVersion().endsWith("-SNAPSHOT")) {
            }
            String routeName = routeNode.getObject().getProperty().getDisplayName();
            BundleModel jobModel = new BundleModel(getGroupId() + '.' + routeName, jobName, getArtifactVersion(), jobFile);
            if (featuresModel.addBundle(jobModel)) {
                exportRouteUsedJobBundle(referencedJobNode, jobFile, jobVersion, jobName, jobVersion,
                        routeNode.getObject().getProperty().getDisplayName(), version, jobContext);
            }
        }
    }

    private void exportAllReferenceRoutelets(ProcessItem routeProcess, Collection<String> routelets)
            throws InvocationTargetException, InterruptedException {
        for (NodeType node : (Collection<NodeType>) routeProcess.getProcess().getNode()) {
            if (!EmfModelUtils.isComponentActive(node)) {
                continue;
            }
            final ElementParameterType routeletId = EmfModelUtils.findElementParameterByName(
                    EParameterName.PROCESS_TYPE.getName() + ':' + EParameterName.PROCESS_TYPE_PROCESS.getName(), node);
            if (null != routeletId) {
                final IRepositoryNode referencedRouteletNode;
                try {
                    referencedRouteletNode = getJobRepositoryNode(routeletId.getValue(),
                            CamelRepositoryNodeType.repositoryRouteletType);// getRouteletRepositoryNode(routeletId);
                } catch (PersistenceException e) {
                    throw new InvocationTargetException(e);
                }

                final ProcessItem routeletProcess = (ProcessItem) referencedRouteletNode.getObject().getProperty().getItem();
                final String className = RouteJavaScriptOSGIForESBManager.getClassName(routeletProcess);
                if (!routelets.add(className)) {
                    continue;
                }

                String routeletVersion = EmfModelUtils
                        .findElementParameterByName(
                                EParameterName.PROCESS_TYPE.getName() + ':' + EParameterName.PROCESS_TYPE_VERSION.getName(), node)
                        .getValue();
                if (RelationshipItemBuilder.LATEST_VERSION.equals(routeletVersion)) {
                    routeletVersion = referencedRouteletNode.getObject().getVersion();
                }

                final File routeletFile;
                try {
                    routeletFile = File.createTempFile("routelet", FileConstants.JAR_FILE_SUFFIX, new File(getTempDir())); //$NON-NLS-1$
                } catch (IOException e) {
                    throw new InvocationTargetException(e);
                }
                BundleModel jobModel = new BundleModel(getGroupId(), referencedRouteletNode.getObject().getLabel(),
                        getArtifactVersion(), routeletFile);
                if (featuresModel.addBundle(jobModel)) {
                    exportRouteBundle(referencedRouteletNode, routeletFile, routeletVersion, routeletVersion, null,
                            EmfModelUtils.findElementParameterByName(
                                    EParameterName.PROCESS_TYPE.getName() + ':' + EParameterName.PROCESS_TYPE_CONTEXT.getName(),
                                    node).getValue());
                    CamelFeatureUtil.addFeatureAndBundles(routeletProcess, featuresModel);

                    exportAllReferenceRoutelets(routeletProcess, routelets);
                }
            }
        }
    }

    private static IRepositoryNode getJobRepositoryNode(String jobId, ERepositoryObjectType type) throws PersistenceException {
        List<IRepositoryViewObject> list = new ArrayList<>();
        List<Project> projects = ProjectManager.getInstance().getAllReferencedProjects();
        for (Project p : projects) {
            list.addAll(ProxyRepositoryFactory.getInstance().getAll(p, type));
        }

        list.addAll(ProxyRepositoryFactory.getInstance().getAll(type));

        for (IRepositoryViewObject job : list) {
            if (job.getId().equals(jobId)) {
                return new RepositoryNode(job, null, ENodeType.REPOSITORY_ELEMENT);
            }
        }
        return null;
    }

    private void exportRouteBundle(IRepositoryNode node, File filePath, String version, String bundleVersion,
            Collection<String> routelets, String context) throws InvocationTargetException, InterruptedException {
        final JobJavaScriptOSGIForESBManager talendJobManager = new RouteJavaScriptOSGIForESBManager(getExportChoice(), context,
                routelets);
        talendJobManager.setBundleVersion(bundleVersion);
        talendJobManager.setMultiNodes(false);
        talendJobManager.setDestinationPath(filePath.getAbsolutePath());
        JobExportAction action = new JobExportAction(Collections.singletonList(node), version, bundleVersion, talendJobManager,
                getTempDir(), "Route");
        action.run(monitor);
    }

    protected void exportRouteUsedJobBundle(IRepositoryNode node, File filePath, String jobVersion, String bundleName,
            String bundleVersion, String routeName, String routeVersion, String context)
            throws InvocationTargetException, InterruptedException {
        RouteUsedJobManager talendJobManager = new RouteUsedJobManager(getExportChoice(), context);
        talendJobManager.setJobVersion(jobVersion);
        talendJobManager.setBundleName(bundleName);
        talendJobManager.setBundleVersion(bundleVersion);
        talendJobManager.setDestinationPath(filePath.getAbsolutePath());
        talendJobManager.setRouteName(routeName);
        talendJobManager.setRouteVersion(routeVersion);
        talendJobManager.setGroupId(getGroupId());
        talendJobManager.setArtifactId(getArtifactId());
        talendJobManager.setArtifactVersion(getArtifactVersion());
        JobExportAction action = new JobExportAction(Collections.singletonList(node), jobVersion, bundleVersion, talendJobManager,
                getTempDir(), "Job");
        action.run(monitor);
    }

    protected static String getTempDir() {
        String path = System.getProperty("java.io.tmpdir") + File.separatorChar + "route" + File.separatorChar; //$NON-NLS-1$
        File file = new File(path);
        if (!file.exists() || !file.isDirectory()) {
            file.mkdirs();
        }

        return path;
    }
    // END of TESB-5328

}
