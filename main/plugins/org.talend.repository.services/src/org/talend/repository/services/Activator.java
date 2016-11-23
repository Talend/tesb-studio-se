package org.talend.repository.services;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.IFileSystem;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.talend.core.PluginChecker;
import org.talend.repository.utils.EsbConfigUtils;

public class Activator extends AbstractUIPlugin {

    // The plug-in ID
    public static final String PLUGIN_ID = "org.talend.repository.services"; //$NON-NLS-1$

    // The shared instance
    private static Activator plugin;

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;

        try {
            copyConfigs();
        } catch (Exception e) {
            getLog().log(new Status(IStatus.ERROR, getBundle().getSymbolicName(), "cannot set Studio ESB configuration", e));
        }
    }

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
    public static Activator getDefault() {
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
                    return; // ignore to do not overwrite possible user changes in configuration files
                }
            } else {
                String esbConfigFileName = esbConfigFileStore.fetchInfo().getName();
                if ("microservice".equals(esbConfigFileName) && PluginChecker.isTIS()) {
                    try {
                        esbConfigFileStore.copy(esbConfigsTargetFolder.getChild(esbConfigFileName), EFS.NONE, null);
                    } catch (CoreException e) {
                        return; // ignore to do not overwrite possible user changes in configuration files
                    }
                }
            }
        }
    }
}
