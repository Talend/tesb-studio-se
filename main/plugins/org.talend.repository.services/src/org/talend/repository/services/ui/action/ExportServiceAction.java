package org.talend.repository.services.ui.action;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.model.Model;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.common.util.EList;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.internal.Messages;
import org.eclipse.m2e.core.internal.project.ProjectConfigurationManager;
import org.eclipse.m2e.core.project.ProjectImportConfiguration;
import org.eclipse.osgi.util.NLS;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.runtime.utils.io.FileCopyUtils;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.repository.constants.FileConstants;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.runtime.process.ITalendProcessJavaProject;
import org.talend.designer.maven.model.MavenSystemFolders;
import org.talend.designer.maven.model.ProjectSystemFolder;
import org.talend.designer.maven.model.TalendMavenConstants;
import org.talend.designer.maven.utils.PomIdsHelper;
import org.talend.designer.publish.core.models.BundleModel;
import org.talend.designer.publish.core.models.FeatureModel;
import org.talend.designer.publish.core.models.FeaturesModel;
import org.talend.designer.runprocess.java.TalendJavaProjectManager;
import org.talend.repository.model.IRepositoryNode.ENodeType;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.services.model.services.ServiceConnection;
import org.talend.repository.services.model.services.ServiceItem;
import org.talend.repository.services.model.services.ServiceOperation;
import org.talend.repository.services.model.services.ServicePort;
import org.talend.repository.services.ui.ServiceMetadataDialog;
import org.talend.repository.services.ui.scriptmanager.ServiceExportManager;
import org.talend.repository.services.utils.WSDLUtils;
import org.talend.repository.ui.wizards.exportjob.action.JobExportAction;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.JobScriptsManager;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.JobScriptsManager.ExportChoice;
import org.talend.repository.utils.EmfModelUtils;
import org.talend.repository.utils.JobContextUtils;
import org.talend.utils.io.FilesUtils;

public class ExportServiceAction implements IRunnableWithProgress {

    private static final String THMAP_COMPONENT_NAME = "tHMap";

    protected final static String PATH_SEPERATOR = "/"; //$NON-NLS-1$

    protected final ServiceItem serviceItem;

    protected Map<ExportChoice, Object> exportChoiceMap;

    protected List<IRepositoryViewObject> nodes = new ArrayList<IRepositoryViewObject>();

    private String serviceVersion;

    protected ServiceExportManager serviceManager;

    protected String tempFolder;

    private IFile serviceWsdl;

    private final Map<ServicePort, Map<String, String>> ports = new HashMap<ServicePort, Map<String, String>>();

    private String serviceName;

    private String groupId;

    private Map<String, String> additionalInfo;

    private Map<String, Map<String, String>> contextValues = new HashMap<String, Map<String, String>>();

    public ExportServiceAction(ServiceItem serviceItem, String targetPath, Map<ExportChoice, Object> exportChoiceMap)
            throws InvocationTargetException {
        this.serviceItem = serviceItem;
        this.exportChoiceMap = exportChoiceMap;
        init(targetPath);
    }

    public ExportServiceAction(ServiceItem serviceItem, String targetPath, Map<ExportChoice, Object> exportChoiceMap,
            ServiceExportManager manager) throws InvocationTargetException {
        this.serviceItem = serviceItem;
        this.exportChoiceMap = exportChoiceMap;
        this.serviceManager = manager;
        init(targetPath);
    }

