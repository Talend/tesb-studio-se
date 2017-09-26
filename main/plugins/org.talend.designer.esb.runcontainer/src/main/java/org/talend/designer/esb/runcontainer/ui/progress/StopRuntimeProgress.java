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
import org.talend.commons.exception.ExceptionHandler;
import org.talend.designer.esb.runcontainer.i18n.RunContainerMessages;
import org.talend.designer.esb.runcontainer.server.RuntimeServerController;

public class StopRuntimeProgress extends RuntimeProgress {

    @Override
    public void run(IProgressMonitor parentMonitor) throws InvocationTargetException, InterruptedException {
        SubMonitor subMonitor = SubMonitor.convert(parentMonitor, 10);
        subMonitor.setTaskName(RunContainerMessages.getString("HaltRuntimeAction.Stoping")); //$NON-NLS-1$
        if (checkRunning()) {
            if (RuntimeServerController.getInstance().isRunning()) {
                try {
                    RuntimeServerController.getInstance().stopRuntimeServer();
                    int i = 0;
                    String dot = "."; //$NON-NLS-1$
                    // JMXUtil.connectToRuntime() != null
                    while (RuntimeServerController.getInstance().isRunning() && i < 11 && !subMonitor.isCanceled()) {
                        subMonitor.setTaskName(RunContainerMessages.getString("HaltRuntimeAction.Task") + dot); //$NON-NLS-1$
                        dot += "."; //$NON-NLS-1$
                        subMonitor.worked(1);
                        Thread.sleep(3000);
                    }
                    if (RuntimeServerController.getInstance().isRunning()) {
                        throw new InterruptedException("Stop runtime server failed, please try again or stop it manually.");
                    }
                } catch (Exception e) {
                    ExceptionHandler.process(e);
                    e.printStackTrace();
                    throw new InvocationTargetException(e);
                }
            }
        }
    }

}
