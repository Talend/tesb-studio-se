// ============================================================================
//
// Copyright (C) 2006-2015 Talend Inc. - www.talend.com
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
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import aQute.bnd.osgi.Analyzer;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.IPath;
import org.eclipse.emf.common.util.EMap;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.IOsgiDependenciesService;
import org.talend.core.model.process.ElementParameterParser;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.repository.constants.FileConstants;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.runtime.process.ITalendProcessJavaProject;
import org.talend.designer.core.ICamelDesignerCoreService;
import org.talend.designer.core.model.utils.emf.talendfile.ConnectionType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.runprocess.IRunProcessService;
import org.talend.repository.documentation.ExportFileResource;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.esb.DataSourceConfig;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.esb.JobJavaScriptOSGIForESBManager;
import org.talend.repository.utils.EmfModelUtils;
import org.talend.repository.utils.TemplateProcessor;

/**
 * DOC ycbai class global comment. Detailled comment
 */
public class RouteJavaScriptOSGIForESBManager extends JobJavaScriptOSGIForESBManager {

    public RouteJavaScriptOSGIForESBManager(Map<ExportChoice, Object> exportChoiceMap, String contextName, String launcher,
            int statisticPort, int tracePort) {
        super(exportChoiceMap, contextName, launcher, statisticPort, tracePort);
    }

    protected String getIncludeRoutinesPath() {
        return USER_BEANS_PATH;
    }

    protected Collection<String> getRoutinesPaths() {
        final Collection<String> include = new ArrayList<String>();
        include.add(getIncludeRoutinesPath());
        include.add(SYSTEM_ROUTINES_PATH);
        return include;
    }

    // Add Route Resource http://jira.talendforge.org/browse/TESB-6227
    @Override
    protected void addResources(ExportFileResource osgiResource, ProcessItem processItem) throws Exception {
        IFolder srcFolder = null;
        if (GlobalServiceRegister.getDefault().isServiceRegistered(IRunProcessService.class)) {
            IRunProcessService processService = (IRunProcessService) GlobalServiceRegister.getDefault().getService(
                    IRunProcessService.class);
            ITalendProcessJavaProject talendProcessJavaProject = processService.getTalendProcessJavaProject();
            if (talendProcessJavaProject != null) {
                srcFolder = talendProcessJavaProject.getResourcesFolder();
            }
        }
        if (srcFolder == null) {
            return;
        }
        IPath srcPath = srcFolder.getLocation();

        // http://jira.talendforge.org/browse/TESB-6437
        // https://jira.talendforge.org/browse/TESB-7893
        ICamelDesignerCoreService camelService = (ICamelDesignerCoreService) GlobalServiceRegister.getDefault().getService(
                ICamelDesignerCoreService.class);
        if (camelService != null) {
            for (IPath path : camelService.synchronizeRouteResource(processItem)) {
                osgiResource.addResource(path.removeLastSegments(1).makeRelativeTo(srcPath).toString(), path.toFile().toURI()
                        .toURL());
            }
        }
    }

    private static final String TEMPLATE_BLUEPRINT_ROUTE = "/resources/route-template.xml"; //$NON-NLS-1$

    @Override
    protected void generateConfig(ExportFileResource osgiResource, ProcessItem processItem, IProcess process) throws IOException {
        final File targetFile = new File(getTmpFolder() + PATH_SEPARATOR + "route.xml"); //$NON-NLS-1$
        TemplateProcessor.processTemplate("ROUTE_BLUEPRINT_CONFIG", //$NON-NLS-1$
            collectRouteInfo(processItem, process), targetFile, getClass().getResourceAsStream(TEMPLATE_BLUEPRINT_ROUTE));
        osgiResource.addResource(FileConstants.META_INF_FOLDER_NAME + "/spring", targetFile.toURI().toURL());
    }

