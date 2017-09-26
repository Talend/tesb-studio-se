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
package org.talend.designer.esb.runcontainer.preferences;

import java.io.File;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.talend.designer.esb.runcontainer.core.ESBRunContainerPlugin;

/**
 * Class used to initialize default preference values.
 */
public class RunContainerPreferenceInitializer extends AbstractPreferenceInitializer {

    //
    public static final String P_ESB_RUNTIME_LOCATION = "ESB_RUNTIME_LOCATION";

    public static final String P_DEFAULT_ESB_RUNTIME_LOCATION = System.getProperty("user.dir") + File.separator + "esb"
            + File.separator + "container";

    //
    public static final String P_ESB_RUNTIME_HOST = "P_ESB_RUNTIME_HOST";

    public static final String P_DEFAULT_ESB_RUNTIME_HOST = "localhost";

    //
    public static final String P_ESB_RUNTIME_PORT = "P_ESB_RUNTIME_PORT";

    public static final int P_DEFAULT_ESB_RUNTIME_PORT = 1099;

    //
    public static final String P_ESB_RUNTIME_USERNAME = "ESB_RUNTIME_USERNAME";

    public static final String P_DEFAULT_ESB_RUNTIME_USERNAME = "tadmin";

    //
    public static final String P_ESB_RUNTIME_PASSWORD = "ESB_RUNTIME_PASSWORD";

    public static final String P_DEFAULT_ESB_RUNTIME_PASSWORD = "tadmin";

    //
    // public static final String P_ESB_RUNTIME_MAVEN_SCRIPT = "ESB_RUNTIME_MAVEN_SCRIPT";

    // public static final boolean P_DEFAULT_ESB_RUNTIME_MAVEN_SCRIPT = false;

    //
    public static final String P_ESB_RUNTIME_CLEAN_CACHE = "ESB_RUNTIME_CLEAN_CACHE";

    public static final boolean P_DEFAULT_ESB_RUNTIME_CLEAN_CACHE = false;

    //
    public static final String P_ESB_RUNTIME_JMX = "ESB_RUNTIME_JMX";

    public static final boolean P_DEFAULT_ESB_RUNTIME_JMX = true;

    //
    public static final String P_ESB_RUNTIME_JMX_PORT = "ESB_RUNTIME_JMX_PORT";

    public static final int P_DEFAULT_ESB_RUNTIME_JMX_PORT = 44444;

    //
    public static final String P_ESB_RUNTIME_SYS_LOG = "ESB_RUNTIME_SYS_LOG";

    public static final boolean P_DEFAULT_ESB_RUNTIME_SYS_LOG = false;

    //
    public static final String P_ESB_RUNTIME_INSTANCE = "ESB_RUNTIME_INSTANCE";

    public static final String P_DEFAULT_ESB_RUNTIME_INSTANCE = "trun";

    //
    public static final String P_ESB_IN_OSGI = "ESB_RUNTIME_IN_OSGI";

    public static final boolean P_DEFAULT_ESB_IN_OSGI = false;

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
     */
    @Override
    public void initializeDefaultPreferences() {
        IPreferenceStore store = ESBRunContainerPlugin.getDefault().getPreferenceStore();
        store.setDefault(P_ESB_RUNTIME_LOCATION, P_DEFAULT_ESB_RUNTIME_LOCATION);
        store.setDefault(P_ESB_RUNTIME_HOST, P_DEFAULT_ESB_RUNTIME_HOST);
        store.setDefault(P_ESB_RUNTIME_PORT, P_DEFAULT_ESB_RUNTIME_PORT);
        store.setDefault(P_ESB_RUNTIME_USERNAME, P_DEFAULT_ESB_RUNTIME_USERNAME);
        store.setDefault(P_ESB_RUNTIME_PASSWORD, P_DEFAULT_ESB_RUNTIME_PASSWORD);
        // store.setDefault(P_ESB_RUNTIME_MAVEN_SCRIPT, P_DEFAULT_ESB_RUNTIME_MAVEN_SCRIPT);
        store.setDefault(P_ESB_RUNTIME_CLEAN_CACHE, P_DEFAULT_ESB_RUNTIME_CLEAN_CACHE);
        store.setDefault(P_ESB_RUNTIME_JMX, P_DEFAULT_ESB_RUNTIME_JMX);
        store.setDefault(P_ESB_RUNTIME_JMX_PORT, P_DEFAULT_ESB_RUNTIME_JMX_PORT);
        store.setDefault(P_ESB_RUNTIME_SYS_LOG, P_DEFAULT_ESB_RUNTIME_SYS_LOG);
        store.setDefault(P_ESB_RUNTIME_INSTANCE, P_DEFAULT_ESB_RUNTIME_INSTANCE);
        store.setDefault(P_ESB_IN_OSGI, P_DEFAULT_ESB_IN_OSGI);
    }
}
