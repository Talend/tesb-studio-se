package org.talend.designer.camel.resource;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class RouteResourceActivator extends AbstractUIPlugin {

    // The plug-in ID
    public static final String PLUGIN_ID = "org.talend.designer.camel.resource"; //$NON-NLS-1$

    // The shared instance
    private static RouteResourceActivator plugin;

    /**
     * Returns the shared instance
     * 
     * @return the shared instance
     */
    public static RouteResourceActivator getDefault() {
        return plugin;
    }

    public RouteResourceActivator() {
    }

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }

    public static ImageDescriptor createImageDesc(String path) {
        return imageDescriptorFromPlugin(PLUGIN_ID, path);
    }
}