    private static Map<String, Object> collectRouteInfo(ProcessItem processItem, IProcess process) {
        Map<String, Object> routeInfo = new HashMap<String, Object>();

        // route name and class name
        String name = processItem.getProperty().getLabel();
        routeInfo.put("name", name); //$NON-NLS-1$
        routeInfo.put("className", getPackageName(processItem) + PACKAGE_SEPARATOR + name); //$NON-NLS-1$

        boolean useSAM = false;
        boolean hasCXFUsernameToken = false;
        boolean hasCXFSamlConsumer = false;
        boolean hasCXFSamlProvider = false;
        boolean hasCXFRSSamlProviderAuthz = false;

        Collection<NodeType> cCXFs = EmfModelUtils.getComponentsByName(processItem, "cCXF");
        boolean hasCXFComponent = !cCXFs.isEmpty();
        cCXFs.addAll(EmfModelUtils.getComponentsByName(processItem, "cCXFRS"));
        if (!cCXFs.isEmpty()) {
            Set<String> consumerNodes = new HashSet<String>();
            @SuppressWarnings("unchecked")
            List<ConnectionType> connections = processItem.getProcess().getConnection();
            for (ConnectionType conn : connections) {
                consumerNodes.add(conn.getTarget());
            }

            boolean isEEVersion = isStudioEEVersion();
            for (NodeType node : cCXFs) {
                boolean nodeUseSAM = false;
                boolean nodeUseSaml = false;
                boolean nodeUseAuthz = false;
                boolean nodeUseRegistry = false;

                // http://jira.talendforge.org/browse/TESB-3850
                String format = EmfModelUtils.computeTextElementValue("DATAFORMAT", node); //$NON-NLS-1$

                if (!useSAM && !"RAW".equals(format)) { //$NON-NLS-1$
                    nodeUseSAM = EmfModelUtils.computeCheckElementValue("ENABLE_SAM", node) //$NON-NLS-1$
                            || EmfModelUtils.computeCheckElementValue("SERVICE_ACTIVITY_MONITOR", node); //$NON-NLS-1$
                }

                // security is disable in case CXF_MESSAGE or RAW dataFormat
                if (!"CXF_MESSAGE".equals(format) && !"RAW".equals(format)) { //$NON-NLS-1$  //$NON-NLS-2$
                    if (isEEVersion && EmfModelUtils.computeCheckElementValue("ENABLE_REGISTRY", node)) { //$NON-NLS-1$
                        nodeUseRegistry = true;
                        // https://jira.talendforge.org/browse/TESB-10725
                        nodeUseSAM = false;
                    } else if (EmfModelUtils.computeCheckElementValue("ENABLE_SECURITY", node)) { //$NON-NLS-1$
                        String securityType = EmfModelUtils.computeTextElementValue("SECURITY_TYPE", node); //$NON-NLS-1$
                        if ("USER".equals(securityType)) { //$NON-NLS-1$
                            hasCXFUsernameToken = true;
                        } else if ("SAML".equals(securityType)) { //$NON-NLS-1$
                            nodeUseSaml = true;
                            nodeUseAuthz = isEEVersion && EmfModelUtils.computeCheckElementValue("USE_AUTHORIZATION", node);
                        }
                    }
                }
                useSAM |= nodeUseSAM;
                if (consumerNodes.contains(ElementParameterParser.getUNIQUENAME(node))) {
                    hasCXFSamlConsumer |= nodeUseSaml | nodeUseRegistry;
                } else {
                    hasCXFSamlProvider |= nodeUseSaml | nodeUseRegistry;
                    hasCXFRSSamlProviderAuthz |= nodeUseAuthz;
                }
            }
        }
        routeInfo.put("useSAM", useSAM); //$NON-NLS-1$
        routeInfo.put("hasCXFUsernameToken", hasCXFUsernameToken); //$NON-NLS-1$
        routeInfo.put("hasCXFSamlConsumer", hasCXFSamlConsumer); //$NON-NLS-1$
        routeInfo.put("hasCXFSamlProvider", hasCXFSamlProvider); //$NON-NLS-1$
        routeInfo.put("hasCXFRSSamlProviderAuthz", hasCXFRSSamlProviderAuthz && !hasCXFComponent); //$NON-NLS-1$
        routeInfo.put("hasCXFComponent", hasCXFComponent); //$NON-NLS-1$

        // route OSGi DataSources
        routeInfo.put("dataSources", DataSourceConfig.getAliases(process)); //$NON-NLS-1$

        return routeInfo;
    }

