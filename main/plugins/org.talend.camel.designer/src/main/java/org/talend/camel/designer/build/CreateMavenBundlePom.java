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
package org.talend.camel.designer.build;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.maven.model.Activation;
import org.apache.maven.model.ActivationProperty;
import org.apache.maven.model.Build;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.model.Profile;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.talend.camel.designer.ui.editor.RouteProcess;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.utils.MojoType;
import org.talend.commons.utils.VersionUtils;
import org.talend.core.CorePlugin;
import org.talend.core.context.Context;
import org.talend.core.context.RepositoryContext;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.process.JobInfo;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.runtime.maven.MavenConstants;
import org.talend.core.runtime.process.LastGenerationInfo;
import org.talend.core.runtime.process.TalendProcessArgumentConstant;
import org.talend.core.runtime.projectsetting.IProjectSettingPreferenceConstants;
import org.talend.core.runtime.projectsetting.IProjectSettingTemplateConstants;
import org.talend.designer.core.IDesignerCoreService;
import org.talend.designer.maven.model.TalendMavenConstants;
import org.talend.designer.maven.template.MavenTemplateManager;
import org.talend.designer.maven.tools.AggregatorPomsHelper;
import org.talend.designer.maven.tools.creator.CreateMavenJobPom;
import org.talend.designer.maven.utils.PomIdsHelper;
import org.talend.designer.maven.utils.PomUtil;
import org.talend.designer.runprocess.IProcessor;
import org.talend.designer.runprocess.IRunProcessService;
import org.talend.designer.runprocess.ItemCacheManager;
import org.talend.repository.ProjectManager;
import org.talend.utils.io.FilesUtils;

/**
 * Route pom creator
 */
public class CreateMavenBundlePom extends CreateMavenJobPom {
    
    private static final String PATH_FEATURE = "${basedir}/src/main/bundle-resources/feature.xml";
    
    private static final String PROJECT_VERSION = "${project.version}";

    private static final String JOB_FINAL_NAME = "talend.job.finalName";

    private static final String PATH_ROUTES = "resources/templates/karaf/routes/";

    private Model bundleModel;

