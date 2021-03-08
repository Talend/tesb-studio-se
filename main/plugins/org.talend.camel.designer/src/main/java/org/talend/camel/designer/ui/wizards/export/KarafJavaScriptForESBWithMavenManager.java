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
package org.talend.camel.designer.ui.wizards.export;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.dom4j.Document;
import org.dom4j.Element;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.common.util.EList;
import org.talend.camel.core.model.camelProperties.CamelProcessItem;
import org.talend.camel.designer.util.CamelFeatureUtil;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.PluginChecker;
import org.talend.core.model.process.IContext;
import org.talend.core.model.process.ProcessUtils;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.Property;
import org.talend.core.model.relationship.RelationshipItemBuilder;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.repository.constants.FileConstants;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.repository.utils.ItemResourceUtil;
import org.talend.core.runtime.process.ITalendProcessJavaProject;
import org.talend.core.runtime.projectsetting.IProjectSettingPreferenceConstants;
import org.talend.core.runtime.projectsetting.IProjectSettingTemplateConstants;
import org.talend.designer.core.ICamelDesignerCoreService;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.maven.model.TalendMavenConstants;
import org.talend.designer.maven.template.MavenTemplateManager;
import org.talend.designer.maven.utils.PomUtil;
import org.talend.designer.runprocess.IProcessor;
import org.talend.designer.runprocess.ProcessorException;
import org.talend.repository.documentation.ExportFileResource;
import org.talend.repository.model.IRepositoryNode.ENodeType;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.ui.wizards.exportjob.action.JobExportAction;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.IMavenProperties;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.JobScriptsManager;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.esb.JavaScriptForESBWithMavenManager;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.esb.OSGIJavaScriptForESBWithMavenManager;
import org.talend.resources.util.EMavenBuildScriptProperties;
import org.talend.utils.io.FilesUtils;

/**
 * DOC ycbai class global comment. Detailled comment
 */
public class KarafJavaScriptForESBWithMavenManager extends JavaScriptForESBWithMavenManager {

    private String destinationKar;

    private static final String TALEND_JOBS_PATH = "TalendJobs/"; //$NON-NLS-1$

    private Map<String, IRepositoryViewObject> talendJobsMap = new HashMap<String, IRepositoryViewObject>();

    private Map<String, String> talendJobContextGroupsMap = new HashMap<String, String>();

    private Map<String, OSGIJavaScriptForESBWithMavenManager> talendJobOsgiWithMavenManagersMap = new HashMap<String, OSGIJavaScriptForESBWithMavenManager>();

    // private String projectName;
    private String routeName;
    private String routeVersion;
    private String groupId;

    private String type = "Job";

    private Collection<String> referenceRoutelets;

    public KarafJavaScriptForESBWithMavenManager(Map<ExportChoice, Object> exportChoiceMap, String destinationKar,
            String contextName, String launcher, int statisticPort, int tracePort, String type) {
        super(exportChoiceMap, contextName, launcher, statisticPort, tracePort);
        this.destinationKar = destinationKar;
        this.type = (type == null) ? "Job" : type;
    }

    public KarafJavaScriptForESBWithMavenManager(Map<ExportChoice, Object> exportChoiceMap, String destinationKar,
                                                 String contextName, String launcher, int statisticPort, int tracePort) {
        this(exportChoiceMap, destinationKar, contextName, launcher, statisticPort, tracePort, null);
    }

    @Override
    public List<ExportFileResource> getExportResources(ExportFileResource[] processes, String... codeOptions)
            throws ProcessorException {

        List<ExportFileResource> list = super.getExportResources(processes, codeOptions);

        // cTalendJob
        addTalendJobsExportResources(list);
        // karaf
        addKarFileToExport(list);
        // feature
        addFeatureFileToExport(list, processes);

        if (type != null && type.equalsIgnoreCase("route")) {
            RouteJavaScriptOSGIForESBManager mgr =
                    new RouteJavaScriptOSGIForESBManager(exportChoice, contextName, referenceRoutelets, null);
            mgr.setJobVersion(this.selectedJobVersion);
            list.addAll(mgr.getExportResources(processes, codeOptions));

            removeJobResources(list);
        }

        return list;
    }