    @Override
    protected void addOsgiDependencies(Analyzer analyzer, ExportFileResource libResource, ProcessItem processItem)
            throws IOException {
        StringBuilder exportPackage = new StringBuilder();
        exportPackage.append(getPackageName(processItem));
        // Add Route Resource Export packages
        // http://jira.talendforge.org/browse/TESB-6227
        for (String routeResourcePackage : addAdditionalExportPackages(processItem)) {
            exportPackage.append(',').append(routeResourcePackage);
        }

        IPath libPath = null;
        if (GlobalServiceRegister.getDefault().isServiceRegistered(IRunProcessService.class)) {
            IRunProcessService processService = (IRunProcessService) GlobalServiceRegister.getDefault().getService(
                    IRunProcessService.class);
            ITalendProcessJavaProject talendProcessJavaProject = processService.getTalendProcessJavaProject();
            if (talendProcessJavaProject != null) {
                libPath = talendProcessJavaProject.getLibFolder().getLocation();
            }
        }
        if (libPath == null) {
            return;
        }
        IOsgiDependenciesService dependenciesService = (IOsgiDependenciesService) GlobalServiceRegister.getDefault().getService(
                IOsgiDependenciesService.class);
        if (dependenciesService != null) {
            Map<String, String> bundleDependences = dependenciesService.getBundleDependences(processItem);
            // process external libs
            String externalLibs = bundleDependences.get(IOsgiDependenciesService.BUNDLE_CLASSPATH);
            String[] libs = externalLibs.split(Character.toString(IOsgiDependenciesService.ITEM_SEPARATOR));
            Set<URL> list = new HashSet<URL>();
            for (String s : libs) {
                if (s.isEmpty()) {
                    continue;
                }
                IPath path = libPath.append(s);
                URL url = path.toFile().toURI().toURL();
                list.add(url);
            }
            libResource.addResources(new ArrayList<URL>(list));

            // add manifest items
            String requireBundles = bundleDependences.get(IOsgiDependenciesService.REQUIRE_BUNDLE);
            if (requireBundles != null && !"".equals(requireBundles)) {
                analyzer.setProperty(Analyzer.REQUIRE_BUNDLE, requireBundles);
            }
            String importPackages = bundleDependences.get(IOsgiDependenciesService.IMPORT_PACKAGE);
            if (importPackages != null && !"".equals(importPackages)) {
                analyzer.setProperty(Analyzer.IMPORT_PACKAGE, importPackages + ",*;resolution:=optional"); //$NON-NLS-1$
            }
            String exportPackages = bundleDependences.get(IOsgiDependenciesService.EXPORT_PACKAGE);
            if (exportPackages != null && !"".equals(exportPackages)) {
                analyzer.setProperty(Analyzer.EXPORT_PACKAGE, exportPackages);
            }
        }
    }

    /**
     * Add route resource packages.
     */
    private Collection<String> addAdditionalExportPackages(ProcessItem item) {
        Collection<String> pkgs = new HashSet<String>();
        EMap<?, ?> additionalProperties = item.getProperty().getAdditionalProperties();
        if (additionalProperties != null) {
            Object resourcesObj = additionalProperties.get("ROUTE_RESOURCES_PROP");
            if (resourcesObj != null) {
                String[] resourceIds = resourcesObj.toString().split(",");
                for (String id : resourceIds) {
                    try {
                        IRepositoryViewObject rvo = ProxyRepositoryFactory.getInstance().getLastVersion(id);
                        if (rvo != null) {
                            String path = rvo.getProperty().getItem().getState().getPath();
                            String exportPkg;
                            if (path != null && !path.isEmpty()) {
                                exportPkg = "route_resources." + path.replace("/", ".");
                            } else {
                                exportPkg = "route_resources";
                            }
                            pkgs.add(exportPkg);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return pkgs;
    }

}