    /**
     * sunchaoqun CreateMavenCamelPom constructor comment.
     *
     * @param jobProcessor
     * @param pomFile
     */
    public CreateMavenBundlePom(IProcessor jobProcessor, IFile pomFile) {
        super(jobProcessor, pomFile);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.maven.tools.creator.CreateMavenBundleTemplatePom#create(org.eclipse.core.runtime.
     * IProgressMonitor)
     */
    @Override
    public void create(IProgressMonitor monitor) throws Exception {

        IFile curPomFile = getPomFile();

        if (curPomFile == null) {
            return;
        }

        bundleModel = createModel();
        // patch for TESB-23953: find "tdm-lib-di-" and remove in route, only keep 'tdm-camel'
        boolean containsTdmCamelDependency = false;
        Dependency tdmDIDependency = null;
        Dependency logbackClassicDependency = null;
        Dependency logbackCoreDependency = null;
        List<Dependency> dependencies = bundleModel.getDependencies();
        for (int i = 0; i < dependencies.size(); i++) {
            String artifactId = dependencies.get(i).getArtifactId();
            if (artifactId.startsWith("tdm-lib-di-")) {
                tdmDIDependency = dependencies.get(i);
            }
            if (artifactId.startsWith("tdm-camel-")) {
                containsTdmCamelDependency = true;
            }
            if(artifactId.equals("logback-classic")) {
            	logbackClassicDependency = dependencies.get(i);
            }
            if(artifactId.equals("logback-core")) {
            	logbackCoreDependency = dependencies.get(i);
            }
        }
        if (containsTdmCamelDependency && tdmDIDependency != null) {
            bundleModel.getDependencies().remove(tdmDIDependency);
        }
        if (logbackClassicDependency != null) {
            bundleModel.getDependencies().remove(logbackClassicDependency);
        }
        if (logbackCoreDependency != null) {
        	bundleModel.getDependencies().remove(logbackCoreDependency);
        }

        IContainer parent = curPomFile.getParent();

        Model pom = new Model();

        boolean route = "CAMEL".equals(getJobProcessor().getProcess().getComponentsType())
                && ERepositoryObjectType.getType(getJobProcessor().getProperty()).equals(ERepositoryObjectType.PROCESS_ROUTE);

        Parent parentPom = new Parent();
        parentPom.setGroupId(bundleModel.getGroupId());
        parentPom.setArtifactId(bundleModel.getArtifactId() + "-Kar");
        parentPom.setVersion(bundleModel.getVersion());
        parentPom.setRelativePath("/");

        if (route) {

            RouteProcess routeProcess = (RouteProcess) getJobProcessor().getProcess();

            boolean publishAsSnapshot = BooleanUtils
                    .toBoolean((String) routeProcess.getAdditionalProperties().get(MavenConstants.NAME_PUBLISH_AS_SNAPSHOT));

            File featurePom = new File(parent.getLocation().toOSString() + File.separator + "pom-feature.xml");

            Model featureModel = new Model();

            featureModel.setModelVersion("4.0.0");
            featureModel.setParent(parentPom);
            featureModel.setGroupId(bundleModel.getGroupId());
            featureModel.setArtifactId(bundleModel.getArtifactId() + "-feature");

            featureModel.setName(bundleModel.getName() + " Feature");

            featureModel.setVersion(bundleModel.getVersion());
            featureModel.setPackaging("pom");

            featureModel.setProperties(bundleModel.getProperties());
            featureModel.addProperty("cloud.publisher.skip", "false");
            Build featureModelBuild = new Build();


            featureModelBuild.addPlugin(addFeaturesMavenPlugin(bundleModel.getProperties().getProperty("talend.job.finalName")));

            featureModelBuild.addPlugin(addOsgiHelperMavenPlugin());
            
            // featureModelBuild.addPlugin(addDeployFeatureMavenPlugin(featureModel.getArtifactId(), featureModel.getVersion(), publishAsSnapshot));
//            featureModelBuild.addPlugin(addSkipDeployFeatureMavenPlugin());
            featureModelBuild.addPlugin(addSkipMavenCleanPlugin());
            featureModelBuild.addPlugin(addSkipDockerMavenPlugin());
            featureModel.setBuild(featureModelBuild);
//            featureModel.addProfile(addProfileForNexus(publishAsSnapshot, featureModel));
            PomUtil.savePom(monitor, featureModel, featurePom);
        }

        pom.setModelVersion("4.0.0");
        pom.setParent(bundleModel.getParent());
        pom.setGroupId(bundleModel.getGroupId());
        pom.setArtifactId(bundleModel.getArtifactId() + "-Kar");
        pom.setName(bundleModel.getName() + " Kar");
        pom.setVersion(bundleModel.getVersion());
        pom.setPackaging("pom");

        pom.addModule("pom-bundle.xml");
        if (route) {
            pom.addModule("pom-feature.xml");
        }
        pom.setDependencies(bundleModel.getDependencies());

        if (pom.getBuild() == null) {
            pom.setBuild(new Build());
        }

        pom.addProfile(addProfileForCloud());

        File pomBundle = new File(parent.getLocation().toOSString() + File.separator + "pom-bundle.xml");
        bundleModel.addProperty(JOB_FINAL_NAME, "${talend.job.name}-${project.version}");

        bundleModel.addProperty("cloud.publisher.skip", "true");
        bundleModel.setParent(parentPom);
        bundleModel.setName(bundleModel.getName() + " Bundle");

        if (bundleModel.getBuild() == null) {
            bundleModel.setBuild(new Build());
        }

        bundleModel.getBuild().addPlugin(addSkipDockerMavenPlugin());
        
        updateBundleMainfest(bundleModel);

        PomUtil.savePom(monitor, bundleModel, pomBundle);

        PomUtil.savePom(monitor, pom, curPomFile);

        parent.refreshLocal(IResource.DEPTH_ONE, monitor);

        afterCreate(monitor);
    }

