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
package org.talend.designer.esb.runcontainer.ui.actions;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.designer.esb.runcontainer.core.ESBRunContainerPlugin;
import org.talend.designer.esb.runcontainer.i18n.RunContainerMessages;
import org.talend.designer.esb.runcontainer.server.RuntimeServerController;
import org.talend.designer.esb.runcontainer.ui.dialog.RuntimeErrorDialog;
import org.talend.designer.esb.runcontainer.ui.progress.StopRuntimeProgress;
import org.talend.designer.runprocess.ui.ERunprocessImages;

public class StopRuntimeAction extends Action {

    private String errorMessage;

    private Shell shell;

    public StopRuntimeAction(Shell shell) {
        this.shell = shell;
        setToolTipText(RunContainerMessages.getString("HaltRuntimeAction.Stop")); //$NON-NLS-1$
        setImageDescriptor(ImageProvider.getImageDesc(ERunprocessImages.KILL_PROCESS_ACTION));
        setEnabled(RuntimeServerController.getInstance().isRunning());
    }

    public String getErrorMessage() {
        return this.errorMessage;
    }

    @Override
    public void run() {

        ProgressMonitorDialog dialog = new ProgressMonitorDialog(shell);
        try {
            dialog.run(false, true, new IRunnableWithProgress() {

                @Override
                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    new StopRuntimeProgress().run(monitor);
                }
            });
        } catch (Throwable e) {
            ExceptionHandler.process(e);
            IStatus status = new Status(IStatus.ERROR, ESBRunContainerPlugin.PLUGIN_ID, e.getMessage(), e);
            if (e.getCause() != null) {
                status = new Status(IStatus.ERROR, ESBRunContainerPlugin.PLUGIN_ID, e.getCause().getMessage(), e.getCause());
            }
            RuntimeErrorDialog.openError(shell,
                    RunContainerMessages.getString("RunContainerPreferencePage.InitailzeDialog6"),
                    RunContainerMessages.getString("RunContainerPreferencePage.InitailzeDialog6"), status);
        }
    }
}
