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
package org.talend.designer.esb.webservice.ui.dialog;

import org.eclipse.jface.wizard.Wizard;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.ui.branding.IBrandingService;
import org.talend.designer.esb.webservice.WebServiceNode;
import org.talend.designer.esb.webservice.ui.WebServiceUI;

public class WebServiceDialog extends Wizard {

    private final WebServiceNode webServiceComponent;

    private WebServiceUI webServiceUI;

    public WebServiceDialog(WebServiceNode webServiceComponent) {
        this.webServiceComponent = webServiceComponent;
        setWindowTitle(
                ((IBrandingService) GlobalServiceRegister.getDefault().getService(IBrandingService.class)).getFullProductName()
                        + " - " + webServiceComponent.getComponent().getName() + //$NON-NLS-1$
                        " - " + webServiceComponent.getUniqueName()); //$NON-NLS-1$
        setNeedsProgressMonitor(true);
    }

    @Override
    public void addPages() {
        webServiceUI = new WebServiceUI(webServiceComponent);
        addPage(webServiceUI);
    }

    @Override
    public boolean performFinish() {
        return webServiceUI.performFinish();
    }

}
