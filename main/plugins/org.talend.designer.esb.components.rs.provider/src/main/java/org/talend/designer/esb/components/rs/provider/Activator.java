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
package org.talend.designer.esb.components.rs.provider;

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

    public static final String DEFAULT_SL_NAMESPACE_PREF = "defaultSLNamespace"; //$NON-NLS-1$

    public static final String DEFAULT_SL_NAMESPACE_DEFAULT = "http://www.talend.org/rest/"; //$NON-NLS-1$

    public static final String IGNORE_SL_STUDIO = "ignoreSLStudio"; //$NON-NLS-1$
    
    public static final boolean IGNORE_SL_STUDIO_DEFAULT = false; //$NON-NLS-1$

    // The shared instance
    private static Activator plugin;

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
        loadCustomProperty();
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

    public void loadCustomProperty() {
        String defaultRestUri = getPreferenceStore().getString(REST_URI_PREFERENCE);
        System.getProperties().put(REST_URI_PREFERENCE, defaultRestUri);
        String ignoreSLStudio = getPreferenceStore().getString(IGNORE_SL_STUDIO);
        System.getProperties().put(IGNORE_SL_STUDIO, ignoreSLStudio);
    }

}
