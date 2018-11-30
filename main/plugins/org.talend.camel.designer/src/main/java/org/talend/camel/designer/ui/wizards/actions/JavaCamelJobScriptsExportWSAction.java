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
package org.talend.camel.designer.ui.wizards.actions;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.commons.lang.BooleanUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.talend.camel.designer.build.RouteBundleExportAction;
import org.talend.camel.designer.ui.wizards.export.RouteDedicatedJobManager;
import org.talend.camel.designer.ui.wizards.export.RouteJavaScriptOSGIForESBManager;
import org.talend.camel.designer.util.CamelFeatureUtil;
import org.talend.camel.model.CamelRepositoryNodeType;
import org.talend.camel.model.RouteProcessingExchange;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.utils.io.FilesUtils;
import org.talend.core.CorePlugin;
import org.talend.core.model.general.Project;
import org.talend.core.model.process.JobInfo;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.relationship.RelationshipItemBuilder;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryObject;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.repository.RepositoryObject;
import org.talend.core.repository.constants.FileConstants;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.runtime.maven.MavenConstants;
import org.talend.core.runtime.process.IBuildJobHandler;
import org.talend.core.runtime.process.ITalendProcessJavaProject;
import org.talend.core.runtime.process.LastGenerationInfo;
import org.talend.core.runtime.repository.build.IBuildResourceParametes;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.maven.utils.PomIdsHelper;
import org.talend.designer.publish.core.models.BundleModel;
import org.talend.designer.publish.core.models.FeatureModel;
import org.talend.designer.publish.core.models.FeaturesModel;
import org.talend.designer.runprocess.IProcessor;
import org.talend.designer.runprocess.IRunProcessService;
import org.talend.designer.runprocess.ProcessorUtilities;
import org.talend.repository.ProjectManager;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.IRepositoryNode.ENodeType;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.ui.wizards.exportjob.action.JobExportAction;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.BuildJobFactory;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.JobScriptsManager;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.JobScriptsManager.ExportChoice;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.esb.JobJavaScriptOSGIForESBManager;
import org.talend.repository.utils.EmfModelUtils;
import org.talend.repository.utils.JobContextUtils;

public class JavaCamelJobScriptsExportWSAction implements IRunnableWithProgress {

    private static final Properties FEATURE_MODULES = createFeatureModules();

    private IProgressMonitor monitor;

    protected final IRepositoryViewObject routeObject;

    protected final String version;

    protected String bundleVersion;

    protected String destinationKar;

    private final JobScriptsManager manager;

    private FeaturesModel featuresModel;

    private final boolean addStatisticsCode;

    private int statisticPort;

    private int tracePort;

    private Map<IRepositoryViewObject, Map<String, File>> buildArtifactsMap = new HashMap<>();

    /*
     * Contains manifest Import-Package entries for subjobs used by cTalendJob components
     * Key - ProcessItem.id of the route
     * Value - Import-package string
     */
    private Map<String, String> subjobImportPackages = new HashMap<>();

    private IBuildJobHandler buildJobHandler = null;

    private boolean buildProject = false;

    public JavaCamelJobScriptsExportWSAction(IRepositoryObject routeObject, String version, String destinationKar,
            boolean addStatisticsCode) {
        // use RepositoryObject instead of any possible instance of RepositoryObject (which will reload the property at
        // each call)
        this.routeObject = new RepositoryObject(routeObject.getProperty());
        this.version = version;
        this.bundleVersion = version;
        this.destinationKar = destinationKar;
        this.addStatisticsCode = addStatisticsCode;

        manager = new JobJavaScriptOSGIForESBManager(getExportChoice(), null, null, IProcessor.NO_STATISTICS,
                IProcessor.NO_TRACES);
        manager.setBundleVersion(version);
    }

    public JavaCamelJobScriptsExportWSAction(IRepositoryNode routeNode, String version, String destinationKar,
            boolean addStatisticsCode) {
        // use RepositoryObject instead of any possible instance of RepositoryObject (which will reload the property at
        // each call)
        this.routeObject = new RepositoryObject(routeNode.getObject().getProperty());
        this.version = version;
        this.bundleVersion = version;
        this.destinationKar = destinationKar;
        this.addStatisticsCode = addStatisticsCode;

        manager = new JobJavaScriptOSGIForESBManager(getExportChoice(), null, null, IProcessor.NO_STATISTICS,
                IProcessor.NO_TRACES);
        manager.setBundleVersion(version);
    }

