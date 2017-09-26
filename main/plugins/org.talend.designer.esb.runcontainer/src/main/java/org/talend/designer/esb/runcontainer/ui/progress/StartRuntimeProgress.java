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

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.designer.esb.runcontainer.core.ESBRunContainerPlugin;
import org.talend.designer.esb.runcontainer.i18n.RunContainerMessages;
import org.talend.designer.esb.runcontainer.preferences.RunContainerPreferenceInitializer;
import org.talend.designer.esb.runcontainer.server.RuntimeServerController;
import org.talend.designer.esb.runcontainer.util.JMXUtil;
import org.talend.designer.esb.runcontainer.util.RuntimeConsoleUtil;

public class StartRuntimeProgress extends RuntimeProgress {

    private boolean loadConsole;

    public StartRuntimeProgress(boolean loadConsole) {
        this.loadConsole = loadConsole;
    }

    @Override
    public void run(IProgressMonitor parentMonitor) throws InvocationTargetException, InterruptedException {
        SubMonitor subMonitor = SubMonitor.convert(parentMonitor, 10);
        subMonitor.setTaskName(RunContainerMessages.getString("StartRuntimeAction.Starting")); //$NON-NLS-1$
        if (!checkRunning()) {
            try {
                IPreferenceStore store = ESBRunContainerPlugin.getDefault().getPreferenceStore();
                Process proc = RuntimeServerController.getInstance().startLocalRuntimeServer(
                        store.getString(RunContainerPreferenceInitializer.P_ESB_RUNTIME_LOCATION));
                int i = 0;
                String dot = "."; //$NON-NLS-1$
                while (JMXUtil.createJMXconnection() == null && ++i < 11 && !subMonitor.isCanceled() && proc.isAlive()) {
                    subMonitor.subTask(RunContainerMessages.getString("StartRuntimeAction.Try") + dot); //$NON-NLS-1$
                    dot += "."; //$NON-NLS-1$
                    subMonitor.worked(1);
                    Thread.sleep(3000);
                }
                if (!proc.isAlive()) {
                    RuntimeServerController.getInstance().stopLocalRuntimeServer();
                    throw new InterruptedException(RunContainerMessages.getString("RunContainerPreferencePage.InitailzeDialog8",
                            proc.exitValue()));
                }
                if (JMXUtil.createJMXconnection() == null) {
                    throw new InterruptedException(RunContainerMessages.getString("RunContainerPreferencePage.InitailzeDialog5"));
                }
            } catch (Exception e) {
                ExceptionHandler.process(e);
                throw new InvocationTargetException(e, e.getMessage());
            }
        }
        loadConsole();
    }

    public void loadConsole() {
        if (loadConsole) {
            RuntimeConsoleUtil.loadConsole();
        }
    }
}
