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
package org.talend.designer.camel.resource.core.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.talend.camel.core.model.camelProperties.CamelProcessItem;
import org.talend.camel.core.model.camelProperties.CamelPropertiesPackage;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.general.Project;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.properties.FolderItem;
import org.talend.core.model.properties.FolderType;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.ReferenceFileItem;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.resources.ResourceItem;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.runtime.process.ITalendProcessJavaProject;
import org.talend.designer.camel.resource.core.model.ResourceDependencyModel;
import org.talend.designer.core.IDesignerCoreService;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.runprocess.IRunProcessService;
import org.talend.repository.ProjectManager;
import org.talend.utils.io.FilesUtils;

/**
 * Route Resource Utility
 *
 * @author xpli
 *
 */
public class RouteResourceUtil {

    private static final String COMMA_TAG = ",";

    private static final String SLASH_TAG = "|";

    private static final String REPACE_SLASH_TAG = "\\|";

    private static final String ROUTE_RESOURCES_PROP = "ROUTE_RESOURCES_PROP";

    private static final String RESOURCES = "resources";

    /**
     * Get source file of Item.
     *
     * @param item
     * @return
     */
    public static IFile getSourceFile(ResourceItem item) {
        // the file may come from a reference project
        IFolder rrfolder = null;
        Resource eResource = item.eResource();
        if (eResource != null) {
            URI uri = eResource.getURI();
            if (uri != null && uri.isPlatformResource()) {
                String platformString = uri.toPlatformString(true);
                IContainer parentContainer = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(platformString))
                        .getParent();
                if (parentContainer instanceof IFolder) {
                    rrfolder = (IFolder) parentContainer;
                }
            }
        }
        if (rrfolder == null) {
            Project talendProject = ProjectManager.getInstance().getCurrentProject();
            String technicalLabel = talendProject.getTechnicalLabel();

            IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(technicalLabel);
            String folderPath = item.getState().getPath();
            rrfolder = project.getFolder("resources");
            if (folderPath != null && !folderPath.isEmpty()) {
                rrfolder = rrfolder.getFolder(folderPath);
            }
        }
        String itemName = item.getProperty().getLabel();
        String version = item.getProperty().getVersion();

        String fileExtension = item.getBindingExtension();
        String fileName = itemName + "_" + version + "." + fileExtension;
        IFile file = rrfolder.getFile(fileName);

