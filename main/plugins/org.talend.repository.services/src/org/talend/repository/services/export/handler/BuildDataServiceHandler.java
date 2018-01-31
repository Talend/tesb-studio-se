// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.repository.services.export.handler;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.Manifest;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.common.util.EList;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.utils.workbench.resources.ResourceUtils;
import org.talend.core.CorePlugin;
import org.talend.core.model.process.IContext;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.runtime.process.IBuildJobHandler;
import org.talend.core.runtime.process.ITalendProcessJavaProject;
import org.talend.core.runtime.process.TalendProcessArgumentConstant;
import org.talend.designer.maven.model.TalendMavenConstants;
import org.talend.designer.publish.core.models.FeatureModel;
import org.talend.designer.publish.core.models.FeaturesModel;
import org.talend.designer.runprocess.IProcessor;
import org.talend.designer.runprocess.IRunProcessService;
import org.talend.repository.documentation.ExportFileResource;
import org.talend.repository.services.model.services.ServiceConnection;
import org.talend.repository.services.model.services.ServiceItem;
import org.talend.repository.services.model.services.ServiceOperation;
import org.talend.repository.services.model.services.ServicePort;
import org.talend.repository.services.ui.ServiceMetadataDialog;
import org.talend.repository.services.ui.scriptmanager.ServiceExportManager;
import org.talend.repository.services.ui.scriptmanager.ServiceExportWithMavenManager;
import org.talend.repository.services.utils.WSDLUtils;
import org.talend.repository.ui.wizards.exportjob.JavaJobScriptsExportWSWizardPage.JobExportType;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.BuildJobFactory;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.JobScriptsManager;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.JobScriptsManager.ExportChoice;
import org.talend.repository.utils.EmfModelUtils;
import org.talend.repository.utils.JobContextUtils;

/**
 * DOC yyan class global comment. Detailled comment
 * 
 * For OSGi data service - SOAP
 */
public class BuildDataServiceHandler implements IBuildJobHandler {

    private ServiceItem serviceItem;

    private String serviceName;

    private String serviceVersion;

    private String version;

    private String contextName;

    private Map<ExportChoice, Object> exportChoice;

    private ITalendProcessJavaProject talendProcessJavaProject;

    private final Map<String, Object> argumentsMap = new HashMap<String, Object>();

    private IFile serviceWsdl;

    private final Map<ServicePort, Map<String, String>> ports = new HashMap<ServicePort, Map<String, String>>();

    private Map<String, String> additionalInfo;

    private List<IRepositoryViewObject> nodes = new ArrayList<IRepositoryViewObject>();

    private Map<String, Map<String, String>> contextValues = new HashMap<String, Map<String, String>>();

    private String groupId;

    private IFolder targetFolder;

    private ServiceExportWithMavenManager serviceExportWithMavenManager;
    
    private ServiceExportManager serviceExportManager;