    protected void updateBundleMainfest(Model bundleModel) {
        // do nothing for route
    }

    @Override
    protected void addChildrenDependencies(final List<Dependency> dependencies) {
        String parentId = getJobProcessor().getProperty().getId();
        final Set<JobInfo> clonedChildrenJobInfors = getJobProcessor().getBuildFirstChildrenJobs();
        for (JobInfo jobInfo : clonedChildrenJobInfors) {
            if (jobInfo.getFatherJobInfo() != null && jobInfo.getFatherJobInfo().getJobId().equals(parentId)) {
                if (!validChildrenJob(jobInfo)) {
                    continue;
                }
                Property property;
                String groupId;
                String artifactId;
                String version;
                String type = null;
                String buildType = null;
                if (!jobInfo.isJoblet()) {
                    property = jobInfo.getProcessItem().getProperty();
                    groupId = PomIdsHelper.getJobGroupId(property);
                    artifactId = PomIdsHelper.getJobArtifactId(jobInfo);
                    version = PomIdsHelper.getJobVersion(property);
                    // try to get the pom version of children job and load from the pom file.
                    String childPomFileName = PomUtil.getPomFileName(jobInfo.getJobName(), jobInfo.getJobVersion());
                    IProject codeProject = getJobProcessor().getCodeProject();
                    if (codeProject != null) {
                        try {
                            codeProject.refreshLocal(IResource.DEPTH_ONE, null); // is it ok or needed here ???
                        } catch (CoreException e) {
                            ExceptionHandler.process(e);
                        }
                        IFile childPomFile = codeProject.getFile(new Path(childPomFileName));
                        if (childPomFile.exists()) {
                            try {
                                Model childModel = MODEL_MANAGER.readMavenModel(childPomFile);
                                // try to get the real groupId, artifactId, version.
                                groupId = childModel.getGroupId();
                                artifactId = childModel.getArtifactId();
                                version = childModel.getVersion();
                            } catch (CoreException e) {
                                ExceptionHandler.process(e);
                            }
                        }
                    }
                } else {
                    property = jobInfo.getJobletProperty();
                    groupId = PomIdsHelper.getJobletGroupId(property);
                    artifactId = PomIdsHelper.getJobletArtifactId(property);
                    version = PomIdsHelper.getJobletVersion(property);
                    type = MavenConstants.PACKAGING_POM;
                }
                if(property != null) {
                    buildType = (String) property.getAdditionalProperties().get(TalendProcessArgumentConstant.ARG_BUILD_TYPE);
                }

                JobInfo mainJobInfo = LastGenerationInfo.getInstance().getLastMainJob();

                boolean needOSGIProcessor = true;

                if (mainJobInfo != null && mainJobInfo.getJobId().equals(property.getId())) {
                    needOSGIProcessor = false;
                }

				Dependency d = PomUtil.createDependency(groupId, artifactId, version, type);
                dependencies.add(d);
            }
        }
    }

    protected void generateAssemblyFile(IProgressMonitor monitor, final Set<JobInfo> clonedChildrenJobInfors) throws Exception {
        IFile assemblyFile = this.getAssemblyFile();
        if (assemblyFile != null) {
            // read template from project setting
            try {
                File templateFile = PomUtil.getTemplateFile(getObjectTypeFolder(), getItemRelativePath(),
                        TalendMavenConstants.ASSEMBLY_FILE_NAME);
                if (!FilesUtils.allInSameFolder(templateFile, TalendMavenConstants.POM_FILE_NAME)) {
                    templateFile = null; // force to set null, in order to use the template from other places.
                }

                final Map<String, Object> templateParameters = PomUtil.getTemplateParameters(getJobProcessor());
                String content = MavenTemplateManager.getTemplateContent(templateFile,
                        IProjectSettingPreferenceConstants.TEMPLATE_ROUTE_ASSEMBLY, JOB_TEMPLATE_BUNDLE,
                        IProjectSettingTemplateConstants.PATH_OSGI_BUNDLE + '/'
                                + IProjectSettingTemplateConstants.ASSEMBLY_ROUTE_TEMPLATE_FILE_NAME,
                        templateParameters);
                if (content != null) {
                    ByteArrayInputStream source = new ByteArrayInputStream(content.getBytes());
                    if (assemblyFile.exists()) {
                        assemblyFile.setContents(source, true, false, monitor);
                    } else {
                        assemblyFile.create(source, true, monitor);
                    }
                    updateDependencySet(assemblyFile);
                }
            } catch (Exception e) {
                ExceptionHandler.process(e);
            }
        }
    }

