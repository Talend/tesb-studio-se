// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.esb.components.rs.provider;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

    // The plug-in ID
    public static final String PLUGIN_ID = "org.talend.designer.esb.components.rs.provider"; //$NON-NLS-1$

    public static final String REST_URI_PREFERENCE = "restServiceDefaultUri"; //$NON-NLS-1$
    public static final String REST_URI_DEFAULT = "http://127.0.0.1:8090/"; //$NON-NLS-1$

    public static final String DEFAULT_SL_NAMESPACE_PREF="defaultSLNamespace"; //$NON-NLS-1$
    public static final String DEFAULT_SL_NAMESPACE_DEFAULT="http://www.talend.org/rest/"; //$NON-NLS-1$

    // The shared instance
    private static Activator plugin;

    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
        loadCustomProperty();
    }

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

    public void loadCustomProperty() {
        String defaultRestUri = getPreferenceStore().getString(REST_URI_PREFERENCE);
        System.getProperties().put(REST_URI_PREFERENCE, defaultRestUri);
    }

    protected void initializeDefaultPreferences(IPreferenceStore store) {
        store.setDefault(REST_URI_PREFERENCE, REST_URI_DEFAULT);
        store.setDefault(DEFAULT_SL_NAMESPACE_PREF, DEFAULT_SL_NAMESPACE_DEFAULT);
    }

    public static IStatus getStatus(final String message, final Throwable e) {
        String msg = message != null ? message : (e.getMessage() != null) ? e.getMessage() : e.getClass().getName();
        return new Status(IStatus.ERROR, getDefault().getBundle().getSymbolicName(), msg, e);
    }

}
