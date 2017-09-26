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
package org.talend.designer.esb.runcontainer.ui.progress;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.talend.designer.esb.runcontainer.core.ESBRunContainerPlugin;
import org.talend.designer.esb.runcontainer.preferences.RunContainerPreferenceInitializer;
import org.talend.designer.esb.runcontainer.util.JMXUtil;

public abstract class RuntimeProgress implements IRunnableWithProgress {

    /**
     * Check if the same runtime server is existing, connect to it directly.
     * 
     * @return
     * @throws Exception
     */
    public boolean checkRunning() throws InvocationTargetException {
        IPreferenceStore store = ESBRunContainerPlugin.getDefault().getPreferenceStore();
        File runtimeLocation = new File(store.getString(RunContainerPreferenceInitializer.P_ESB_RUNTIME_LOCATION));
        try {
            if (JMXUtil.createJMXconnection() != null) {
                File karafHome;
                karafHome = new File(JMXUtil.getSystemPropertie("karaf.home").replaceFirst("\\\\:", ":")); //$NON-NLS-1$ //$NON-NLS-2$
                // is the same runtime, but it is running
                if (runtimeLocation.getAbsolutePath().equals(karafHome.getAbsolutePath())) {
                    return true;
                } else {
                    // different runtime is running
                    JMXUtil.closeJMXConnection();
                    throw new InterruptedException("Another runtime server instance is running (Runtime home :" + karafHome
                            + "), please stop it first.");
                }
            }
        } catch (Exception e) {
            throw new InvocationTargetException(e, e.getMessage());
        }
        return false;
    }

}