    /**
     * skip depoly phase in publich to cloud in parent pom, enable in nexus.
     */
    private Profile addProfileForCloud() {
        Profile deployCloudProfile = new Profile();
        deployCloudProfile.setId("deploy-cloud");
        Activation deployCloudActivation = new Activation();
        ActivationProperty activationProperty = new ActivationProperty();
        activationProperty.setName("!altDeploymentRepository");
        deployCloudActivation.setProperty(activationProperty);
        deployCloudProfile.setActivation(deployCloudActivation);
        deployCloudProfile.setBuild(new Build());
        deployCloudProfile.getBuild().addPlugin(addSkipDeployFeatureMavenPlugin());
        return deployCloudProfile;
    }

    private Plugin addFeaturesMavenPlugin(String finalNameValue) {
        Plugin plugin = new Plugin();

        plugin.setGroupId("org.apache.karaf.tooling");
        plugin.setArtifactId("karaf-maven-plugin");
        plugin.setVersion("4.2.4");

        Xpp3Dom configuration = new Xpp3Dom("configuration");

        Xpp3Dom finalName = new Xpp3Dom("finalName");

        finalName.setValue(finalNameValue);// "${talend.job.finalName}"

        Xpp3Dom resourcesDir = new Xpp3Dom("resourcesDir");
        resourcesDir.setValue("${project.build.directory}/bin");

        Xpp3Dom featuresFile = new Xpp3Dom("featuresFile");
        featuresFile.setValue(PATH_FEATURE);

        configuration.addChild(finalName);
        configuration.addChild(resourcesDir);
        configuration.addChild(featuresFile);

        List<PluginExecution> pluginExecutions = new ArrayList<PluginExecution>();
        PluginExecution pluginExecution = new PluginExecution();
        pluginExecution.setId("create-kar");
        pluginExecution.addGoal("kar");
        pluginExecution.setConfiguration(configuration);

        pluginExecutions.add(pluginExecution);
        plugin.setExecutions(pluginExecutions);

        return plugin;
    }
    
    private Plugin addOsgiHelperMavenPlugin() {
        Plugin plugin = new Plugin();

        plugin.setGroupId(TalendMavenConstants.DEFAULT_CI_GROUP_ID);
        plugin.setArtifactId(MojoType.OSGI_HELPER.getArtifactId());
        plugin.setVersion(VersionUtils.getMojoVersion(MojoType.OSGI_HELPER));

        Xpp3Dom configuration = new Xpp3Dom("configuration");
        Xpp3Dom featuresFile = new Xpp3Dom("featuresFile");
        featuresFile.setValue(PATH_FEATURE);
        configuration.addChild(featuresFile);

        List<PluginExecution> pluginExecutions = new ArrayList<PluginExecution>();
        PluginExecution pluginExecution = new PluginExecution();
        pluginExecution.setId("feature-helper");
        pluginExecution.setPhase("generate-sources");
        pluginExecution.addGoal("generate");
        pluginExecution.setConfiguration(configuration);
        pluginExecutions.add(pluginExecution);
        plugin.setExecutions(pluginExecutions);

        return plugin;
    }