    private void init(String targetPath) throws InvocationTargetException {
        serviceName = serviceItem.getProperty().getLabel();
        serviceVersion = serviceItem.getProperty().getVersion();

        serviceWsdl = WSDLUtils.getWsdlFile(serviceItem);
        ServiceConnection serviceConnection = (ServiceConnection) serviceItem.getConnection();
        additionalInfo = serviceConnection.getAdditionalInfo().map();
        EList<ServicePort> listPort = serviceConnection.getServicePort();

        List<IRepositoryViewObject> jobs;
        try {
            jobs = ProxyRepositoryFactory.getInstance().getAll(ERepositoryObjectType.PROCESS);
        } catch (PersistenceException e) {
            throw new InvocationTargetException(e);
        }
        for (ServicePort port : listPort) {
            List<ServiceOperation> listOperation = port.getServiceOperation();
            Map<String, String> operations = new HashMap<String, String>(listOperation.size());
            for (ServiceOperation operation : listOperation) {
                String jobId = operation.getReferenceJobId();
                if (jobId != null && !jobId.equals("")) {
                    String operationName = operation.getName();
                    IRepositoryViewObject jobNode = null;
                    for (IRepositoryViewObject job : jobs) {
                        if (job.getId().equals(jobId)) {
                            jobNode = job;
                            break;
                        }
                    }
                    if (jobNode == null) {
                        continue;
                    }
                    String jobName = jobNode.getLabel();
                    operations.put(operationName, jobName);
                    nodes.add(jobNode);
                    contextValues.putAll(JobContextUtils.getContextsMap((ProcessItem) jobNode.getProperty().getItem()));
                }
            }
            ports.put(port, operations);
        }

        if (this.serviceManager == null) {
            this.serviceManager = new ServiceExportManager(exportChoiceMap);
        }
        serviceManager.setDestinationPath(targetPath);
        tempFolder = getTmpFolderPath();
    }

    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        
        ITalendProcessJavaProject javaProject = createServiceJavaProject();
        String path = javaProject.getProject().getLocation().toPortableString();
        
        String destinationPath = serviceManager.getDestinationPath();
        if (!destinationPath.endsWith(FileConstants.KAR_FILE_SUFFIX)) {
            destinationPath = destinationPath.replace("\\", PATH_SEPERATOR); //$NON-NLS-1$
            if (destinationPath.indexOf(PATH_SEPERATOR) != -1) {
                String filePath = destinationPath.substring(0, destinationPath.lastIndexOf(PATH_SEPERATOR) + 1);
                String fileName = destinationPath.substring(destinationPath.lastIndexOf(PATH_SEPERATOR) + 1);
                if (fileName.indexOf('.') != -1) {
                    fileName = fileName.substring(0, fileName.lastIndexOf('.'));
                }
                destinationPath = filePath + fileName + FileConstants.KAR_FILE_SUFFIX;
            }
        }

        FeaturesModel feature = new FeaturesModel(getGroupId(), getServiceName(), getServiceVersion());
        feature.setConfigName(getServiceName());
        feature.setContexts(contextValues);

