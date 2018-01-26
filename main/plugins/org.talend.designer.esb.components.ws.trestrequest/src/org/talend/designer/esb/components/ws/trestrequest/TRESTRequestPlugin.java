package org.talend.designer.esb.components.ws.trestrequest;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class TRESTRequestPlugin extends AbstractUIPlugin {

    // The plug-in ID
    public static final String PLUGIN_ID = "org.talend.designer.esb.components.ws.trestrequest"; //$NON-NLS-1$

    // The shared instance
    private static TRESTRequestPlugin plugin;

    /**
     * The constructor
     */
    public TRESTRequestPlugin() {
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

    /**
     * Returns the shared instance
     *
     * @return the shared instance
     */
    public static TRESTRequestPlugin getDefault() {
        return plugin;
    }

}
