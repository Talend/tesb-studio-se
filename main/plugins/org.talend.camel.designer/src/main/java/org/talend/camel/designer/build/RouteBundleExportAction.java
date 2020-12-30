//============================================================================
//
// Talend Community Edition
//
// Copyright (C) 2006-2019 Talend – www.talend.com
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
//
//============================================================================
package org.talend.camel.designer.build;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.talend.camel.designer.ui.wizards.export.RouteDedicatedJobManager;
import org.talend.camel.designer.ui.wizards.export.RouteJavaScriptOSGIForESBManager;
import org.talend.commons.utils.io.FilesUtils;
import org.talend.core.CorePlugin;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.relationship.RelationshipItemBuilder;
import org.talend.core.repository.constants.FileConstants;
import org.talend.core.runtime.process.ITalendProcessJavaProject;
import org.talend.designer.core.ICamelDesignerCoreService;
import org.talend.designer.runprocess.IRunProcessService;
import org.talend.designer.runprocess.ItemCacheManager;
import org.talend.repository.documentation.ExportFileResource;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.ui.wizards.exportjob.action.JobExportAction;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.JobScriptsManager;

/**
 * DOC sunchaoqun  class global comment. Detailled comment
 * <br/>
 *
 * $Id$
 *
 */
public class RouteBundleExportAction extends JobExportAction {

    private List<? extends IRepositoryNode> nodes;

    private IRunProcessService runProcessService;

    private JobScriptsManager manager;

    private String type;

    private final String LIB = "lib";

    /**
     * DOC sunchaoqun RouteBundleExportAction constructor comment.
     *
     * @param nodes
     * @param jobVersion
     * @param bundleVersion
     * @param manager
     * @param directoryName
     * @param type
     */
    public RouteBundleExportAction(List<? extends IRepositoryNode> nodes, String jobVersion, String bundleVersion,
            JobScriptsManager manager, String directoryName, String type) {
        super(nodes, jobVersion, bundleVersion, manager, directoryName, type);
        this.nodes = nodes;
        this.runProcessService = CorePlugin.getDefault().getRunProcessService();
        this.type = type;
        if (manager instanceof RouteDedicatedJobManager) {
            this.manager = (RouteDedicatedJobManager) manager;
        } else {
            this.manager = (RouteJavaScriptOSGIForESBManager) manager;
        }

    }

    private File getTemporaryStoreFile(File realFile, String relatedPath) {

        if (nodes != null && nodes.size() > 0) {
            ITalendProcessJavaProject talendProcessJavaProject = runProcessService
                    .getTalendJobJavaProject(nodes.get(0).getObject().getProperty());
            File temporaryFile = new File(talendProcessJavaProject.getBundleResourcesFolder().getLocation().toOSString()
                    + File.separator
                    + relatedPath + File.separator + realFile.getName());

            return temporaryFile;
        }

        return null;
    }

    @Override
    protected void doArchiveExport(IProgressMonitor monitor, List<ExportFileResource> resourcesToExport) {

        Collection<String> unSelectedBundles = new ArrayList();

        if (resourcesToExport.size() > 0) {
            FilesUtils.emptyFolder(getTemporaryStoreFile(new File(""), LIB));

            ProcessItem item = ItemCacheManager.getProcessItem(nodes.get(0).getId(), RelationshipItemBuilder.LATEST_VERSION);

            if (GlobalServiceRegister.getDefault().isServiceRegistered(ICamelDesignerCoreService.class)) {
                ICamelDesignerCoreService camelService = (ICamelDesignerCoreService) GlobalServiceRegister.getDefault()
                        .getService(ICamelDesignerCoreService.class);
                unSelectedBundles = camelService.getUnselectDependenciesBundle(item);
            }
        }

        for (ExportFileResource fileResource : resourcesToExport) {
            // String rootName = fileResource.getDirectoryName();

            Set<String> paths = fileResource.getRelativePathList();

            for (Object element : paths) {
                String relativePath = (String) element;
                Set<URL> resource = fileResource.getResourcesByRelativePath(relativePath);
                for (URL url : resource) {
                    // String currentResource = FilesUtils.getFileRealPath(url.getPath());
                    try {
                        File file = new File(new File(url.getFile()).toURI());
                        if (FileConstants.META_INF_FOLDER_NAME.equals(fileResource.getDirectoryName())) {
                            FilesUtils.copyFile(file, getTemporaryStoreFile(file, FileConstants.META_INF_FOLDER_NAME));
                        } else if (fileResource.getDirectoryName().equals(LIB)) {

                            if (file.getName().matches("^camel-(.*)-alldep-(.*)")
                                    || file.getName().matches("^activemq-all-[\\d\\.]*.jar")
                                    || file.getName().matches("^jms[\\d\\.-]*.jar")) {
                                continue;
                            }

                            boolean exist = false;
                            for (String name : unSelectedBundles) {
                                if (name.equals(file.getName())) {
                                    exist = true;
                                }
                            }

                            if (!exist) {
                                FilesUtils.copyFile(file, getTemporaryStoreFile(file, LIB));
                            }

                        } else if (fileResource.getDirectoryName().equals("")) {
                            if (FileConstants.BLUEPRINT_FOLDER_NAME.equals(relativePath)) {
                                FilesUtils.copyFile(file, getTemporaryStoreFile(file, FileConstants.BLUEPRINT_FOLDER_NAME));
                            } else if (FileConstants.SPRING_FOLDER_NAME.equals(relativePath)) {
                                FilesUtils.copyFile(file, getTemporaryStoreFile(file, FileConstants.SPRING_FOLDER_NAME));
                            } else if (relativePath.startsWith(FileConstants.TALEND_FOLDER_NAME)) {
                                FilesUtils.copyFile(file, getTemporaryBundleResource(file, FileConstants.TALEND_FOLDER_NAME));
                            } else if (relativePath.startsWith(FileConstants.MAVEN_FOLDER_NAME)) {
                                FilesUtils.copyFile(file, getTemporaryStoreFile(file, relativePath));
                            }
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

        }
    }
    
    private File getTemporaryBundleResource(File realFile, String relatedPath) {

        if (nodes != null && nodes.size() > 0) {
            ITalendProcessJavaProject talendProcessJavaProject = runProcessService
                    .getTalendJobJavaProject(nodes.get(0).getObject().getProperty());
            File temporaryFile = new File(talendProcessJavaProject.getBundleResourcesFolder().getLocation().toOSString()
                    + File.separator
                    + relatedPath + File.separator + realFile.getName());

            return temporaryFile;
        }

        return null;
    }

}