    private Plugin addSkipDeployFeatureMavenPlugin() {

        Plugin plugin = new Plugin();

        plugin.setGroupId("org.apache.maven.plugins");
        plugin.setArtifactId("maven-deploy-plugin");
        plugin.setVersion("2.7");

        Xpp3Dom configuration = new Xpp3Dom("configuration");

        Xpp3Dom skip = new Xpp3Dom("skip");
        skip.setValue("true");
        configuration.addChild(skip);
        plugin.setConfiguration(configuration);

        return plugin;

    }

    private Xpp3Dom createInstallFileElement(JobInfo job) {
        Xpp3Dom file = null;
        if (getJobProcessor() != null && getProcessor(job) != null) {
            IPath currentProjectRootDir = getTalendJobJavaProject(getJobProcessor()).getProject().getLocation();
            IPath targetDir = getTalendJobJavaProject(getProcessor(job)).getTargetFolder().getLocation();
            String relativeTargetDir = targetDir.makeRelativeTo(currentProjectRootDir).toString();

            if (!ProjectManager.getInstance().isInCurrentMainProject(job.getProcessItem().getProperty())) {
                // this job/routelet is from a reference project
                currentProjectRootDir = new Path(currentProjectRootDir.getDevice(),
                        currentProjectRootDir.toString().replaceAll("/\\d+/", "/"));
                targetDir = new Path(targetDir.getDevice(), targetDir.toString().replaceAll("/\\d+/", "/"));
                relativeTargetDir = targetDir.makeRelativeTo(currentProjectRootDir).toString();
            }

            IFile jobPom = AggregatorPomsHelper.getItemPomFolder(job.getProcessItem().getProperty())
                    .getFile(TalendMavenConstants.POM_FILE_NAME);
            if (jobPom.exists()) {
                try {
                    Model jobModel = MODEL_MANAGER.readMavenModel(jobPom);
                    String resolvedFinalName = null;
                    if (jobModel.getProperties().getProperty(JOB_FINAL_NAME) != null) {
                        resolvedFinalName = resolveJobFinalName(jobModel.getProperties().getProperty(JOB_FINAL_NAME), jobModel);
                    } else {
                        for (String modelName : jobModel.getModules()) {
                            IFile subPom = AggregatorPomsHelper.getItemPomFolder(job.getProcessItem().getProperty())
                                    .getFile(modelName);
                            if (subPom.exists()) {
                                Model subModel = MODEL_MANAGER.readMavenModel(subPom);
                                if (subModel.getProperties().getProperty(JOB_FINAL_NAME) != null) {
                                    resolvedFinalName = resolveJobFinalName(subModel.getProperties().getProperty(JOB_FINAL_NAME),
                                            subModel);
                                    break;
                                }
                            }
                        }
                    }
                    if (resolvedFinalName == null) {
                        resolvedFinalName = job.getJobName().toLowerCase() + "_"
                                + PomIdsHelper.getJobVersion(job).replaceAll("\\.", "_");
                    }

                    String pathToJar = relativeTargetDir + Path.SEPARATOR + resolvedFinalName + ".jar";
                    file = new Xpp3Dom("file");
                    file.setValue(pathToJar);
                } catch (CoreException e) {
                    e.printStackTrace();
                }
            }
        }
        return file;
    }

    private String resolveJobFinalName(String finalNameWithToken, Model jobModel) {
        if (finalNameWithToken != null) {
            Pattern p = Pattern.compile("\\$\\{([^\\}]+)\\}");
            Matcher m = p.matcher(finalNameWithToken);
            while (m.find()) {
                if (PROJECT_VERSION.equals(m.group(0))) {
                    finalNameWithToken = finalNameWithToken.replace(PROJECT_VERSION, jobModel.getVersion());
                } else {
                    String propertyValue = jobModel.getProperties().getProperty(m.group(1));
                    if (propertyValue != null) {
                        finalNameWithToken = finalNameWithToken.replace(m.group(0), propertyValue);
                    }
                }
            }
        }
        return finalNameWithToken;
    }

