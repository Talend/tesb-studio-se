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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.talend.designer.esb.runcontainer.preferences.RunContainerPreferencePage;

public class OpenRuntimePrefsAction extends Action {


    public OpenRuntimePrefsAction() {
        setToolTipText("Open Settings"); //$NON-NLS-1$
        setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_DEF_VIEW));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
        PreferenceDialog dlg = new PreferenceDialog(Display.getDefault().getActiveShell(), PlatformUI.getWorkbench()
                .getPreferenceManager());
        dlg.setSelectedNode(RunContainerPreferencePage.ID);
        dlg.open();

    }
}
