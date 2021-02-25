package org.talend.repository.services;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.IFileSystem;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.core.PluginChecker;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.Property;
import org.talend.core.model.properties.impl.ProcessItemImpl;
import org.talend.core.model.relationship.Relation;
import org.talend.core.model.relationship.RelationshipItemBuilder;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.maven.model.TalendMavenConstants;
import org.talend.designer.maven.tools.AggregatorPomsHelper;
import org.talend.repository.documentation.ERepositoryActionName;
import org.talend.repository.services.model.services.ServiceItem;
import org.talend.repository.utils.EsbConfigUtils;

public class ServicesPlugin extends AbstractUIPlugin {

    private static final String CLIENTSTORE_CONDUITS_JKS = "clientstore-conduits.jks";

	// The plug-in ID
    public static final String PLUGIN_ID = "org.talend.repository.services"; //$NON-NLS-1$

    // The shared instance
    private static ServicesPlugin plugin;

    private static final String TESB_PROVIDER_REQUEST = "tESBProviderRequest";

    private static final String TESB_PROVIDER_RESPONSE = "tESBProviderResponse";

    /*
     * (non-Javadoc)
     *
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;

        try {
            copyConfigs();
        } catch (Exception e) {
            getLog().log(new Status(IStatus.ERROR, getBundle().getSymbolicName(), "cannot set Studio ESB configuration", e));
        }

        ProxyRepositoryFactory.getInstance().addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent event) {
                String propertyName = event.getPropertyName();
                Object newValue = event.getNewValue();
                if (propertyName.equals(ERepositoryActionName.IMPORT.getName())) {
                    caseImport(propertyName, newValue);
                }
            }

        });
    }

    /**
     * @param propertyName
     * @param newValue
     */
    protected void caseImport(String propertyName, Object newValue) {
        if (newValue instanceof Set) {
            Set<Item> importItems = (Set<Item>) newValue;
            boolean includeServiceItem = false;
            for (Item item : importItems) {
                if (item instanceof ServiceItem) {
                    includeServiceItem = true;
                    break;
                }
            }
            if (includeServiceItem) {
                boolean needSave = false;
                for (Item item : importItems) {
                    if (isDataServiceJob(item)) {
                        createServiceRelation(item, false);
                        needSave = true;
                        removeFromParentModules(item);
                    }
                }
                if (needSave) {
                    RelationshipItemBuilder.getInstance().saveRelations();
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    @Override
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }

    /**
     * Returns the shared instance
     *
     * @return the shared instance
     */
    public static ServicesPlugin getDefault() {
        return plugin;
    }

    /**
     * Returns an image descriptor for the image file at the given plug-in relative path
     *
     * @param path the path
     * @return the image descriptor
     */
    public static ImageDescriptor getImageDescriptor(String path) {
        return imageDescriptorFromPlugin(PLUGIN_ID, path);
    }

    private void copyConfigs() throws CoreException, IOException, URISyntaxException {

        // find ESB configuration files folder in plug-in
        URL esbConfigsFolderUrl = FileLocator.find(getBundle(), new Path("esb"), null);
        if (null == esbConfigsFolderUrl) {
            getLog().log(new Status(IStatus.WARNING, getBundle().getSymbolicName(), "cannot find ESB configuration files"));
            return;
        }

        // resolve plug-in ESB config folder URL into file protocol URL
        esbConfigsFolderUrl = FileLocator.toFileURL(esbConfigsFolderUrl);

        // create ESB configuration folder under Studio instalation
        IFileSystem fileSystem = EFS.getLocalFileSystem();
        IFileStore esbConfigsTargetFolder = fileSystem.getStore(EsbConfigUtils.getEclipseEsbFolder().toURI());
        esbConfigsTargetFolder = esbConfigsTargetFolder.mkdir(EFS.SHALLOW, null);

        // retrieve all ESB configuration files packed inside plug-in
        File fileEsbConfigFolder = new File(esbConfigsFolderUrl.getPath());
        IFileStore esbConfigsFolderStore = fileSystem.getStore(fileEsbConfigFolder.toURI());
        IFileStore[] esbConfigsFolderStoreChildren = esbConfigsFolderStore.childStores(EFS.NONE, null);
        if (0 == esbConfigsFolderStoreChildren.length) {
            getLog().log(new Status(IStatus.WARNING, getBundle().getSymbolicName(), "cannot find any ESB configuration files"));
            return;
        }

        // try to copy ESB configuration files (without overwriting)
        for (IFileStore esbConfigFileStore : esbConfigsFolderStoreChildren) {
            if (!esbConfigFileStore.fetchInfo().isDirectory()) {
                String esbConfigFileName = esbConfigFileStore.fetchInfo().getName();
                try {
                    esbConfigFileStore.copy(esbConfigsTargetFolder.getChild(esbConfigFileName), EFS.NONE, null);
                } catch (CoreException e) {
                    // ignore to do not overwrite possible user changes in configuration files
                }
            } else {
                String esbConfigFileName = esbConfigFileStore.fetchInfo().getName();
                if ("microservice".equals(esbConfigFileName) && PluginChecker.isTIS()) {
                    try {
                        esbConfigFileStore.copy(esbConfigsTargetFolder.getChild(esbConfigFileName), EFS.NONE, null);
                    } catch (CoreException e) {
                        // ignore to do not overwrite possible user changes in configuration files
                    }
                    try {
	                    IFileStore clientStoreConduits = esbConfigFileStore.getChild(CLIENTSTORE_CONDUITS_JKS);
	                    clientStoreConduits.copy(esbConfigsTargetFolder.getChild("microservice" + "/" + CLIENTSTORE_CONDUITS_JKS), EFS.OVERWRITE, null);
                    } catch(CoreException e) {
                        // ignore to do not overwrite possible user changes in configuration files
                    }
                }
            }
        }
    }

    private boolean isDataServiceJob(Item item) {
        if (ProcessItemImpl.class == item.getClass()) {
            ProcessItemImpl processItem = (ProcessItemImpl) item;
            for (NodeType node : (Collection<NodeType>) processItem.getProcess().getNode()) {
                if (TESB_PROVIDER_REQUEST.equals(node.getComponentName())
                        || TESB_PROVIDER_RESPONSE.equals(node.getComponentName())) {
                    return true;
                }
            }
        }
        return false;
    }

    private void createServiceRelation(Item item, boolean saveProject) {
        Set<Relation> relationSet = new HashSet<Relation>();
        Relation addedRelation = new Relation();
        addedRelation.setId(item.getProperty().getId());
        addedRelation.setType(RelationshipItemBuilder.SERVICES_RELATION);
        addedRelation.setVersion(item.getProperty().getVersion());
        relationSet.add(addedRelation);

        Map<Relation, Set<Relation>> merge = new HashMap<Relation, Set<Relation>>();
        Relation processRelation = new Relation();
        processRelation.setId(item.getProperty().getId());
        processRelation.setType(RelationshipItemBuilder.JOB_RELATION);
        processRelation.setVersion(item.getProperty().getVersion());

        merge.put(processRelation, relationSet);

        RelationshipItemBuilder.mergeRelationship(RelationshipItemBuilder.getInstance().getCurrentProjectItemsRelations(), merge);

        if (saveProject) {
            RelationshipItemBuilder.getInstance().saveRelations();
        }
    }

    private void removeFromParentModules(Item item) {
        // remove from parant pom
        try {
            Property property = item.getProperty();
            if (property != null) {
                IFile jobPom = AggregatorPomsHelper.getItemPomFolder(property).getFile(TalendMavenConstants.POM_FILE_NAME);
                AggregatorPomsHelper.removeFromParentModules(jobPom);
            }
        } catch (Exception ex) {
            ExceptionHandler.process(ex);
        }
    }
}