    public JavaCamelJobScriptsExportWSAction(Map<ExportChoice, Object> exportChoiceMap, IRepositoryNode routeNode,
            String version, String destinationKar, boolean addStatisticsCode) {
        // use RepositoryObject instead of any possible instance of RepositoryObject (which will reload the property at
        // each call)
        this.routeObject = new RepositoryObject(routeNode.getObject().getProperty());
        this.version = version;
        this.bundleVersion = version;
        this.destinationKar = destinationKar;
        this.addStatisticsCode = addStatisticsCode;

        exportChoiceMap.putAll(getExportChoice());
        manager = new JobJavaScriptOSGIForESBManager(exportChoiceMap, null, null, IProcessor.NO_STATISTICS,
                IProcessor.NO_TRACES);
        manager.setBundleVersion(version);
    }

    public JavaCamelJobScriptsExportWSAction(IRepositoryNode routeNode, String version, String bundleVersion) {
        this(routeNode, version, null, false);
        this.bundleVersion = bundleVersion;
    }

    public JavaCamelJobScriptsExportWSAction(IRepositoryNode routeNode, String version, String destinationKar,
            boolean addStatisticsCode, int statisticPort, int tracePort) {
        // use RepositoryObject instead of any possible instance of RepositoryObject (which will reload the property at
        // each call)
        this.routeObject = new RepositoryObject(routeNode.getObject().getProperty());
        this.version = version;
        this.bundleVersion = version;
        this.destinationKar = destinationKar;
        this.addStatisticsCode = addStatisticsCode;
        this.statisticPort = statisticPort;
        this.tracePort = tracePort;

        manager = new JobJavaScriptOSGIForESBManager(getExportChoice(), null, null, statisticPort, tracePort);
        manager.setBundleVersion(version);
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
        return PomIdsHelper.getJobGroupId(routeObject.getProperty());
    }

    protected String getArtifactId() {
        return PomIdsHelper.getJobArtifactId(routeObject.getProperty());
    }

    protected String getArtifactVersion() {
        return PomIdsHelper.getJobVersion(routeObject.getProperty());
    }

    private String getJobProcessItemVersion(String jobId) {
        if (jobId == null) {
            return null;
        }

        String version = null;

        for (JobInfo job : LastGenerationInfo.getInstance().getLastGeneratedjobs()) {
            if (jobId.equals(job.getJobId())) {
                ProcessItem processItem;
                processItem = job.getProcessItem();
                if (processItem == null && job.getJobVersion() == null) {
                    processItem = org.talend.designer.runprocess.ItemCacheManager.getProcessItem(job.getJobId());
                }
                if (processItem == null && job.getJobVersion() != null) {
                    processItem = org.talend.designer.runprocess.ItemCacheManager
                            .getProcessItem(job.getJobId(), job.getJobVersion());
                }
                version = PomIdsHelper.getJobVersion(processItem.getProperty());
                break;
            }
        }
        return version;
    }

    @Override
    public final void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

        this.monitor = monitor;
        String groupId = getGroupId();
        String routeName = getArtifactId();
        String routeVersion = getArtifactVersion();

        // FIXME temporary solution, should be replaced by proper handling
        // of MicroService vs. KAR build.
        boolean isCreatingMicroService = false;
        Boolean oldMS = RouteProcessingExchange.isCreatingMicroService.get();
        RouteProcessingExchange.isCreatingMicroService.set(Boolean.FALSE);

        try {
            prepareJobBuild();
            Boolean isMS = RouteProcessingExchange.isCreatingMicroService.get();
            if (isMS != null) {
                isCreatingMicroService = isMS.booleanValue();
            }
        } catch (Exception e) {
            throw new InvocationTargetException(e);
        } finally {
            RouteProcessingExchange.isCreatingMicroService.set(oldMS);
            if (oldMS == null) {
                RouteProcessingExchange.resetMavenOffline();
            }
        }

