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
package org.talend.designer.esb.runcontainer.ui.wizard;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.Wizard;
import org.talend.designer.esb.runcontainer.i18n.RunContainerMessages;
import org.talend.designer.esb.runcontainer.preferences.RunContainerPreferenceInitializer;
import org.talend.designer.esb.runcontainer.util.FileUtil;

public class AddRuntimeWizard extends Wizard {

    private String target;

    private AddRuntimeDirWizardPage dirPage;

    public AddRuntimeWizard(String target) {
        this.target = target;
        setWindowTitle(RunContainerMessages.getString("AddRuntimeWizard.Title")); //$NON-NLS-1$
    }

    @Override
    public void addPages() {
        dirPage = new AddRuntimeDirWizardPage();
        addPage(dirPage);
    }

    @Override
    public boolean performFinish() {
        if (dirPage.shouldInstallNewContainer()) {
            target = dirPage.getTargetDir();
            if (target == null) {
                target = RunContainerPreferenceInitializer.P_DEFAULT_ESB_RUNTIME_LOCATION;
            }
            try {
                getContainer().run(true, true, new IRunnableWithProgress() {

                    @Override
                    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                        try {
                            FileUtil.unzipContainer(dirPage.getRuntimeArchive(), target, monitor);
                        } catch (IOException e) {
                            throw new InterruptedException(RunContainerMessages.getString("AddRuntimeWizard.ErrorCopy"));
                        }
                    }
                });
            } catch (Exception e) {
                MessageDialog.openError(getContainer().getShell(),
                        RunContainerMessages.getString("AddRuntimeWizard.ErrorCopy"), ExceptionUtils.getStackTrace(e)); //$NON-NLS-1$
            }
        } else {
            target = dirPage.getRuntimeHome();
        }
        /*
         * if (dirPage.isCopyNeeded()) { try { String runtimeHome = dirPage.getRuntimeHome(); getContainer().run(true,
         * true, new IRunnableWithProgress() {
         * 
         * @Override public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
         * try { FileUtil.copyContainer(runtimeHome, target, monitor); } catch (IOException e) { } }
         * 
         * }); } catch (Exception e) { MessageDialog.openError(this.getShell(),
         * RunContainerMessages.getString("AddRuntimeWizard.ErrorCopy"), ExceptionUtils.getStackTrace(e)); //$NON-NLS-1$
         * return false; } } else { target = dirPage.getRuntimeHome(); }
         */
        // MessageDialog.openInformation(this.getShell(), "Not implemented", "Only copying is supported");

        // return false;
        return true;
    }

    public String getTarget() {
        return target;
    }

    // @Override
    // public boolean canFinish() {
    // return super.canFinish(); // dirPage.isPageComplete();
    // }
}