        try {
            addRequiredFeatures(feature);

            exportJobsBundle(monitor, feature);

            // control bundle
            addControlBundle(feature);

            processFeature(feature);

            processFinalResult(destinationPath);
        } catch (InvocationTargetException e) {
            throw e;
        } catch (InterruptedException e) {
            throw e;
        } catch (Exception e) {
            throw new InvocationTargetException(e);
        } finally {
            clean();
        }
    }


    protected ITalendProcessJavaProject createServiceJavaProject() {
        IProgressMonitor monitor = new NullProgressMonitor();

        // temp model.
        Model model = new Model();
        model.setModelVersion("4.0.0"); //$NON-NLS-1$
        model.setGroupId(PomIdsHelper.getJobGroupId("services.talend.org.CRMService.CRMService"));
        model.setArtifactId("talend"); //$NON-NLS-1$
        model.setVersion(PomIdsHelper.getProjectVersion());
        model.setPackaging(TalendMavenConstants.PACKAGING_JAR);
        // if (pomFile.exists()) {
        // model = MavenPlugin.getMavenModelManager().readMavenModel(pomFile);
        // } else {
        // // first time create, use temp model.
        // }
        // always use temp model to avoid classpath problem?

        final ProjectImportConfiguration importConfiguration = new ProjectImportConfiguration();
        IProject p = ResourcesPlugin.getWorkspace().getRoot().getProject("LOCAL_PROJECT");
        // beforeCreate(monitor, p);// mvn clean

        try {
            return createSimpleProject(monitor, p, model, importConfiguration);
        } catch (CoreException e) {
            e.printStackTrace();
        }
        return null;

        // gen jobs project with OSGi type

        // package the parent maven project

    }
    

    private ITalendProcessJavaProject createSimpleProject(IProgressMonitor monitor, IProject p, Model model,
            ProjectImportConfiguration importConfiguration) throws CoreException {
        final String[] directories = getFolders();

        ProjectConfigurationManager projectConfigurationManager = (ProjectConfigurationManager) MavenPlugin
                .getProjectConfigurationManager();

        String projectName = p.getName();
        monitor.beginTask(NLS.bind(Messages.ProjectConfigurationManager_task_creating, projectName), 5);

        monitor.subTask(Messages.ProjectConfigurationManager_task_creating_workspace);
        IProjectDescription description = ResourcesPlugin.getWorkspace().newProjectDescription(projectName);

        ITalendProcessJavaProject javaProject = TalendJavaProjectManager.getTalendJobJavaProject(serviceItem.getProperty());
        // ITalendProcessJavaProject javaProject =
        // TalendJavaProjectManager.getTalendCodeJavaProject(ERepositoryObjectType.PROCESS);
        
        p.open(monitor);
        monitor.worked(1);

        // hideNestedProjectsFromParents(Collections.singletonList(p));

        monitor.worked(1);

        monitor.subTask(Messages.ProjectConfigurationManager_task_creating_pom);
        IFile pomFile = p.getFile(TalendMavenConstants.POM_FILE_NAME);
        if (!pomFile.exists()) {
            MavenPlugin.getMavenModelManager().createMavenModel(pomFile, model);
        }
        monitor.worked(1);

        monitor.subTask(Messages.ProjectConfigurationManager_task_creating_folders);
        for (int i = 0; i < directories.length; i++) {
            ProjectConfigurationManager.createFolder(p.getFolder(directories[i]), false);
        }
        monitor.worked(1);

        monitor.subTask(Messages.ProjectConfigurationManager_task_creating_project);
        projectConfigurationManager.enableMavenNature(p, importConfiguration.getResolverConfiguration(), monitor);
        monitor.worked(1);

        // if (this.pomFile == null) {
        // this.pomFile = pomFile;
        // }

        return javaProject;
    }
    
    protected String[] getFolders() {
        ProjectSystemFolder[] mavenDirectories = MavenSystemFolders.ALL_DIRS;

        String[] directories = new String[mavenDirectories.length];
        for (int i = 0; i < directories.length; i++) {
            directories[i] = mavenDirectories[i].getPath();
        }

        return directories;
    }
    
    private void addControlBundle(FeaturesModel feature) throws IOException, CoreException {
        final String artifactName = getServiceName() + "-control-bundle"; //$NON-NLS-1$
        feature.addBundle(new BundleModel(getGroupId(), artifactName, getServiceVersion(), generateControlBundle(getGroupId(),
                artifactName)));
    }

    private void exportJobsBundle(IProgressMonitor monitor, FeaturesModel feature) throws InvocationTargetException,
            InterruptedException {
        String directoryName = serviceManager.getRootFolderName(tempFolder);
        for (IRepositoryViewObject node : nodes) {
            JobScriptsManager manager = serviceManager.getJobManager(exportChoiceMap, tempFolder, node, getGroupId(),
                    getServiceVersion());
            JobExportAction job = new JobExportAction(Collections.singletonList(new RepositoryNode(node, null,
                    ENodeType.REPOSITORY_ELEMENT)), node.getVersion(), getBundleVersion(), manager, directoryName, "Service"); //$NON-NLS-1$
            job.run(monitor);
            feature.addBundle(new BundleModel(getGroupId(), serviceManager.getNodeLabel(node), getServiceVersion(), new File(
                    manager.getDestinationPath())));
        }
    }

    private void addRequiredFeatures(FeaturesModel features) {
        // add correlation feature
        ServiceConnection connection = (ServiceConnection) serviceItem.getConnection();
        String useRegistry = connection.getAdditionalInfo().get(ServiceMetadataDialog.USE_SERVICE_REGISTRY);
        if (!"true".equals(useRegistry)) {
            String useCorrelation = connection.getAdditionalInfo().get(ServiceMetadataDialog.USE_BUSINESS_CORRELATION);
            if ("true".equals(useCorrelation)) {
                features.addFeature(new FeatureModel(FeaturesModel.CORRELATION_FEATURE_NAME));
            }
        }

        // add talend-data-mapper feature
        for (IRepositoryViewObject node : nodes) {
            ProcessItem processItem = (ProcessItem) node.getProperty().getItem();
            if (null != EmfModelUtils.getComponentByName(processItem, THMAP_COMPONENT_NAME)) {
                features.addFeature(new FeatureModel(FeaturesModel.TALEND_DATA_MAPPER_FEATURE_NAME));
                break;
            }
        }

    }

    private File generateControlBundle(String groupId, String artefactName) throws IOException, CoreException {
        File tempWsdl = new File(tempFolder, "control-bundle"); //$NON-NLS-1$
        // wsdl
        File wsdl = new File(tempWsdl, serviceWsdl.getName());
        // Fix for TUP-15998 Improve build performances
        if (!tempWsdl.exists()) {
            tempWsdl.mkdir();
        }
        FilesUtils.copyFile(serviceWsdl.getContents(), wsdl);
        // wsdl:import
        String serviceWsdlPrefix = serviceName + '_' + serviceVersion + '.';
        for (IResource resource : serviceWsdl.getParent().members()) {
            if (IResource.FILE == resource.getType() && resource.getName().startsWith(serviceWsdlPrefix)
                    && "wsdl".equals(resource.getFileExtension())) {
                FilesUtils.copyFile(((IFile) resource).getContents(), new File(tempWsdl, resource.getName()));
            }
        }
        // blueprint
        File blueprint = new File(tempWsdl, FileConstants.BLUEPRINT_FOLDER_NAME);
        blueprint.mkdirs();
        serviceManager
                .createBlueprint(new File(blueprint, "blueprint.xml"), ports, additionalInfo, serviceWsdl, getServiceName());
        String jarName = artefactName + '-' + getServiceVersion() + FileConstants.JAR_FILE_SUFFIX;
        File jar = new File(serviceManager.getFilePath(tempFolder, groupId, artefactName, getServiceVersion()), jarName);
        FilesUtils.jar(serviceManager.getManifest(artefactName, getBundleVersion(), additionalInfo), tempWsdl, jar);
        FilesUtils.removeFolder(tempWsdl, true);
        return jar;
    }

    protected void processFeature(FeaturesModel feature) throws IOException {
        OutputStream os = new FileOutputStream(getFeatureFile());
        try {
            FileCopyUtils.copyStreams(feature.getContent(), os);
        } finally {
            os.close();
        }
    }

    protected void processFinalResult(String destinationPath) throws IOException {
        FilesUtils.zip(tempFolder, destinationPath);
    }

    protected void clean() {
        FilesUtils.removeFolder(tempFolder, true);
    }

    protected String getControlBundleFilePath() {
        String artefactName = getServiceName() + "-control-bundle"; //$NON-NLS-1$
        String fileName = artefactName + '-' + getServiceVersion() + FileConstants.JAR_FILE_SUFFIX;
        String filePath = new File(serviceManager.getFilePath(tempFolder, getGroupId(), artefactName, getServiceVersion()),
                fileName).getAbsolutePath();

        return filePath;
    }

    protected File getFeatureFile() {
        final String artifactName = getServiceName() + FeaturesModel.NAME_SUFFIX;
        return new File(serviceManager.getFilePath(tempFolder, getGroupId(), artifactName, getServiceVersion()), artifactName
                + '-' + getServiceVersion() + ".xml"); //$NON-NLS-1$
    }

    public String getServiceVersion() {
        return serviceVersion;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getGroupId() {
        if (null == groupId) {
            try {
                groupId = getGroupId(WSDLUtils.getDefinition(serviceWsdl).getTargetNamespace(), getServiceName());
            } catch (CoreException e) {
                throw new RuntimeException(e);
            }
        }
        return groupId;
    }

    public String getBundleVersion() {
        return serviceVersion;
    }

    private final static String getGroupId(String serviceNS, String serviceName) {
        // TESB-7782: Can't Export service if the targetNamespace of it contains a illegal character
        String servNS = URI.create(serviceNS).getSchemeSpecificPart().substring(2).replace(':', '.');
        if (!servNS.endsWith("/")) {
            servNS += '/';
        }
        return (servNS + serviceName).replace('/', '.');
    }

    public String getTmpFolderPath() {
        try {
            File tmpExportFolder = File.createTempFile("service", null); //$NON-NLS-1$
            if (tmpExportFolder.exists() && tmpExportFolder.isFile()) {
                tmpExportFolder.delete();
                tmpExportFolder.mkdirs();
            }
            tmpExportFolder.deleteOnExit();
            return tmpExportFolder.getAbsolutePath();
        } catch (IOException e) {
            ExceptionHandler.process(e);
            return null;
        }
    }

}