        // FIXME may require some further actions to get all POMs.
        if (ProcessorUtilities.isGeneratePomOnly()) {
            return;
        }

        featuresModel = new FeaturesModel(groupId, routeName, routeVersion);
        try {
            File routeFile;
            try {
                routeFile = File.createTempFile("route", FileConstants.JAR_FILE_SUFFIX, new File(getTempDir())); // $NON-NLS-1$
                addBuildArtifact(routeObject, "jar", routeFile);
            } catch (IOException e) {
                throw new InvocationTargetException(e);
            }

            BundleModel routeModel = new BundleModel(groupId, routeName, routeVersion, routeFile);
            final ProcessItem routeProcess = (ProcessItem) routeObject.getProperty().getItem();

            if (featuresModel.addBundle(routeModel)) {

                CamelFeatureUtil.addFeatureAndBundles(routeProcess, featuresModel);
                featuresModel.setConfigName(routeObject.getLabel());
                featuresModel.setContexts(JobContextUtils.getContextsMap(routeProcess));

                exportAllReferenceJobs(routeName, routeProcess);
                final Set<String> routelets = new HashSet<>();
                exportAllReferenceRoutelets(routeName, routeProcess, routelets);

                exportRouteBundle(routeObject, routeFile, version, null, null, bundleVersion, null, routelets, null);
            }

            try {

                if (destinationKar != null) {
                    // FIXME should be replaced by proper handling of
                    // microservice vs. KAR creation.
                    String dest = destinationKar;
                    int suffixNdx = dest.length() - 4;
                    String suffix = "kar";
                    if (isCreatingMicroService) {
                        if (dest.regionMatches(true, suffixNdx, ".kar", 0, 4)) {
                            dest = dest.substring(0, suffixNdx) + ".jar";
                            suffix = "jar";
                        } else if (dest.regionMatches(true, suffixNdx, ".zip", 0, 4)) {
                            suffix = "zip";
                        }
                    } else {
                        if (dest.regionMatches(true, suffixNdx, ".zip", 0, 4)) {
                            Boolean isZip = (Boolean) manager.getExportChoice().get(ExportChoice.needMavenScript);
                            if (isZip == null || !isZip.booleanValue()) {
                                dest = dest.substring(0, suffixNdx) + ".kar";
                            }
                        }
                    }
                    addBuildArtifact(routeObject, suffix, new File(dest));
                }

                IRunProcessService runProcessService = CorePlugin.getDefault().getRunProcessService();
                ITalendProcessJavaProject talendProcessJavaProject =
                        runProcessService.getTalendJobJavaProject(routeObject.getProperty());

                FilesUtils
                        .copyFile(featuresModel.getContent(),
                                new File(talendProcessJavaProject.getBundleResourcesFolder().getLocation().toOSString()
                                        + File.separator + "feature.xml"));

                // Build project and collect build artifacts
                try {
                    buildJob();
                } catch (Exception ex) {
                    throw new InvocationTargetException(ex);
                }

                collectBuildArtifacts();

            } catch (Exception e) {
                e.printStackTrace();
            }

            processResults(featuresModel, monitor);

        } finally {
            // remove generated files
            removeTempFiles();
        }
    }

    protected void addBuildArtifact(IRepositoryViewObject repositoryObject, String extension, File destination) {
        Map<String, File> m = this.buildArtifactsMap.get(repositoryObject);
        if (m == null) {
            m = new HashMap<String, File>();
        }
        m.put(extension, destination);
        buildArtifactsMap.put(repositoryObject, m);
    }

    protected void collectBuildArtifacts() throws IOException {

        IRunProcessService runProcessService = CorePlugin.getDefault().getRunProcessService();

        for (Map.Entry<IRepositoryViewObject, Map<String, File>> e : buildArtifactsMap.entrySet()) {

            IRepositoryViewObject repoObject = e.getKey();
            Map<String, File> m = e.getValue();

            ITalendProcessJavaProject talendProcessJavaProject =
                    runProcessService.getTalendJobJavaProject(repoObject.getProperty());

            for (Map.Entry<String, File> e1 : m.entrySet()) {
                String extension = e1.getKey();
                File destination = e1.getValue();

                List<File> fileList = new ArrayList<File>();
                FilesUtils
                        .getAllFilesFromFolder(talendProcessJavaProject.getTargetFolder().getLocation().toFile(),
                                fileList, null);
                if (!fileList.isEmpty()) {
                    for (File f : fileList) {
                        if (f.isFile() && f.getName().endsWith(extension) && destination != null) {
                            if (!"classpath.jar".equalsIgnoreCase(f.getName())) {
                                FilesUtils.copyFile(f, destination);
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    protected void processResults(FeaturesModel featuresModel, IProgressMonitor monitor)
            throws InvocationTargetException, InterruptedException {
        // do nothing (kar will be created by maven)
        return;
    }

    private void exportAllReferenceJobs(String routeName, ProcessItem routeProcess)
            throws InvocationTargetException, InterruptedException {

        Set<String> jobPackageNames = new HashSet<String>();

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
            IRepositoryViewObject repositoryObject;
            Project jobProject;
            try {
                repositoryObject = getJobRepositoryNode(jobId, ERepositoryObjectType.PROCESS);
                jobProject = getJobProject(jobId, ERepositoryObjectType.PROCESS);
            } catch (PersistenceException e) {
                throw new InvocationTargetException(e);
            }

            if (RelationshipItemBuilder.LATEST_VERSION.equals(jobVersion)) {
                jobVersion = repositoryObject.getVersion();
            }

            String jobName = repositoryObject.getProperty().getDisplayName();
            String jobBundleName = routeName + "_" + jobName;
            String jobBundleSymbolicName = jobBundleName;

            String jobPackageName = getJobPackageName(jobProject, jobName, jobVersion);
            if (!jobPackageNames.contains(jobPackageName)) {
                jobPackageNames.add(jobPackageName);
            }

            Project project = ProjectManager.getInstance().getCurrentProject();
            if (project != null) {
                String projectName = project.getLabel();
                if (projectName != null && projectName.length() > 0) {
                    jobBundleSymbolicName = projectName.toLowerCase() + '.' + jobBundleSymbolicName;
                }
            }
            File jobFile;
            try {
                jobFile = File.createTempFile("job", FileConstants.JAR_FILE_SUFFIX, new File(getTempDir())); // $NON-NLS-1$
                addBuildArtifact(repositoryObject, "jar", jobFile);
            } catch (IOException e) {
                throw new InvocationTargetException(e);
            }
            String jobArtifactVersion = buildArtifactVersionForReferencedJob(routeProcess, jobId);
            String jobBundleVersion = bundleVersion;
            BundleModel jobModel = new BundleModel(getGroupId(), jobBundleName, jobArtifactVersion, jobFile);
            if (featuresModel.addBundle(jobModel)) {
                exportRouteUsedJobBundle(repositoryObject, jobFile, jobVersion, jobBundleName, jobBundleSymbolicName,
                        jobBundleVersion, getArtifactId(), version, jobContext);
            }
        }

        addJobPackageToOsgiImport(routeProcess, jobPackageNames);

    }

    private String buildArtifactVersionForReferencedJob(ProcessItem routeProcess, String jobId) {
        boolean isSnapshot = BooleanUtils
                .toBoolean((String) routeProcess
                        .getProperty()
                        .getAdditionalProperties()
                        .get(MavenConstants.NAME_PUBLISH_AS_SNAPSHOT));
        String jobArtifactVersion = getJobProcessItemVersion(jobId);
        if (jobArtifactVersion == null || jobArtifactVersion.isEmpty()) {
            return "";
        }
        if (!jobArtifactVersion.endsWith(MavenConstants.SNAPSHOT) && isSnapshot) {
            jobArtifactVersion += MavenConstants.SNAPSHOT;
        } else if (jobArtifactVersion.endsWith(MavenConstants.SNAPSHOT) && !isSnapshot) {
            jobArtifactVersion =
                    jobArtifactVersion.substring(0, jobArtifactVersion.lastIndexOf(MavenConstants.SNAPSHOT));
        }
        return jobArtifactVersion;
    }

    @SuppressWarnings("unchecked")
    protected final void exportAllReferenceRoutelets(String routeName, ProcessItem routeProcess, Set<String> routelets)
            throws InvocationTargetException, InterruptedException {
        for (NodeType node : (Collection<NodeType>) routeProcess.getProcess().getNode()) {
            if (!EmfModelUtils.isComponentActive(node)) {
                continue;
            }
            final ElementParameterType routeletId = EmfModelUtils
                    .findElementParameterByName(
                            EParameterName.PROCESS_TYPE.getName() + ':' + EParameterName.PROCESS_TYPE_PROCESS.getName(),
                            node);
            if (null != routeletId) {
                final IRepositoryViewObject referencedRouteletNode;
                try {
                    referencedRouteletNode =
                            getJobRepositoryNode(routeletId.getValue(), CamelRepositoryNodeType.repositoryRouteletType);
                    // getRouteletRepositoryNode(routeletId);
                } catch (PersistenceException e) {
                    throw new InvocationTargetException(e);
                }

                final ProcessItem routeletProcess = (ProcessItem) referencedRouteletNode.getProperty().getItem();
                final String className = RouteJavaScriptOSGIForESBManager.getClassName(routeletProcess);
                String idSuffix = "-" + routeName;
                if (!routelets.add(className + idSuffix)) {
                    continue;
                }

                String routeletVersion = EmfModelUtils
                        .findElementParameterByName(EParameterName.PROCESS_TYPE.getName() + ':'
                                + EParameterName.PROCESS_TYPE_VERSION.getName(), node)
                        .getValue();
                if (RelationshipItemBuilder.LATEST_VERSION.equals(routeletVersion)) {
                    routeletVersion = referencedRouteletNode.getVersion();
                }

                final File routeletFile;
                try {
                    routeletFile =
                            File.createTempFile("routelet", FileConstants.JAR_FILE_SUFFIX, new File(getTempDir())); // $NON-NLS-1$
                    addBuildArtifact(referencedRouteletNode, "jar", routeletFile);
                } catch (IOException e) {
                    throw new InvocationTargetException(e);
                }
                String routeletName = referencedRouteletNode.getLabel();
                String routeletBundleName = routeName + "_" + routeletName;
                String routeletBundleSymbolicName = routeletBundleName;
                Project project = ProjectManager.getInstance().getCurrentProject();
                if (project != null) {
                    String projectName = project.getLabel();
                    if (projectName != null && projectName.length() > 0) {
                        routeletBundleSymbolicName = projectName.toLowerCase() + '.' + routeletBundleSymbolicName;
                    }
                }
                // TESB-24268 - route is snapshot / routelet is not
                String routeletVersionToExport = PomIdsHelper.getJobVersion(referencedRouteletNode.getProperty());
                if (getArtifactVersion().endsWith(MavenConstants.SNAPSHOT)) {
                    routeletVersionToExport.concat(MavenConstants.SNAPSHOT);
                }
                BundleModel routeletModel =
                        new BundleModel(getGroupId(), routeletBundleName, routeletVersionToExport, routeletFile);
                if (featuresModel.addBundle(routeletModel)) {
                    exportRouteBundle(referencedRouteletNode, routeletFile, routeletVersion, routeletBundleName,
                            routeletBundleSymbolicName, bundleVersion, idSuffix, null,
                            EmfModelUtils
                                    .findElementParameterByName(EParameterName.PROCESS_TYPE.getName() + ':'
                                            + EParameterName.PROCESS_TYPE_CONTEXT.getName(), node)
                                    .getValue());
                    CamelFeatureUtil.addFeatureAndBundles(routeletProcess, featuresModel);
                    exportAllReferenceJobs(routeletName, routeletProcess);
                    exportAllReferenceRoutelets(routeName, routeletProcess, routelets);
                }
            }
        }
    }

    private static IRepositoryViewObject getJobRepositoryNode(String jobId, ERepositoryObjectType type)
            throws PersistenceException {
        List<IRepositoryViewObject> list = new ArrayList<>();
        List<Project> projects = ProjectManager.getInstance().getAllReferencedProjects();
        for (Project p : projects) {
            list.addAll(ProxyRepositoryFactory.getInstance().getAll(p, type));
        }

        list.addAll(ProxyRepositoryFactory.getInstance().getAll(type));

        for (IRepositoryViewObject job : list) {
            if (job.getId().equals(jobId)) {
                return new RepositoryObject(job.getProperty());
            }
        }
        return null;
    }

    private static Project getJobProject(String jobId, ERepositoryObjectType type) throws PersistenceException {
        // Check reference project first
        List<Project> projects = ProjectManager.getInstance().getAllReferencedProjects();
        for (Project p : projects) {
            for (IRepositoryViewObject job : ProxyRepositoryFactory.getInstance().getAll(p, type)) {
                if (job.getId().equals(jobId)) {
                    return p;
                }
            }
        }
        // If the job is not from reference project, then it is from current project
        return ProjectManager.getInstance().getCurrentProject();
    }

    private String getJobPackageName(Project project, String jobName, String version) {
        String p = project.getTechnicalLabel().toLowerCase();
        String j = jobName.toLowerCase();
        String[] varr = version.split("\\.");
        return p + "." + j + "_" + varr[0] + "_" + varr[1];
    }

    private void addJobPackageToOsgiImport(ProcessItem process, Set<String> jobPackageNames) {
        if (jobPackageNames.isEmpty()) {
            return;
        }
        String packages = "";
        for (String packageName : jobPackageNames) {
            if (!packages.isEmpty()) {
                packages = packages + ",";
            }
            packages = packages + packageName;
        }
        final String IMPORT_PACKAGE_KEY = "Import-Package";
        if (process.getProperty().getAdditionalProperties().containsKey(IMPORT_PACKAGE_KEY)) {
            Object o = process.getProperty().getAdditionalProperties().get(IMPORT_PACKAGE_KEY);
            if (o == null) {
                subjobImportPackages.put(process.getProperty().getId(), packages);
            } else if (o instanceof String) {
                String s = (String) o;
                if (s.isEmpty()) {
                    subjobImportPackages.put(process.getProperty().getId(), packages);
                } else {
                    subjobImportPackages.put(process.getProperty().getId(), s + "," + packages);
                }
            }
        } else {
            subjobImportPackages.put(process.getProperty().getId(), packages);
        }
    }

    private void exportRouteBundle(IRepositoryViewObject object, File filePath, String version, String bundleName,
            String bundleSymbolicName, String bundleVersion, String idSuffix, Collection<String> routelets,
            String context) throws InvocationTargetException, InterruptedException {
        final RouteJavaScriptOSGIForESBManager talendJobManager = new RouteJavaScriptOSGIForESBManager(
                getExportChoice(), context, routelets, getModulesProvidedByFeatures(), statisticPort, tracePort);
        talendJobManager.setBundleName(bundleName);
        talendJobManager.setBundleSymbolicName(bundleSymbolicName);
        talendJobManager.setBundleVersion(bundleVersion);
        talendJobManager.setOsgiServiceIdSuffix(idSuffix);
        talendJobManager.setMultiNodes(false);
        talendJobManager.setDestinationPath(filePath.getAbsolutePath());
        talendJobManager.setSubjobImportPackages(subjobImportPackages);

        RepositoryNode node = new RepositoryNode(object, null, ENodeType.REPOSITORY_ELEMENT);
        JobExportAction action = new RouteBundleExportAction(Collections.singletonList(node), version, bundleVersion,
                talendJobManager, getTempDir(), "Route");
        action.run(monitor);
    }

    protected void exportRouteUsedJobBundle(IRepositoryViewObject object, File filePath, String jobVersion,
            String bundleName, String bundleSymbolicName, String bundleVersion, String routeName, String routeVersion,
            String context) throws InvocationTargetException, InterruptedException {
        RouteDedicatedJobManager talendJobManager = new RouteDedicatedJobManager(getExportChoice(), context);
        talendJobManager.setJobVersion(jobVersion);
        talendJobManager.setBundleName(bundleName);
        talendJobManager.setBundleSymbolicName(bundleSymbolicName);
        talendJobManager.setBundleVersion(bundleVersion);
        talendJobManager.setDestinationPath(filePath.getAbsolutePath());
        talendJobManager.setRouteName(routeName);
        talendJobManager.setRouteVersion(routeVersion);
        talendJobManager.setGroupId(getGroupId());
        talendJobManager.setArtifactId(getArtifactId());
        talendJobManager.setArtifactVersion(getArtifactVersion());
        RepositoryNode node = new RepositoryNode(object, null, ENodeType.REPOSITORY_ELEMENT);
        node.getObject().getProperty().setVersion(jobVersion);
        JobExportAction action = new RouteBundleExportAction(Collections.singletonList(node), jobVersion, bundleVersion,
                talendJobManager, getTempDir(), "Job");
        action.run(monitor);
    }

    protected static String getTempDir() {
        String path = System.getProperty("java.io.tmpdir") + File.separatorChar + "route" + File.separatorChar; //$NON-NLS-2$
        File file = new File(path);
        if (!file.exists() || !file.isDirectory()) {
            file.mkdirs();
        }

        return path;
    }

    // END of TESB-5328

    protected void removeTempFiles() {
        FilesUtils.removeFolder(getTempDir(), true);
    }

    protected void prepareJobBuild() throws Exception {
        if (getBuildJobHandler() != null && getBuildProject()) {
            getBuildJobHandler().prepare(new NullProgressMonitor(), getBuildJobHandlerPrepareParams());
        }
    }

    protected void buildJob() throws Exception {
        if (getBuildJobHandler() != null && getBuildProject()) {
            getBuildJobHandler().build(new NullProgressMonitor());
        }
    }

    protected ProcessItem getProcessItem() {
        return (ProcessItem) routeObject.getProperty().getItem();
    }

    protected String getContextName() {
        return getProcessItem().getProcess().getDefaultContext();
    }

    public IBuildJobHandler getBuildJobHandler() {
        if (buildJobHandler == null && getBuildProject()) {
            buildJobHandler = BuildJobFactory
                    .createBuildJobHandler(getProcessItem(), getContextName(), version, getExportChoiceMap(), "ROUTE");
        }
        return buildJobHandler;
    }

    protected Map<String, Object> getBuildJobHandlerPrepareParams() {
        Map<String, Object> prepareParams = new HashMap<String, Object>();
        prepareParams.put(IBuildResourceParametes.OPTION_ITEMS, true);
        prepareParams.put(IBuildResourceParametes.OPTION_ITEMS_DEPENDENCIES, true);
        return prepareParams;
    }

    protected Map<ExportChoice, Object> getExportChoiceMap() {
        Map<ExportChoice, Object> exportChoiceMap = new EnumMap<ExportChoice, Object>(ExportChoice.class);

        exportChoiceMap.put(ExportChoice.esbExportType, "kar");
        exportChoiceMap.put(ExportChoice.needJobItem, false);
        exportChoiceMap.put(ExportChoice.needSourceCode, false);
        exportChoiceMap.put(ExportChoice.needMetaInfo, true);
        exportChoiceMap.put(ExportChoice.needContext, true);
        exportChoiceMap.put(ExportChoice.needLauncher, false);

        exportChoiceMap.put(ExportChoice.onlyDefautContext, false);
        return exportChoiceMap;
    }

    public boolean getBuildProject() {
        return buildProject;
    }

    public void setBuildProject(boolean buildProject) {
        this.buildProject = buildProject;
    }

    private Set<String> getModulesProvidedByFeatures() {
        Set<String> result = new HashSet<>();
        for (FeatureModel f : featuresModel.getFeatures()) {
            addFeatureModules(f.getArtifactId(), result);
        }
        return result;
    }

    private static Properties createFeatureModules() {
        Properties result = new Properties();
        InputStream is = JavaCamelJobScriptsExportWSAction.class
                .getClassLoader()
                .getResourceAsStream("resources/feature-modules.properties");
        if (is == null) {
            return result;
        }
        try {
            try {
                result.load(is);
            } finally {
                is.close();
            }
        } catch (IOException e) {
            // ignore
        }
        return result;
    }

    private static void addFeatureModules(String featureName, Set<String> collectModuleNames) {
        String modulesRaw = FEATURE_MODULES.getProperty(featureName);
        if (modulesRaw != null) {
            StringTokenizer t = new StringTokenizer(modulesRaw, ",", false);
            while (t.hasMoreTokens()) {
                String mod = t.nextToken().trim();
                if (mod.length() > 0) {
                    collectModuleNames.add(mod);
                }
            }
        }
    }
}
