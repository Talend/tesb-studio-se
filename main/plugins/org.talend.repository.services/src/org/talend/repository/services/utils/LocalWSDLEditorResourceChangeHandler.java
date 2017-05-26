// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.repository.services.utils;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.wst.wsdl.ui.internal.InternalWSDLMultiPageEditor;
import org.eclipse.wst.wsdl.ui.internal.WSDLEditorResourceChangeHandler;
import org.talend.designer.core.ui.views.jobsettings.JobSettings;

@SuppressWarnings("restriction")
public class LocalWSDLEditorResourceChangeHandler extends WSDLEditorResourceChangeHandler {

    protected InternalPartListener partListener;

    public LocalWSDLEditorResourceChangeHandler(InternalWSDLMultiPageEditor wsdlEditor) {
        super(wsdlEditor);
        partListener = new InternalPartListener();
    }

    @Override
    public void attach() {
        ResourcesPlugin.getWorkspace().addResourceChangeListener(resourceChangeListener);
        wsdlEditor.getSite().getWorkbenchWindow().getPartService().addPartListener(partListener);
    }

    @Override
    public void dispose() {
        ResourcesPlugin.getWorkspace().removeResourceChangeListener(resourceChangeListener);
        wsdlEditor.getSite().getWorkbenchWindow().getPartService().removePartListener(partListener);
    }

    class InternalPartListener implements IPartListener {

        public void partActivated(IWorkbenchPart part) {
            if (part == wsdlEditor) {
                if (isUpdateRequired) {
                    isUpdateRequired = false;
                    performReload();
                }
            }
        }

        public void partBroughtToTop(IWorkbenchPart part) {
            if (part instanceof LocalWSDLEditor) {
                JobSettings.switchToCurJobSettingsView();
            }
        }

        public void partClosed(IWorkbenchPart part) {
            if (part instanceof LocalWSDLEditor) {
                JobSettings.switchToCurJobSettingsView();
            }
        }

        public void partDeactivated(IWorkbenchPart part) {
        }

        public void partOpened(IWorkbenchPart part) {
        }
    }
}