    private void removeJobResources(List<ExportFileResource> list) {
        Set<String> relativePaths = null;
        ExportFileResource resource = null;
        Iterator<ExportFileResource> it = list.iterator();
        while (it.hasNext()) {
            resource = it.next();
            relativePaths = resource.getRelativePathList();

            // remove src/main/resources/OSGI-INF/blueprint/job.xml
            if (relativePaths.contains("src/main/resources/OSGI-INF/blueprint") && relativePaths.size() == 1) {
                it.remove();
                continue;
            }

            // remove lib/routines.jar
            if (resource.getDirectoryName().equals("lib")) {
                Set<URL> res = resource.getResourcesByRelativePath("");
                Iterator<URL> iterator = res.iterator();
                while (iterator.hasNext()){
                    URL url = iterator.next();
                    if (url.getPath() != null && url.getPath().endsWith("routines.jar")) {
                        iterator.remove();
                        break;
                    }
                }
            }

            // remove directories with .class files
            Collection<Set<URL>> allResources = resource.getAllResources();
            boolean containsClassFile = false;
            for (Set<URL> set : allResources) {
                for (URL url : set) {
                    if (url.getPath() != null && url.getPath().endsWith(".class")) {
                        containsClassFile = true;
                        break;
                    }
                }
                if (containsClassFile) {
                    break;
                }
            }
            if (containsClassFile) {
                it.remove();
                continue;
            }

            // remove META-INF/MANIFEST.MF
            if (resource.getDirectoryName().equals("META-INF") && relativePaths.size() == 1) {
                Set<URL> res = resource.getResourcesByRelativePath("");
                if (res.size() == 1) {
                    URL url = (URL) res.toArray()[0];
                    if (url.getPath().endsWith("MANIFEST.MF")) {
                        it.remove();
                        continue;
                    }
                }
            }

            // move Spring-related stuff to src/main/resources
            if (relativePaths.contains("META-INF/spring") && relativePaths.size() == 1) {
                resource.setDirectoryName("src/main/resources");
                continue;
            }
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    protected void analysisMavenModule(Item item) {
        if (GlobalServiceRegister.getDefault().isServiceRegistered(ICamelDesignerCoreService.class)) {
            ICamelDesignerCoreService camelService = (ICamelDesignerCoreService) GlobalServiceRegister.getDefault().getService(
                    ICamelDesignerCoreService.class);
            if (camelService.isInstanceofCamelRoutes(item)) {
                ProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
                List<String> mavenModules = getMavenModules();
                mavenModules.clear();
                talendJobsMap.clear();

                EList nodes = ((CamelProcessItem) item).getProcess().getNode();
                for (NodeType node : (List<NodeType>) nodes) {
                    if ("cTalendJob".equals(node.getComponentName())) { //$NON-NLS-1$
                        String talendJobId = null;
                        String talendJobVersion = null;
                        String talendJobContextGroup = null;

                        EList elementParameters = node.getElementParameter();
                        for (ElementParameterType paramType : (List<ElementParameterType>) elementParameters) {
                            if ("SELECTED_JOB_NAME:PROCESS_TYPE_PROCESS".equals(paramType.getName())) { //$NON-NLS-1$
                                talendJobId = ProcessUtils.getPureItemId(paramType.getValue());
                            } else if ("SELECTED_JOB_NAME:PROCESS_TYPE_VERSION".equals(paramType.getName())) { //$NON-NLS-1$
                                talendJobVersion = paramType.getValue();
                            } else if ("SELECTED_JOB_NAME:PROCESS_TYPE_CONTEXT".equals(paramType.getName())) { //$NON-NLS-1$
                                talendJobContextGroup = paramType.getValue();
                            }

                            if (talendJobId != null && talendJobVersion != null && talendJobContextGroup != null) {
                                break; // found
                            }
                        }
                        if (talendJobId != null) {
                            if (talendJobVersion == null) {
                                talendJobVersion = RelationshipItemBuilder.LATEST_VERSION;
                            }
                            if (talendJobContextGroup == null) {
                                talendJobContextGroup = IContext.DEFAULT;
                            }
                            IRepositoryViewObject foundObject = null;
                            try {
                                if (RelationshipItemBuilder.LATEST_VERSION.equals(talendJobVersion)) {
                                    foundObject = factory.getLastVersion(talendJobId);
                                } else {
                                    // find out the fixing version
                                    List<IRepositoryViewObject> allVersionObjects = factory.getAllVersion(talendJobId);
                                    if (allVersionObjects != null) {
                                        for (IRepositoryViewObject obj : allVersionObjects) {
                                            if (obj.getVersion().equals(talendJobVersion)) {
                                                foundObject = obj;
                                                break;
                                            }
                                        }
                                    }
                                }
                            } catch (PersistenceException e) {
                                ExceptionHandler.process(e);
                            }

                            if (foundObject != null) {
                                Property property = foundObject.getProperty();
                                if (property != null) {
                                    talendJobsMap.put(talendJobId, foundObject);
                                    talendJobContextGroupsMap.put(talendJobId, talendJobContextGroup);
                                    // we do not export the referenced jobs literally,
                                    // but get them for build from the Maven repository.
                                    // Thus, we must not add module declarations for them.
                                    // mavenModules.add(TALEND_JOBS_PATH + property.getLabel());
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.repository.ui.wizards.exportjob.scriptsmanager.JobJavaScriptsManager#getMainMavenProperties(org.talend
     * .core.model.properties.Item)
     */
    @Override
    protected Map<String, String> getMainMavenProperties(Item item) {
        Map<String, String> mavenPropertiesMap = super.getMainMavenProperties(item);
        // projectName = getCorrespondingProjectName(item);
        routeName = item.getProperty().getLabel();
        routeVersion = item.getProperty().getVersion();
        groupId = CamelFeatureUtil.getMavenGroupId(item);
        mavenPropertiesMap.put(EMavenBuildScriptProperties.ItemGroupName.getVarScript(), groupId);
        return mavenPropertiesMap;
    }

    @Override
    protected void addMavenBuildScripts(ExportFileResource[] processes, List<URL> scriptsUrls,
            Map<String, String> mavenPropertiesMap) {
        if (!PluginChecker.isPluginLoaded(PluginChecker.EXPORT_ROUTE_PLUGIN_ID)) {
            return;
        }

        Item item = processes[0].getItem();
        File templatePomFile = null, templateBundleFile = null, templateFeatureFile = null, templateParentFile = null;

        if (item != null) {
            IPath itemLocationPath = ItemResourceUtil.getItemLocationPath(item.getProperty());
            IFolder objectTypeFolder = ItemResourceUtil.getObjectTypeFolder(item.getProperty());
            if (itemLocationPath != null && objectTypeFolder != null) {
                IPath itemRelativePath = itemLocationPath.removeLastSegments(1).makeRelativeTo(objectTypeFolder.getLocation());
                templatePomFile = PomUtil.getTemplateFile(objectTypeFolder, itemRelativePath, TalendMavenConstants.POM_FILE_NAME);

                if (FilesUtils.allInSameFolder(templatePomFile,
                        IProjectSettingTemplateConstants.MAVEN_KARAF_BUILD_BUNDLE_FILE_NAME,
                        IProjectSettingTemplateConstants.MAVEN_KARAF_BUILD_FEATURE_FILE_NAME,
                        IProjectSettingTemplateConstants.MAVEN_KARAF_BUILD_PARENT_FILE_NAME)) {

                    templateBundleFile = new File(templatePomFile.getParentFile(),
                            IProjectSettingTemplateConstants.MAVEN_KARAF_BUILD_BUNDLE_FILE_NAME);
                    templateFeatureFile = new File(templatePomFile.getParentFile(),
                            IProjectSettingTemplateConstants.MAVEN_KARAF_BUILD_FEATURE_FILE_NAME);
                    templateParentFile = new File(templatePomFile.getParentFile(),
                            IProjectSettingTemplateConstants.MAVEN_KARAF_BUILD_PARENT_FILE_NAME);

                } else { // don't use template file from repository.
                    // other files is not init, so no need set null at all.
                    templatePomFile = null;
                }
            }
        }

        File mavenBuildFile = new File(getTmpFolder() + PATH_SEPARATOR + IProjectSettingTemplateConstants.POM_FILE_NAME);
        File mavenBuildBundleFile = new File(getTmpFolder() + PATH_SEPARATOR
                + IProjectSettingTemplateConstants.MAVEN_KARAF_BUILD_BUNDLE_FILE_NAME);
        File mavenBuildFeatureFile = new File(getTmpFolder() + PATH_SEPARATOR
                + IProjectSettingTemplateConstants.MAVEN_KARAF_BUILD_FEATURE_FILE_NAME);
        File mavenBuildParentFile = new File(getTmpFolder() + PATH_SEPARATOR
                + IProjectSettingTemplateConstants.MAVEN_KARAF_BUILD_PARENT_FILE_NAME);

        try {
            final Map<String, Object> templateParameters = PomUtil.getTemplateParameters(item.getProperty());

            String mavenScript = MavenTemplateManager.getTemplateContent(templatePomFile,
                    IProjectSettingPreferenceConstants.TEMPLATE_ROUTES_KARAF_POM, PluginChecker.EXPORT_ROUTE_PLUGIN_ID,
                    IProjectSettingTemplateConstants.PATH_ROUTE + '/' + TalendMavenConstants.POM_FILE_NAME, templateParameters);
            if (mavenScript != null) {
                createMavenBuildFileFromTemplate(mavenBuildFile, mavenScript);
                updateMavenBuildFileContent(mavenBuildFile, mavenPropertiesMap, false, true);
                scriptsUrls.add(mavenBuildFile.toURI().toURL());
            }

            mavenScript = MavenTemplateManager.getTemplateContent(templateBundleFile,
                    IProjectSettingPreferenceConstants.TEMPLATE_ROUTES_KARAF_BUNDLE, PluginChecker.EXPORT_ROUTE_PLUGIN_ID,
                    IProjectSettingTemplateConstants.PATH_ROUTE + '/'
                            + IProjectSettingTemplateConstants.MAVEN_KARAF_BUILD_BUNDLE_FILE_NAME, templateParameters);
            if (mavenScript != null) {
                createMavenBuildFileFromTemplate(mavenBuildBundleFile, mavenScript);
                updateMavenBuildFileContent(mavenBuildBundleFile, mavenPropertiesMap, true, false);
                scriptsUrls.add(mavenBuildBundleFile.toURI().toURL());
            }

            mavenScript = MavenTemplateManager.getTemplateContent(templateFeatureFile,
                    IProjectSettingPreferenceConstants.TEMPLATE_ROUTES_KARAF_FEATURE, PluginChecker.EXPORT_ROUTE_PLUGIN_ID,
                    IProjectSettingTemplateConstants.PATH_ROUTE + '/'
                            + IProjectSettingTemplateConstants.MAVEN_KARAF_BUILD_FEATURE_FILE_NAME, templateParameters);
            if (mavenScript != null) {
                createMavenBuildFileFromTemplate(mavenBuildFeatureFile, mavenScript);
                updateMavenBuildFileContent(mavenBuildFeatureFile, mavenPropertiesMap);
                scriptsUrls.add(mavenBuildFeatureFile.toURI().toURL());
            }

            mavenScript = MavenTemplateManager.getTemplateContent(templateParentFile,
                    IProjectSettingPreferenceConstants.TEMPLATE_ROUTES_KARAF_PARENT, PluginChecker.EXPORT_ROUTE_PLUGIN_ID,
                    IProjectSettingTemplateConstants.PATH_ROUTE + '/'
                            + IProjectSettingTemplateConstants.MAVEN_KARAF_BUILD_PARENT_FILE_NAME, templateParameters);
            if (mavenScript != null) {
                createMavenBuildFileFromTemplate(mavenBuildParentFile, mavenScript);
                updateMavenBuildFileContent(mavenBuildParentFile, mavenPropertiesMap);
                scriptsUrls.add(mavenBuildParentFile.toURI().toURL());
            }
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
    }

    @Override
    protected void setMavenDependencyElements(Document pomDocument) {
        Element rootElement = pomDocument.getRootElement();
        Element parentEle = rootElement.element(IMavenProperties.ELE_DEPENDENCIES);
        if (parentEle == null) {
            parentEle = rootElement.addElement(IMavenProperties.ELE_DEPENDENCIES);
        }
        removeComments(parentEle);
        // cTalendJob
        for (String key : talendJobsMap.keySet()) {
            IRepositoryViewObject repObject = talendJobsMap.get(key);
            if (repObject != null) {
                Element dependencyElement = parentEle.addElement(IMavenProperties.ELE_DEPENDENCY);
                Element groupIdElement = dependencyElement.addElement(IMavenProperties.ELE_GROUP_ID);
                groupIdElement.setText(groupId);
                Element artifactIdElement = dependencyElement.addElement(IMavenProperties.ELE_ARTIFACT_ID);
                artifactIdElement.setText(routeName + "_" + repObject.getLabel());
                Element versionElement = dependencyElement.addElement(IMavenProperties.ELE_VERSION);
                versionElement.setText(routeVersion);
            }
        }
        // add libs.
        super.setMavenDependencyElements(pomDocument);
    }

    private void addTalendJobsExportResources(List<ExportFileResource> list) {
        // for cTalendJob

        Iterator<String> iterator = this.talendJobsMap.keySet().iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            IRepositoryViewObject repObject = this.talendJobsMap.get(key);
            if (repObject == null) {
                continue;
            }
            final Item talendJobItem = repObject.getProperty().getItem();
            if (talendJobItem == null) {
                continue;
            }
            String contextgGroup = this.talendJobContextGroupsMap.get(key);
            if (contextgGroup == null) {
                contextgGroup = IContext.DEFAULT;
            }

            final String talendJobLabel = talendJobItem.getProperty().getLabel();
            final String talendJobVersion = talendJobItem.getProperty().getVersion();
            File talendJobDestFile = new File(getTmpFolder(), talendJobLabel + '_' + talendJobVersion
                    + FileConstants.ZIP_FILE_SUFFIX);
            String talendJobPath = talendJobDestFile.getAbsolutePath();

            OSGIJavaScriptForESBWithMavenManager osgiWithMavenManager = getOsgiWithMavenManager(list, talendJobLabel,
                    contextgGroup);
            osgiWithMavenManager.setDestinationPath(talendJobPath);
            osgiWithMavenManager.setJobVersion(talendJobVersion);
            talendJobOsgiWithMavenManagersMap.put(key, osgiWithMavenManager);

            ExportFileResource talendJobResource = new ExportFileResource(talendJobItem, ""); //$NON-NLS-1$
            talendJobResource.setDirectoryName(talendJobPath);
            try {

                RepositoryNode node = new RepositoryNode(repObject, null, ENodeType.REPOSITORY_ELEMENT);

                JobExportAction job = new JobExportAction(Collections.singletonList(node), talendJobVersion,
                        osgiWithMavenManager, talendJobPath) {

                    @Override
                    protected void doArchiveExport(IProgressMonitor monitor, List<ExportFileResource> resourcesToExport) {
                        // TDI-23377, no need do archive, because will re-use the resources to export for route
                        // super.doArchiveExport(monitor, resourcesToExport);
                    }

                    @Override
                    protected void reBuildJobZipFile(List<ExportFileResource> processes) {
                        // TDI-23377, no need do archive, because will re-use the resources to export for route
                        // super.reBuildJobZipFile(processes);
                    }

                    @Override
                    protected void clean() {
                        // TDI-23377, will re-use the tmp. so don't clean
                        // super.clean();
                    }

                };
                job.run(this.progressMonitor != null ? this.progressMonitor : new NullProgressMonitor());

            } catch (Exception e) {
                ExceptionHandler.process(e);
            }

        }
    }

    private String getGroupId() {
    	return groupId;
    }

    /**
     * DOC ggu Comment method "getOsgiWithMavenManager".
     *
     * @param contextgGroup
     * @return
     */
    private OSGIJavaScriptForESBWithMavenManager getOsgiWithMavenManager(final List<ExportFileResource> list,
            final String talendJobLabel, String contextgGroup) {
        OSGIJavaScriptForESBWithMavenManager osgiWithMavenManager = new OSGIJavaScriptForESBWithMavenManager(
                new EnumMap<ExportChoice, Object>(this.exportChoice), contextgGroup, JobScriptsManager.LAUNCHER_ALL,
                IProcessor.NO_STATISTICS, IProcessor.NO_TRACES) {

            @Override
            protected Map<String, String> getMainMavenProperties(Item item) {
                Map<String, String> mavenPropertiesMap = super.getMainMavenProperties(item);
                // same group id with karaf
                mavenPropertiesMap.put(EMavenBuildScriptProperties.ItemGroupName.getVarScript(), getGroupId());
                return mavenPropertiesMap;
            }

            @Override
            protected void setMavenBuildScriptProperties(Document pomDocument, Map<String, String> mavenPropertiesMap) {
                super.setMavenBuildScriptProperties(pomDocument, mavenPropertiesMap);
                String itemName = mavenPropertiesMap.get(EMavenBuildScriptProperties.ItemName.getVarScript());
                if (itemName != null && pomDocument != null) {
                    Element rootElement = pomDocument.getRootElement();
                    // Because re-use the osgi bundle for service, but for artifactId, there is no "-bundle"
                    // suffix. TDI-23491
                    Element artifactIdEle = rootElement.element(IMavenProperties.ELE_ARTIFACT_ID);
                    if (artifactIdEle != null) {
                        artifactIdEle.setText(itemName);
                    }
                }
            }

            @Override
            protected String getTmpFolder() {
                return getSystemTempFolder().getAbsolutePath();
            }

            @Override
            public void setTopFolder(List<ExportFileResource> resourcesToExport) {
                super.setTopFolder(resourcesToExport);
                // TDI-23377， need reset the path and add it in for route exporting.
                for (ExportFileResource fileResource : resourcesToExport) {
                    String directory = fileResource.getDirectoryName();
                    fileResource.setDirectoryName(TALEND_JOBS_PATH + talendJobLabel + PATH_SEPARATOR + directory);
                    // add to current route to export
                    list.add(fileResource);
                }
            }

        };
        return osgiWithMavenManager;
    }

    private void addKarFileToExport(List<ExportFileResource> list) {
        if (destinationKar != null) {
            File karFile = new File(destinationKar);
            if (karFile.exists()) {
                ExportFileResource karFileResource = new ExportFileResource(null, ""); //$NON-NLS-1$
                try {
                    karFileResource.addResource("", karFile.toURI().toURL()); //$NON-NLS-1$
                } catch (MalformedURLException e) {
                    ExceptionHandler.process(e);
                }
                list.add(karFileResource);
            }
        }
    }

    @SuppressWarnings("deprecation")
    private void addFeatureFileToExport(List<ExportFileResource> list, ExportFileResource[] processes) {
        if (destinationKar != null) {
            File karFile = new File(destinationKar);
            if (karFile.exists()) {
                ProcessItem processItem = (ProcessItem) processes[0].getItem();
                String projectName = getCorrespondingProjectName(processItem);
                String jobName = processItem.getProperty().getLabel();
                String jobVersion = processItem.getProperty().getVersion();
                StringBuilder sb = new StringBuilder();
                sb.append("repository/").append(projectName).append(PATH_SEPARATOR).append(jobName).append(PATH_SEPARATOR); //$NON-NLS-1$
                String featurePath = sb.append(jobName).append("-feature/").append(jobVersion).append(PATH_SEPARATOR) //$NON-NLS-1$
                        .append(jobName).append("-feature-").append(jobVersion).append(".xml").toString(); //$NON-NLS-1$ //$NON-NLS-2$
                ExportFileResource featureFileResource = new ExportFileResource(null, ""); //$NON-NLS-1$
                try {
                    ZipFile zipFile = new ZipFile(karFile);
                    ZipEntry zipEntry = zipFile.getEntry(featurePath);
                    if (zipEntry != null) {
                        InputStream in = null;
                        try {
                            in = zipFile.getInputStream(zipEntry);
                            File featureFile = new File(getTmpFolder() + "feature/feature.xml"); //$NON-NLS-1$
                            FilesUtils.copyFile(in, featureFile);
                            featureFileResource
                                    .addResource(IMavenProperties.MAIN_RESOURCES_PATH + "feature", featureFile.toURL()); //$NON-NLS-1$
                        } finally {
                            zipFile.close();
                        }
                    }
                } catch (Exception e) {
                    ExceptionHandler.process(e);
                }
                list.add(featureFileResource);
            }
        }
    }

    @Override
    public String getOutputSuffix() {
        return FileConstants.ZIP_FILE_SUFFIX;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.repository.ui.wizards.exportjob.scriptsmanager.JobJavaScriptsManager#addRoutinesSourceCodes(org.talend
     * .repository.documentation.ExportFileResource[], org.talend.repository.documentation.ExportFileResource,
     * ITalendProcessJavaProject, boolean)
     */
    @Override
    protected void addRoutinesSourceCodes(ExportFileResource[] process, ExportFileResource resource,
            ITalendProcessJavaProject talendProcessJavaProject, boolean useBeans) throws Exception {
        if (useBeans) {
            super.addRoutinesSourceCodes(process, resource, talendProcessJavaProject, true);
            // FIXME, need add routines also, else the maven should be error to execute
            super.addRoutinesSourceCodes(process, resource, talendProcessJavaProject, false);
        }
    }

    @Override
    protected String getTmpFolder() {
        return getSystemTempFolder().getAbsolutePath();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.repository.ui.wizards.exportjob.scriptsmanager.JobScriptsManager#deleteTempFiles()
     */
    @Override
    public void deleteTempFiles() {
        super.deleteTempFiles();
        // delete the tmp for cTalendJob also
        for (String key : talendJobOsgiWithMavenManagersMap.keySet()) {
            OSGIJavaScriptForESBWithMavenManager manager = talendJobOsgiWithMavenManagersMap.get(key);
            manager.deleteTempFiles();
        }
    }

    public void setReferenceRoutelets(Collection<String> routelets) {
        this.referenceRoutelets = routelets;
    }
}
