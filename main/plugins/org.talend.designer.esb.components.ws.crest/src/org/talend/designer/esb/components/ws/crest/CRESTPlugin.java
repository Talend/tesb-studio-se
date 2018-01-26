package org.talend.designer.esb.components.ws.crest;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class CRESTPlugin extends AbstractUIPlugin {

    // The plug-in ID
    public static final String PLUGIN_ID = "org.talend.designer.esb.components.ws.crest"; //$NON-NLS-1$

    // The shared instance
    private static CRESTPlugin plugin;

    /**
     * The constructor
     */
    public CRESTPlugin() {
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
    public static CRESTPlugin getDefault() {
        return plugin;
    }

}
