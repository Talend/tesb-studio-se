package org.talend.designer.esb.webservice;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.IESBService;

/**
 * The activator class controls the plug-in life cycle
 */
public class WebServiceComponentPlugin extends AbstractUIPlugin {

    // The shared instance
    private static WebServiceComponentPlugin plugin;

    private static boolean hasRepositoryServices;

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;

        hasRepositoryServices = GlobalServiceRegister.getDefault().isServiceRegistered(IESBService.class);
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
    public static WebServiceComponentPlugin getDefault() {
        return plugin;
    }

    public static boolean hasRepositoryServices() {
        return hasRepositoryServices;
    }

    public static IStatus getStatus(final String message, final Throwable e) {
        String msg = message != null ? message : (e.getMessage() != null) ? e.getMessage() : e.getClass().getName();
        return new Status(IStatus.ERROR, getDefault().getBundle().getSymbolicName(), msg, e);
    }

}
