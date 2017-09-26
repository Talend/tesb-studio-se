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
package org.talend.designer.esb.runcontainer.ui.dialog;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;

public class RuntimeErrorDialog extends ErrorDialog {

    private IStatus initStatus;

    public RuntimeErrorDialog(Shell parentShell, String dialogTitle, String message, IStatus status, int displayMask) {
        super(parentShell, dialogTitle, message, status, displayMask);
        this.initStatus = status;
    }

    public static int openError(Shell parent, String dialogTitle, String message, IStatus status) {
        return openError(parent, dialogTitle, message, status, IStatus.OK | IStatus.INFO | IStatus.WARNING | IStatus.ERROR);
    }

    public static int openError(Shell parentShell, String title, String message, IStatus status, int displayMask) {
        RuntimeErrorDialog dialog = new RuntimeErrorDialog(parentShell, title, message, status, displayMask);
        return dialog.open();
    }

    @Override
    public int open() {
        // patch for swtbot test
        boolean mode = ErrorDialog.AUTOMATED_MODE;
        ErrorDialog.AUTOMATED_MODE = false;
        int code = super.open();
        ErrorDialog.AUTOMATED_MODE = mode;
        return code;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.dialogs.ErrorDialog#createDropDownList(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected List createDropDownList(Composite parent) {
        List list = super.createDropDownList(parent);
        for (StackTraceElement st : initStatus.getException().getStackTrace()) {
            list.add(st.toString());
        }
        return list;
    }
}