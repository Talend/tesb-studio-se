package org.talend.designer.esb.components.router;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

    private static BundleContext context;

    // The shared instance
    private static Activator plugin;

    static BundleContext getContext() {
        return context;
    }

    public void start(BundleContext bundleContext) throws Exception {
        Activator.context = bundleContext;
        this.plugin = this;
    }

    public void stop(BundleContext bundleContext) throws Exception {
        Activator.context = null;
        this.plugin = null;
    }

    public static Activator getDefault() {
        return plugin;
    }

    public static Bundle getBundle() {
        return context.getBundle();
    }

}