    boolean isRoutelet(JobInfo job) {
        if (job != null && job.getProcessItem() != null) {
            Property p = job.getProcessItem().getProperty();
            if (p != null) {
                return ERepositoryObjectType.getType(p).equals(ERepositoryObjectType.PROCESS_ROUTELET);
            }
        }
        return false;
    }

    boolean isJob(JobInfo job) {
        if (job != null && job.getProcessItem() != null) {
            Property p = job.getProcessItem().getProperty();
            if (p != null) {
                return ERepositoryObjectType.getType(p).equals(ERepositoryObjectType.PROCESS);
            }
        }
        return false;
    }

    public static IProcessor getProcessor(JobInfo jobInfo) {

        if (jobInfo.getProcessor() != null) {
            return jobInfo.getProcessor();
        }

        IProcess process = null;
        ProcessItem processItem;

        processItem = jobInfo.getProcessItem();

        if (processItem == null && jobInfo.getJobVersion() == null) {
            processItem = ItemCacheManager.getProcessItem(jobInfo.getJobId());
        }

        if (processItem == null && jobInfo.getJobVersion() != null) {
            processItem = ItemCacheManager.getProcessItem(jobInfo.getJobId(), jobInfo.getJobVersion());
        }

        if (processItem == null && jobInfo.getProcess() == null) {
            return null;
        }

        if (jobInfo.getProcess() == null) {
            if (processItem != null) {
                IDesignerCoreService service = CorePlugin.getDefault().getDesignerCoreService();
                process = service.getProcessFromProcessItem(processItem);
                if (process instanceof IProcess2) {
                    ((IProcess2) process).setProperty(processItem.getProperty());
                }
            }
            if (process == null) {
                return null;
            }
        } else {
            process = jobInfo.getProcess();
        }

        Property curProperty = processItem.getProperty();
        if (processItem.getProperty() == null && process instanceof IProcess2) {
            curProperty = ((IProcess2) process).getProperty();
        }

        IRunProcessService service = CorePlugin.getDefault().getRunProcessService();
        IProcessor processor = service.createCodeProcessor(process, curProperty,
                ((RepositoryContext) CorePlugin.getContext().getProperty(Context.REPOSITORY_CONTEXT_KEY)).getProject()
                        .getLanguage(),
                true);

        jobInfo.setProcessor(processor);

        return processor;
    }

    /**
     * Skip clean control-bundle file in target folde, in case of using mvn clean + package goal
     *
     * @return plugin
     */
    private Plugin addSkipMavenCleanPlugin() {
        Plugin plugin = new Plugin();

        plugin.setGroupId("org.apache.maven.plugins");
        plugin.setArtifactId("maven-clean-plugin");
        plugin.setVersion("3.0.0");

        Xpp3Dom configuration = new Xpp3Dom("configuration");
        Xpp3Dom skipClean = new Xpp3Dom("skip");
        skipClean.setValue("true");
        configuration.addChild(skipClean);
        plugin.setConfiguration(configuration);

        return plugin;
    }

    @Override
    protected InputStream getTemplateStream() throws IOException {
        File templateFile = PomUtil.getTemplateFile(getObjectTypeFolder(), getItemRelativePath(),
                TalendMavenConstants.POM_FILE_NAME);
        if (!FilesUtils.allInSameFolder(templateFile, TalendMavenConstants.ASSEMBLY_FILE_NAME)) {
            templateFile = null; // force to set null, in order to use the template from other places.
        }
        try {
            final Map<String, Object> templateParameters = PomUtil.getTemplateParameters(getJobProcessor());
            return MavenTemplateManager.getTemplateStream(templateFile,
                    IProjectSettingPreferenceConstants.TEMPLATE_ROUTES_KARAF_BUNDLE, "org.talend.resources.export.route",
                    getBundleTemplatePath(), templateParameters);
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    protected String getBundleTemplatePath() {
        return PATH_ROUTES + IProjectSettingTemplateConstants.MAVEN_KARAF_BUILD_BUNDLE_FILE_NAME;
    }

}
