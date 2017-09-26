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
import org.talend.designer.esb.runcontainer.ui.progress.StartRuntimeProgress;
import org.talend.designer.esb.runcontainer.util.RuntimeConsoleUtil;
import org.talend.designer.runprocess.ui.ERunprocessImages;

/**
 * Start runtime server steps:
 * 
 * 1. check is there a local runtime server exists(start in studio or is an external runtime server)
 * 
 * 2.
 */
public class StartRuntimeAction extends Action {

    private boolean needConsole;

    private String errorMessage;

    private Shell shell;

    public StartRuntimeAction(boolean needConsole, Shell shell) {
        this.needConsole = needConsole;
        this.shell = shell;
        setToolTipText(RunContainerMessages.getString("StartRuntimeAction.Start")); //$NON-NLS-1$
        setImageDescriptor(ImageProvider.getImageDesc(ERunprocessImages.RUN_PROCESS_ACTION));
        setEnabled(!RuntimeServerController.getInstance().isRunning());
    }

    public String getErrorMessage() {
        return this.errorMessage;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {

        ProgressMonitorDialog dialog = new ProgressMonitorDialog(shell);
        try {
            dialog.run(false, true, new IRunnableWithProgress() {

                @Override
                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    new StartRuntimeProgress(true).run(monitor);
                }
            });
        } catch (Throwable e) {
            ExceptionHandler.process(e);
            IStatus status = new Status(IStatus.ERROR, ESBRunContainerPlugin.PLUGIN_ID, e.getMessage(), e);
            if (e.getCause() != null) {
                status = new Status(IStatus.ERROR, ESBRunContainerPlugin.PLUGIN_ID, e.getCause().getMessage(), e.getCause());
            }
            RuntimeErrorDialog.openError(shell,
                    RunContainerMessages.getString("RunContainerPreferencePage.InitailzeDialog3"),
                    RunContainerMessages.getString("RunContainerPreferencePage.InitailzeDialog3"), status);
        }
    }

    public void loadConsole() {
        if (needConsole) {
            RuntimeConsoleUtil.loadConsole();
        }
    }
}