    public BuildDataServiceHandler(ServiceItem serviceItem, String version, String contextName,
            Map<ExportChoice, Object> exportChoiceMap) {

        this.serviceItem = serviceItem;
        this.serviceName = serviceItem.getProperty().getLabel();
        this.serviceVersion = serviceItem.getProperty().getVersion();

        this.version = version;
        this.contextName = contextName;
        if (exportChoiceMap != null) {
            this.exportChoice = exportChoiceMap;
        } else {
            this.exportChoice = new HashMap<ExportChoice, Object>();
        }

        serviceWsdl = WSDLUtils.getWsdlFile(serviceItem);
        ServiceConnection serviceConnection = (ServiceConnection) serviceItem.getConnection();
        additionalInfo = serviceConnection.getAdditionalInfo().map();
        EList<ServicePort> listPort = serviceConnection.getServicePort();

        try {
            List<IRepositoryViewObject> jobs = ProxyRepositoryFactory.getInstance().getAll(ERepositoryObjectType.PROCESS);
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
        } catch (PersistenceException e1) {
        }

        IRunProcessService runProcessService = CorePlugin.getDefault().getRunProcessService();
        this.talendProcessJavaProject = runProcessService.getTalendJobJavaProject(serviceItem.getProperty());
        targetFolder = talendProcessJavaProject.getTargetFolder();
        try {
            ResourceUtils.emptyFolder(targetFolder);
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
        }

        serviceExportWithMavenManager = new ServiceExportWithMavenManager(exportChoiceMap, IContext.DEFAULT,
                JobScriptsManager.LAUNCHER_ALL, IProcessor.NO_STATISTICS, IProcessor.NO_TRACES);
        
        serviceExportManager = new ServiceExportManager(exportChoiceMap);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.ui.wizards.exportjob.handler.BuildJobHandler#build(org.eclipse.core.runtime.
     * IProgressMonitor)
     */
    public void build(IProgressMonitor monitor) throws Exception {
        argumentsMap.put(TalendProcessArgumentConstant.ARG_GOAL, TalendMavenConstants.GOAL_PACKAGE);
        talendProcessJavaProject.buildModules(monitor, null, argumentsMap);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.runtime.repository.build.IBuildExportHandler#getArguments()
     */
    @Override
    public Map<String, Object> getArguments() {
        return argumentsMap;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.runtime.process.IBuildJobHandler#generateJobFiles(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public IProcessor generateJobFiles(IProgressMonitor monitor) throws Exception {
        // TODO Generate nodes job
        // @see void
        // org.talend.repository.services.ui.action.ExportServiceWithMavenAction.addJobFilesToExport(IProgressMonitor
        // monitor) throws Exception
        for (IRepositoryViewObject node : nodes) {
            String artefactName = node.getProperty().getLabel();
            String version = node.getVersion();
            ProcessItem processItem = (ProcessItem) node.getProperty().getItem();
            IBuildJobHandler buildJobOSGiHandler = BuildJobFactory.createBuildJobHandler(processItem, contextName,
                    processItem.getProperty().getVersion(), exportChoice, JobExportType.OSGI);
            if (buildJobOSGiHandler != null) {
                buildJobOSGiHandler.generateItemFiles(true, monitor);
                buildJobOSGiHandler.generateJobFiles(monitor);
            }
        }

        // src\main\resources\feature\feature.xml
        FeaturesModel features = new FeaturesModel(getGroupId(), serviceName, serviceVersion);
        features.setConfigName(serviceName);
        features.setContexts(contextValues);

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
            if (null != EmfModelUtils.getComponentByName(processItem, "tHMap")) {
                features.addFeature(new FeatureModel(FeaturesModel.TALEND_DATA_MAPPER_FEATURE_NAME));
                break;
            }
        }
        
        IFile feature = talendProcessJavaProject.createSubFolder(monitor, talendProcessJavaProject.getResourcesFolder(), "feature").getFile("feature.xml");
        setFileContent(features.getContent(), feature, monitor);

        // resources\META-INF\MANIFEST.MF
        Manifest manifest = serviceExportManager.getManifest(serviceName, serviceVersion, additionalInfo);
        IFile mf = talendProcessJavaProject.createSubFolder(monitor, talendProcessJavaProject.getResourcesFolder(), "META-INF").getFile("MANIFEST.MF");
        //talendProcessJavaProject.getResourceSubFolder(monitor, "META-INF").getFile("MANIFEST.MF");
        FileOutputStream outputStream = new FileOutputStream(mf.getLocation().toFile());
        manifest.write(outputStream);
        outputStream.flush();
        outputStream.close();
        
        // resources\**.wsdl
        IFile wsdl = talendProcessJavaProject.getResourcesFolder().getFile(serviceWsdl.getName());
        setFileContent(serviceWsdl.getContents(), wsdl, monitor);
        
        // resources\OSGI-INF\blueprint\blueprint.xml
        IFile blueprint = talendProcessJavaProject.createSubFolder(monitor, talendProcessJavaProject.getResourcesFolder(), "OSGI-INF/blueprint").getFile("blueprint.xml"); 
                //talendProcessJavaProject.getResourceSubFolder(monitor, "OSGI-INF/blueprint").getFile("blueprint.xml");
        serviceExportManager.createBlueprint(blueprint.getLocation().toFile(), ports, additionalInfo, wsdl, serviceName);
        
        // Generate poms
        List<ExportFileResource> resources = serviceExportWithMavenManager
                .getExportResources(new ExportFileResource[] { new ExportFileResource(serviceItem, "") });
        for (ExportFileResource resource : resources) {
            Iterable<Set<URL>> paths = resource.getAllResources();
            for (Set<URL> set : paths) {
                for (URL url : set) {
                    String fileName = url.toString().substring(url.toString().lastIndexOf('/') + 1, url.toString().length());
                    IFile ifile = talendProcessJavaProject.getProject().getFile(fileName);
                    setFileContent(new FileInputStream(url.getFile()), ifile, monitor);
                }
            }
        }

        return null;// as not only one job code generated
    }

    private void setFileContent(InputStream inputStream, IFile ifile, IProgressMonitor monitor) throws CoreException {
        if (ifile.exists()) {
            ifile.setContents(inputStream, 0, monitor);
        } else {
            ifile.create(inputStream, 0, monitor);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.talend.core.runtime.process.IBuildJobHandler#generateTestReports(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public void generateTestReports(IProgressMonitor monitor) throws Exception {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.runtime.process.IBuildJobHandler#generateItemFiles(boolean,
     * org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public void generateItemFiles(boolean withDependencies, IProgressMonitor monitor) throws Exception {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.runtime.process.IBuildJobHandler#getJobTargetFile()
     */
    @Override
    public IFile getJobTargetFile() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getGroupId() {
        if (null == groupId) {
            try {
                groupId = getGroupId(WSDLUtils.getDefinition(serviceWsdl).getTargetNamespace(), serviceName);
            } catch (CoreException e) {
                throw new RuntimeException(e);
            }
        }
        return groupId;
    }

    private final String getGroupId(String serviceNS, String serviceName) {
        // TESB-7782: Can't Export service if the targetNamespace of it contains a illegal character
        String servNS = URI.create(serviceNS).getSchemeSpecificPart().substring(2).replace(':', '.');
        if (!servNS.endsWith("/")) {
            servNS += '/';
        }
        return (servNS + serviceName).replace('/', '.');
    }

    /* (non-Javadoc)
     * @see org.talend.core.runtime.repository.build.IBuildExportHandler#prepare(org.eclipse.core.runtime.IProgressMonitor, java.util.Map)
     */
    @Override
    public void prepare(IProgressMonitor monitor, Map<String, Object> parameters) throws Exception {
        // TODO Auto-generated method stub
        
    }

}