        return file;
    }

    /**
     *
     * @param routeItem
     * @param models
     */
    public static void saveResourceDependency(Map<Object, Object> map, Collection<ResourceDependencyModel> models) {
        final StringBuilder sb = new StringBuilder();
        for (ResourceDependencyModel item : models) {
            if (item.isBuiltIn()) {
                continue;
            }
            if (sb.length() != 0) {
                sb.append(RouteResourceUtil.COMMA_TAG);
            }
            sb.append(item.getItem().getProperty().getId());
            sb.append(RouteResourceUtil.SLASH_TAG);
            sb.append(item.getSelectedVersion());
        }
        if (sb.length() > 0) {
            map.put(ROUTE_RESOURCES_PROP, sb.toString());
        } else {
            map.remove(ROUTE_RESOURCES_PROP);
        }
    }

    /**
     *
     * @param routeItem
     * @param models
     */
    public static Collection<ResourceDependencyModel> getResourceDependencies(ProcessItem routeItem) {
        return getResourceDependencies(
            getBuiltInResourceDependencies(routeItem),
            (String) routeItem.getProperty().getAdditionalProperties().get(ROUTE_RESOURCES_PROP));
    }

    /**
     *
     * @param routeItem
     * @param models
     */
    public static Collection<ResourceDependencyModel> getResourceDependencies(IProcess2 process) {
        return getResourceDependencies(
            getBuiltInResourceDependencies(process),
            (String) process.getAdditionalProperties().get(ROUTE_RESOURCES_PROP));
    }

    /**
     *
     * @param id
     * @param version
     * @return
     */
    public static ResourceDependencyModel createDependency(String id, String version) {
        try {
            final IRepositoryViewObject rvo;
            if (ResourceDependencyModel.LATEST_VERSION.equals(version)) {
                rvo = ProxyRepositoryFactory.getInstance().getLastVersion(id);
            } else {
                rvo = ProxyRepositoryFactory.getInstance().getSpecificVersion(id, version, true);
            }
            if (rvo != null) {
                final ResourceDependencyModel model =
                        new ResourceDependencyModel((ResourceItem) rvo.getProperty().getItem());
                model.setSelectedVersion(version);
                return model;
            }
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
        }
        return null;
    }

    public static Collection<IPath> synchronizeRouteResource(final ProcessItem item) {
        final boolean routelet;
        if (item.eClass() == CamelPropertiesPackage.Literals.CAMEL_PROCESS_ITEM) {
            routelet = false;
        } else if (item.eClass() == CamelPropertiesPackage.Literals.ROUTELET_PROCESS_ITEM) {
            routelet = true;
        } else {
            return null;
        }
        if (!GlobalServiceRegister.getDefault().isServiceRegistered(IRunProcessService.class)) {
            return null;
        }
        final IRunProcessService processService = (IRunProcessService) GlobalServiceRegister.getDefault().getService(
                IRunProcessService.class);
        final ITalendProcessJavaProject talendProcessJavaProject = processService.getTalendJobJavaProject(item.getProperty());
        if (talendProcessJavaProject == null) {
            return null;
        }

        final IFolder routeExternalResourceFolder = talendProcessJavaProject.getExternalResourcesFolder();

        final IFolder routeResourceFolder = talendProcessJavaProject.getResourcesFolder();

        final Collection<IPath> result = new ArrayList<IPath>();
        // https://jira.talendforge.org/browse/TESB-7893
        // add spring file
        if (!routelet) {
            final IFolder metaExtInf = routeExternalResourceFolder.getFolder("META-INF/spring/");

            final IFolder metaInf = routeResourceFolder.getFolder("META-INF/spring/");
            try {
                prepareFolder(metaExtInf);
                prepareFolder(metaInf);
                final IFile springExt = metaExtInf.getFile(item.getProperty().getLabel().toLowerCase() + ".xml");
                final IFile spring = metaInf.getFile(item.getProperty().getLabel().toLowerCase() + ".xml");
                final InputStream inputStream = new ByteArrayInputStream(((CamelProcessItem) item).getSpringContent().getBytes());

                routeExternalResourceFolder.refreshLocal(IResource.DEPTH_INFINITE, null);
                routeResourceFolder.refreshLocal(IResource.DEPTH_INFINITE, null);

                if (springExt.exists()) {
                    springExt.setContents(inputStream, 0, null);
                } else {
                    springExt.create(inputStream, true, null);
                }
                result.add(springExt.getLocation());

                if (spring.exists()) {
                    spring.setContents(springExt.getContents(), 0, null);
                } else {
                    spring.create(springExt.getContents(), true, null);
                }

            } catch (CoreException e) {
                ExceptionHandler.process(e);
            }
        }

        for (ResourceDependencyModel model : getResourceDependencies(item)) {

            IFile file = copyResources(routeExternalResourceFolder, model);

            String itemName = getItemName(model.getItem()) + file.getName();

            if (file != null) {

                try {
                    FilesUtils.copyFile(file.getLocation().toFile(),
                            new File(talendProcessJavaProject.getBundleResourcesFolder().getLocation().toOSString()
                                    + File.separator
                                    + RESOURCES + File.separator + itemName));

                    FilesUtils.copyFile(file.getLocation().toFile(),
                            new File(talendProcessJavaProject.getResourcesFolder().getLocation().toOSString() + File.separator
                                    + itemName));
                } catch (IOException e) {
                    e.printStackTrace();
                }

                result.add(file.getLocation());
            }
        }

        return result;
    }

    /**
     *
     * @param routeItem
     * @param models
     */
    private static Collection<ResourceDependencyModel> getResourceDependencies(
        Collection<ResourceDependencyModel> builtInModels, String userResources) {
        final Collection<ResourceDependencyModel> models = new ArrayList<ResourceDependencyModel>(builtInModels);
        if (userResources != null) {
            for (String id : userResources.split(RouteResourceUtil.COMMA_TAG)) {
                final String[] parts = id.split(REPACE_SLASH_TAG);
                if (parts.length == 2) {
                    final ResourceDependencyModel model = createDependency(parts[0], parts[1]);
                    if (null != model && !builtInModels.contains(model)) {
                        models.add(model);
                    }
                }
            }
        }
        return models;
    }

    private static String getItemName(Item item) {
        Item pi = (Item) item.getParent();

        if (pi instanceof FolderItem) {
            FolderItem folderItem = (FolderItem) pi;

            if (folderItem.getType() == FolderType.SYSTEM_FOLDER_LITERAL) {
                return "";
            } else if (folderItem.getType() == FolderType.FOLDER_LITERAL) {
                String in = getItemName(folderItem);
                return in + folderItem.getProperty().getLabel() + File.separator;
            }
        }

        return null;
    }

    /**
     * @param routeItem
     *
     * @param models
     */
    private static Collection<ResourceDependencyModel> getBuiltInResourceDependencies(final ProcessItem routeItem) {
        if (!containsResourceNode(routeItem)) {
            return Collections.emptyList();
        }
        // TDI-24563
        final IProcess2 process = getProcessFromItem(routeItem);
        if (process == null) {
            return Collections.emptySet();
        }
        return getBuiltInResourceDependencies(process);
    }

    /**
     * @param routeItem
     *
     * @param models
     */
    private static Collection<ResourceDependencyModel> getBuiltInResourceDependencies(final IProcess2 process) {
        final Collection<ResourceDependencyModel> models = new HashSet<ResourceDependencyModel>();
        for (INode node : process.getGraphicalNodes()) {
            final ResourceDependencyModel rdm = createDenpendencyModel(node);
            if (null != rdm && !models.add(rdm)) { // Merge and add
                for (ResourceDependencyModel model : models) {
                    if (model.equals(rdm)) {
                        model.getRefNodes().addAll(rdm.getRefNodes());
                        break;
                    }
                }
            }
        }
        return models;
    }

    @SuppressWarnings("unchecked")
    private static boolean containsResourceNode(final ProcessItem routeItem) {
        for (NodeType node : (Collection<NodeType>) routeItem.getProcess().getNode()) {
            for (ElementParameterType param : (Collection<ElementParameterType>) node.getElementParameter()) {
                if (EParameterFieldType.ROUTE_RESOURCE_TYPE.getName().equals(param.getField())) {
                    return true;
                }
            }
        }
        return false;
    }

    private static IProcess2 getProcessFromItem(ProcessItem item) {
        final IDesignerCoreService designerCoreService = (IDesignerCoreService) GlobalServiceRegister.getDefault()
            .getService(IDesignerCoreService.class);
        if (designerCoreService != null) {
            return (IProcess2) designerCoreService.getProcessFromItem(item);
        }
        return null;
    }

    /**
     * Create ResourceDependencyModel
     *
     * @param paramName
     * @param node
     * @return
     */
    private static ResourceDependencyModel createDenpendencyModel(final INode node) {
        if (!node.isActivate()) {
            return null;
        }
        final IElementParameter idParam = node.getElementParameterFromField(EParameterFieldType.ROUTE_RESOURCE_TYPE);
        if (null == idParam || !idParam.isShow(node.getElementParameters())) {
            return null;
        }
        final IElementParameter versionParam =
            node.getElementParameter(idParam.getName() + ':' + EParameterName.ROUTE_RESOURCE_TYPE_VERSION);
        final ResourceDependencyModel model =
            RouteResourceUtil.createDependency((String) idParam.getValue(), (String)  versionParam.getValue());
        if (null != model) {
            model.setBuiltIn(true);
            model.getRefNodes().add(node.getUniqueName());
        }
        return model;
    }

    /**
     * Copy route resource
     *
     * @param model
     * @throws CoreException
     */
    private static IFile copyResources(final IFolder folder, final ResourceDependencyModel model) {
        final ResourceItem item = model.getItem();
        EList<?> referenceResources = item.getReferenceResources();
        if (referenceResources.isEmpty()) {
            return null;
        }
        final IFile classpathFile = folder.getFile(new Path(model.getClassPathUrl()));
        final ReferenceFileItem refFile = (ReferenceFileItem) referenceResources.get(0);
        try (final InputStream inputStream = new ByteArrayInputStream(refFile.getContent().getInnerContent())) {
            if (classpathFile.exists()) {
                classpathFile.setContents(inputStream, 0, null);
            } else {
                if (!classpathFile.getParent().exists()) {
                    prepareFolder((IFolder) classpathFile.getParent());
                }
                classpathFile.create(inputStream, true, null);
            }
        } catch (CoreException | IOException e) {
            ExceptionHandler.process(e);
        }

        return classpathFile;
    }

    public static void prepareFolder(IFolder folder) throws CoreException {
        IContainer parent = folder.getParent();
        if (IResource.FOLDER == parent.getType()) {
            prepareFolder((IFolder) parent);
        }
        if (!folder.exists()) {
            folder.create(true, true, null);
        }
    }
}
