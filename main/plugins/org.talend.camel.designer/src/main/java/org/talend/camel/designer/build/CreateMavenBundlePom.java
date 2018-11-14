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
package org.talend.camel.designer.build;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.talend.core.CorePlugin;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.context.Context;
import org.talend.core.context.RepositoryContext;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.process.JobInfo;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.runtime.maven.MavenConstants;
import org.talend.core.runtime.process.ITalendProcessJavaProject;
import org.talend.core.runtime.process.TalendProcessArgumentConstant;
import org.talend.core.runtime.projectsetting.IProjectSettingPreferenceConstants;
import org.talend.core.runtime.projectsetting.IProjectSettingTemplateConstants;
import org.talend.designer.core.IDesignerCoreService;
import org.talend.designer.maven.model.TalendMavenConstants;
import org.talend.designer.maven.template.MavenTemplateManager;
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

    private Model bundleModel;

    /**
     * DOC sunchaoqun CreateMavenCamelPom constructor comment.
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
        List<Dependency> dependencies = bundleModel.getDependencies();
        for (int i = 0; i < dependencies.size(); i++) {
            String artifactId = dependencies.get(i).getArtifactId();
            if (artifactId.startsWith("tdm-lib-di-")) {
                tdmDIDependency = dependencies.get(i);
            }
            if (artifactId.startsWith("tdm-camel-")) {
                containsTdmCamelDependency = true;
            }
        }
        if (containsTdmCamelDependency && tdmDIDependency != null) {
            bundleModel.getDependencies().remove(tdmDIDependency);
        }
            
        IContainer parent = curPomFile.getParent();

        Model pom = new Model();

        boolean route = "CAMEL".equals(getJobProcessor().getProcess().getComponentsType());
        
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


            Set<JobInfo> subjobs = getJobProcessor().getBuildChildrenJobs();
            if (subjobs != null && !subjobs.isEmpty()) {
                int ndx = 0;
                for (JobInfo subjob : subjobs) {
                    if (isRoutelet(subjob) || isJob(subjob)) {
                        featureModelBuild.addPlugin(addFileInstallPlugin(subjob, ndx++));
                    }
                }
            }
            featureModelBuild.addPlugin(addFeaturesMavenPlugin(bundleModel.getProperties().getProperty("talend.job.finalName")));

            // featureModelBuild.addPlugin(addDeployFeatureMavenPlugin(featureModel.getArtifactId(), featureModel.getVersion(), publishAsSnapshot));
            featureModelBuild.addPlugin(addSkipDeployFeatureMavenPlugin());
            featureModelBuild.addPlugin(addSkipMavenCleanPlugin());
            featureModel.setBuild(featureModelBuild);
            featureModel.addProfile(addProfileForNexus(publishAsSnapshot, featureModel));
            /*
             * <modelVersion>4.0.0</modelVersion>
             * 
             * <groupId>org.talend.job.ffffff</groupId> <artifactId>simpleRoute-feature</artifactId>
             * <version>3.0.0</version> <packaging>pom</packaging> <build> <plugins> <plugin>
             * <groupId>org.apache.karaf.tooling</groupId> <artifactId>features-maven-plugin</artifactId>
             * <version>2.2.9</version> <executions> <execution> <id>create-kar</id> <goals> <goal>create-kar</goal>
             * </goals> <configuration> <finalName>simpleroute_3_0</finalName>
             * <resourcesDir>${project.build.directory}/bin</resourcesDir>
             * <featuresFile>/Volumes/M2/tmp/feature/feature.xml</featuresFile> </configuration> </execution>
             * </executions> </plugin> </plugins> </build>
             * 
             */

            PomUtil.savePom(monitor, featureModel, featurePom);
        }

        pom.setModelVersion("4.0.0");
         pom.setParent(bundleModel.getParent());
        pom.setGroupId(bundleModel.getGroupId());
        pom.setArtifactId(bundleModel.getArtifactId() + "-Kar");
        pom.setName(bundleModel.getName() + " Kar");
        pom.setVersion(bundleModel.getVersion());
        pom.setPackaging("pom");

        for (JobInfo job : getJobProcessor().getBuildChildrenJobs()) {
            if (isRoutelet(job)) {
                IPath currentProjectRootDir = getTalendJobJavaProject(getJobProcessor()).getProject().getLocation();
                IPath routeletPomPath = getTalendJobJavaProject(getProcessor(job)).getProjectPom().getLocation();
                String relativePomPath = routeletPomPath.makeRelativeTo(currentProjectRootDir).toString();
                // pom.addModule(relativePomPath); //TESB-22753
            }
        }

        pom.addModule("pom-bundle.xml");
        if (route) {
            pom.addModule("pom-feature.xml");
        } else {
            // for (JobInfo job : LastGenerationInfo.getInstance().getLastGeneratedjobs()) {
            // if (model.getArtifactId().equals(job.getJobName())) {
            // if (job.getFatherJobInfo() != null) {
            // model.setArtifactId(job.getFatherJobInfo().getJobName() + "_" + model.getArtifactId());
            // break;
            // }
            // }
            // }
            //
        }
        pom.setDependencies(bundleModel.getDependencies());

        if (pom.getBuild() == null) {
            pom.setBuild(new Build());
        }

        // pom.getBuild().addPlugin(addSkipDeployFeatureMavenPlugin());
        pom.addProfile(addProfileForCloud());
        /*
         * 
         * <modelVersion>4.0.0</modelVersion> <parent> <groupId>org.talend.master.ffffff</groupId>
         * <artifactId>code.Master</artifactId> <version>7.0.1</version> <relativePath>../../../</relativePath>
         * </parent> <groupId>org.talend.job.ffffff</groupId> <artifactId>simpleRoute-ogsi</artifactId>
         * <version>3.0.0</version> <packaging>pom</packaging> <modules> <module>a.xml</module> <module>b.xml</module>
         * </modules>
         * 
         */

        File pomBundle = new File(parent.getLocation().toOSString() + File.separator + "pom-bundle.xml");
        // model.setParent(null);
        // model.setDependencies(null);

        // List<Plugin> plugins = model.getBuild().getPlugins();
        //
        // for (Plugin plugin : plugins) {
        // if (plugin.getArtifactId().equals("maven-jar-plugin")) {
        // PluginExecution pluginExecution = plugin.getExecutionsAsMap().get("default-jar");
        // Xpp3Dom configuration = (Xpp3Dom) pluginExecution.getConfiguration();
        // /*
        // * <archive> <manifestFile>${project.build.outputDirectory}/META-INF/MANIFEST.MF</manifestFile>
        // * </archive>
        // */
        //
        // Xpp3Dom archive = new Xpp3Dom("archive");
        // Xpp3Dom manifestFile = new Xpp3Dom("manifestFile");
        // manifestFile.setValue("${project.build.outputDirectory}/META-INF/MANIFEST.MF");
        //
        // archive.addChild(manifestFile);
        //
        // configuration.addChild(archive);
        // System.out.println(configuration);
        // }
        // }
        bundleModel.addProperty("cloud.publisher.skip", "true");
        bundleModel.setParent(parentPom);
        List<Profile> profiles = bundleModel.getProfiles();

        for (Profile profile : profiles) {

            if (profile.getId().equals("packaging-and-assembly")) {
                List<Plugin> plugins = profile.getBuild().getPlugins();

                for (Plugin plugin : plugins) {
                    if (plugin.getArtifactId().equals("maven-assembly-plugin")) {
                        PluginExecution pluginExecution = plugin.getExecutionsAsMap().get("default");
                        Xpp3Dom configuration = (Xpp3Dom) pluginExecution.getConfiguration();
                        /*
                         * <archive> <manifestFile>${project.build.outputDirectory}/META-INF/MANIFEST.MF</manifestFile>
                         * </archive>
                         */

                        Xpp3Dom archive = new Xpp3Dom("archive");
                        Xpp3Dom manifestFile = new Xpp3Dom("manifestFile");
                        manifestFile.setValue("${current.bundle.resources.dir}/META-INF/MANIFEST.MF");

                        archive.addChild(manifestFile);

                        configuration.addChild(archive);
                        // System.out.println(configuration);
                    }
                }

            }

        }

        bundleModel.setName(bundleModel.getName() + " Bundle");

        PomUtil.savePom(monitor, bundleModel, pomBundle);

        PomUtil.savePom(monitor, pom, curPomFile);

        parent.refreshLocal(IResource.DEPTH_ONE, monitor);

        afterCreate(monitor);

    }
    
    /* (non-Javadoc)
     * @see org.talend.designer.maven.tools.creator.AbstractMavenProcessorPom#addChildrenDependencies(java.util.List)
     */
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
                Dependency d = PomUtil.createDependency(groupId, "OSGI".equals(buildType) && isJob(jobInfo) ? artifactId + "-bundle" : artifactId, version, type);
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
     * DOC enable depoly feature.xml in nexus in feature pom, skip when publish to cloud.
     */
    private Profile addProfileForNexus(boolean publishAsSnapshot, Model featureModel) {
        Profile deployFeatureProfile = new Profile();
        deployFeatureProfile.setId("deploy-nexus");
        Activation deployFeatureActivation = new Activation();
        ActivationProperty activationProperty2 = new ActivationProperty();
        activationProperty2.setName("altDeploymentRepository");
        deployFeatureActivation.setProperty(activationProperty2);
        deployFeatureProfile.setActivation(deployFeatureActivation);
        Build deployFeatureBuild = new Build();
        deployFeatureBuild.addPlugin(
                addDeployFeatureMavenPlugin(featureModel.getArtifactId(), featureModel.getVersion(), publishAsSnapshot));
        deployFeatureProfile.setBuild(deployFeatureBuild);
        return deployFeatureProfile;
    }

    /**
     * DOC skip depoly phase in publich to cloud in parent pom, enable in nexus.
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
    
    private Plugin addMavenBundlePlugin() {

        Plugin plugin = new Plugin();

        plugin.setGroupId("org.apache.felix");
        plugin.setArtifactId("maven-bundle-plugin");
        plugin.setVersion("2.5.3");

        plugin.setExtensions(true);

        Xpp3Dom configuration = new Xpp3Dom("configuration");

        Xpp3Dom archive = new Xpp3Dom("archive");
        Xpp3Dom addMavenDescriptor = new Xpp3Dom("addMavenDescriptor");

        addMavenDescriptor.setValue("false");
        archive.addChild(addMavenDescriptor);

        Xpp3Dom instructions = new Xpp3Dom("instructions");

        Xpp3Dom bundleSymbolicName = new Xpp3Dom("Bundle-SymbolicName");
        bundleSymbolicName.setValue("${project.groupId}.${project.artifactId}");

        Xpp3Dom bundleName = new Xpp3Dom("Bundle-Name");
        bundleName.setValue("${talend.job.name}");

        Xpp3Dom bundleVersion = new Xpp3Dom("Bundle-Version");
        bundleVersion.setValue("${project.version}");

        Xpp3Dom exportPackage = new Xpp3Dom("Export-Package");
        exportPackage.setValue("${bundle.config.export.package}");

        Xpp3Dom exportService = new Xpp3Dom("Export-Service");
        exportService.setValue("${bundle.config.export.service}");

        Xpp3Dom importPackage = new Xpp3Dom("Import-Package");
        importPackage.setValue("${bundle.config.import.package}, *;resolution:=optional");

        Xpp3Dom bundleClassPath = new Xpp3Dom("Bundle-ClassPath");
        bundleClassPath.setValue("., {maven-dependencies}");

        instructions.addChild(bundleSymbolicName);
        instructions.addChild(bundleName);
        instructions.addChild(bundleVersion);
        instructions.addChild(exportPackage);
        instructions.addChild(exportService);
        instructions.addChild(importPackage);
        instructions.addChild(bundleClassPath);

        configuration.addChild(instructions);

        plugin.setConfiguration(configuration);

        return plugin;
    }

    private Plugin addFeaturesMavenPlugin(String finalNameValue) {
        Plugin plugin = new Plugin();

        plugin.setGroupId("org.apache.karaf.tooling");
        plugin.setArtifactId("features-maven-plugin");
        plugin.setVersion("2.2.9");

        Xpp3Dom configuration = new Xpp3Dom("configuration");

        Xpp3Dom finalName = new Xpp3Dom("finalName");

        finalName.setValue(finalNameValue);// "${talend.job.finalName}"

        Xpp3Dom resourcesDir = new Xpp3Dom("resourcesDir");
        resourcesDir.setValue("${project.build.directory}/bin");

        Xpp3Dom featuresFile = new Xpp3Dom("featuresFile");
        featuresFile.setValue("${basedir}/src/main/bundle-resources/feature.xml");

        configuration.addChild(finalName);
        configuration.addChild(resourcesDir);
        configuration.addChild(featuresFile);

        List<PluginExecution> pluginExecutions = new ArrayList<PluginExecution>();
        PluginExecution pluginExecution = new PluginExecution();
        pluginExecution.setId("create-kar");
        pluginExecution.addGoal("create-kar");
        pluginExecution.setConfiguration(configuration);

        pluginExecutions.add(pluginExecution);
        plugin.setExecutions(pluginExecutions);

        return plugin;
    }

    private Plugin addDeployFeatureMavenPlugin(String modelArtifactId, String modelVersion, boolean publishAsSnapshot) {
        Plugin plugin = new Plugin();

        plugin.setGroupId("org.apache.maven.plugins");
        plugin.setArtifactId("maven-deploy-plugin");
        plugin.setVersion("2.7");

        Xpp3Dom configuration = new Xpp3Dom("configuration");

        Xpp3Dom file = new Xpp3Dom("file");
        file.setValue("${basedir}/src/main/bundle-resources/feature.xml");

        Xpp3Dom groupId = new Xpp3Dom("groupId");
        groupId.setValue(bundleModel.getGroupId());

        Xpp3Dom artifactId = new Xpp3Dom("artifactId");
        artifactId.setValue(modelArtifactId);

        Xpp3Dom version = new Xpp3Dom("version");
        version.setValue(modelVersion);

        Xpp3Dom classifier = new Xpp3Dom("classifier");
        classifier.setValue("features");

        Xpp3Dom packaging = new Xpp3Dom("packaging");
        packaging.setValue("xml");

        Xpp3Dom repositoryId = new Xpp3Dom("repositoryId");
        repositoryId.setValue(publishAsSnapshot ? "${project.distributionManagement.snapshotRepository.id}"
                : "${project.distributionManagement.repository.id}");

        Xpp3Dom url = new Xpp3Dom("url");
        url.setValue(publishAsSnapshot ? "${project.distributionManagement.snapshotRepository.url}"
                : "${project.distributionManagement.repository.url}");

        configuration.addChild(file);
        configuration.addChild(groupId);
        configuration.addChild(artifactId);
        configuration.addChild(version);
        configuration.addChild(classifier);
        configuration.addChild(packaging);
        configuration.addChild(repositoryId);
        configuration.addChild(url);

        List<PluginExecution> pluginExecutions = new ArrayList<PluginExecution>();
        PluginExecution pluginExecution = new PluginExecution();
        pluginExecution.setId("deploy-file");
        pluginExecution.setPhase("deploy");
        pluginExecution.addGoal("deploy-file");
        pluginExecution.setConfiguration(configuration);

        pluginExecutions.add(pluginExecution);
        
        // deploy features to nexus server
        Set<JobInfo> subjobs = getJobProcessor().getBuildChildrenJobs();
        if (subjobs != null && !subjobs.isEmpty()) {
            int ndx = 0;
            for (JobInfo subjob : subjobs) {
                if (isRoutelet(subjob) || isJob(subjob)) {

                    Xpp3Dom subjobFile = new Xpp3Dom("file");
                    boolean addFile = false;
                    if (getJobProcessor() != null && getProcessor(subjob) != null) {
                        IPath currentProjectRootDir = getTalendJobJavaProject(getJobProcessor()).getProject().getLocation();
                        IPath targetDir = getTalendJobJavaProject(getProcessor(subjob)).getTargetFolder().getLocation();
                        String relativeTargetDir = targetDir.makeRelativeTo(currentProjectRootDir).toString();

                        if (!ProjectManager.getInstance().isInCurrentMainProject(subjob.getProcessItem().getProperty())) {
                            // this job/routelet is from a reference project
                            currentProjectRootDir = new Path(currentProjectRootDir.getDevice(),
                                    currentProjectRootDir.toString().replaceAll("/\\d+/", "/"));
                            targetDir = new Path(targetDir.getDevice(), targetDir.toString().replaceAll("/\\d+/", "/"));
                            relativeTargetDir = targetDir.makeRelativeTo(currentProjectRootDir).toString();
                        }

                        Property property = null;
                        String buildType = null;
                        if (!subjob.isJoblet()) {
                            property = subjob.getProcessItem().getProperty();
                        } else {
                            property = subjob.getJobletProperty();
                        }
                        if (property != null) {
                            buildType = (String) property.getAdditionalProperties()
                                    .get(TalendProcessArgumentConstant.ARG_BUILD_TYPE);
                        }
                        
                        String pathToJar = "OSGI".equals(buildType)
                                ? relativeTargetDir + Path.SEPARATOR + subjob.getJobName() + "-bundle-"
                                        + PomIdsHelper.getJobVersion(subjob.getProcessItem().getProperty()) + ".jar"
                                : relativeTargetDir + Path.SEPARATOR + subjob.getJobName().toLowerCase() + "_"
                                        + PomIdsHelper.getJobVersion(subjob).replaceAll("\\.", "_") + ".jar";
                        subjobFile.setValue(pathToJar);
                        addFile = true;
                    }
                    if (addFile) {
                        PluginExecution pluginDeployExecution = new PluginExecution();
                        pluginDeployExecution.setId("deploy-" + bundleModel.getArtifactId() + "_" + subjob.getJobName());
                        pluginDeployExecution.setPhase("deploy");
                        pluginDeployExecution.addGoal("deploy-file");

                        Xpp3Dom subjobConfiguration = new Xpp3Dom("configuration");
                        Xpp3Dom subjobGroupId = new Xpp3Dom("groupId");
                        subjobGroupId.setValue(PomIdsHelper.getJobGroupId(subjob.getProcessItem().getProperty()));
                        Xpp3Dom subjobArtifactId = new Xpp3Dom("artifactId");
                        subjobArtifactId.setValue(bundleModel.getArtifactId() + "_" + subjob.getJobName());
                        Xpp3Dom subjobVersion = new Xpp3Dom("version");
                        subjobVersion.setValue(PomIdsHelper.getJobVersion(subjob.getProcessItem().getProperty()));

                        Xpp3Dom subjobPackaging = new Xpp3Dom("packaging");
                        subjobPackaging.setValue("jar");

                        subjobConfiguration.addChild(subjobFile);
                        subjobConfiguration.addChild(subjobGroupId);
                        subjobConfiguration.addChild(subjobArtifactId);
                        subjobConfiguration.addChild(subjobVersion);
                        subjobConfiguration.addChild(subjobPackaging);
                        subjobConfiguration.addChild(repositoryId);
                        subjobConfiguration.addChild(url);

                        pluginDeployExecution.setConfiguration(subjobConfiguration);
                        pluginExecutions.add(pluginDeployExecution);
                    }
                }
            }
        }
        
        
        
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

    private Plugin addFileInstallPlugin(JobInfo job, int ndx) {
        Plugin plugin = new Plugin();

        plugin.setGroupId("org.apache.maven.plugins");
        plugin.setArtifactId("maven-install-plugin");
        plugin.setVersion("2.5.1");

        String jobVersion = PomIdsHelper.getJobVersion(job);

        Xpp3Dom configuration = new Xpp3Dom("configuration");

        Xpp3Dom groupId = new Xpp3Dom("groupId");
        groupId.setValue(PomIdsHelper.getJobGroupId(job.getProcessItem().getProperty()));

        Xpp3Dom artifactId = new Xpp3Dom("artifactId");
        artifactId.setValue(bundleModel.getArtifactId() + "_" + job.getJobName());

        Xpp3Dom version = new Xpp3Dom("version");
        version.setValue(PomIdsHelper.getJobVersion(job.getProcessItem().getProperty()));

        Xpp3Dom packaging = new Xpp3Dom("packaging");
        packaging.setValue("jar");

        Xpp3Dom file = new Xpp3Dom("file");
        boolean addFile = false;
        if (getJobProcessor() != null && getProcessor(job) != null) {
            IPath currentProjectRootDir = getTalendJobJavaProject(getJobProcessor()).getProject().getLocation();
            IPath targetDir = getTalendJobJavaProject(getProcessor(job)).getTargetFolder().getLocation();
            String relativeTargetDir = targetDir.makeRelativeTo(currentProjectRootDir).toString();
            
            if(!ProjectManager.getInstance().isInCurrentMainProject(job.getProcessItem().getProperty())) {
                // this job/routelet is from a reference project
                currentProjectRootDir = new Path(currentProjectRootDir.getDevice()  ,currentProjectRootDir.toString().replaceAll("/\\d+/", "/"));
                targetDir = new Path(targetDir.getDevice()  ,targetDir.toString().replaceAll("/\\d+/", "/"));
                relativeTargetDir = targetDir.makeRelativeTo(currentProjectRootDir).toString();
            }
            
            Property property = null;
            String buildType = null;
            if (!job.isJoblet()) {
                property = job.getProcessItem().getProperty();
            } else {
                property = job.getJobletProperty();
            }
            if (property != null) {
                buildType = (String) property.getAdditionalProperties()
                        .get(TalendProcessArgumentConstant.ARG_BUILD_TYPE);
            }
            
            String pathToJar = "OSGI".equals(buildType)
                    ? relativeTargetDir + Path.SEPARATOR + job.getJobName() + "-bundle-"
                            + PomIdsHelper.getJobVersion(job.getProcessItem().getProperty()) + ".jar"
                    : relativeTargetDir + Path.SEPARATOR + job.getJobName().toLowerCase() + "_"
                            + PomIdsHelper.getJobVersion(job).replaceAll("\\.", "_") + ".jar";
            
            file.setValue(pathToJar);
            addFile = true;
        }

        Xpp3Dom generatePom = new Xpp3Dom("generatePom");
        generatePom.setValue("true");

        configuration.addChild(groupId);
        configuration.addChild(artifactId);
        configuration.addChild(version);
        configuration.addChild(packaging);
        if (addFile) {
            configuration.addChild(file);
        }
        configuration.addChild(generatePom);

        List<PluginExecution> pluginExecutions = new ArrayList<PluginExecution>();
        PluginExecution pluginExecution = new PluginExecution();
        pluginExecution.setId("install-jar-lib-" + ndx);
        pluginExecution.addGoal("install-file");
        pluginExecution.setPhase("validate");

        pluginExecution.setConfiguration(configuration);
        pluginExecutions.add(pluginExecution);
        plugin.setExecutions(pluginExecutions);

        return plugin;
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

    private ITalendProcessJavaProject getTalendJobJavaProject(IProcessor processor) {
        ITalendProcessJavaProject talendProcessJavaProject = processor.getTalendJavaProject();

        if (talendProcessJavaProject == null) {
            if (GlobalServiceRegister.getDefault().isServiceRegistered(IRunProcessService.class)) {
                IRunProcessService service = (IRunProcessService) GlobalServiceRegister.getDefault()
                        .getService(IRunProcessService.class);

                talendProcessJavaProject = service.getTalendJobJavaProject(processor.getProperty());

            }
        }

        return talendProcessJavaProject;
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
}
