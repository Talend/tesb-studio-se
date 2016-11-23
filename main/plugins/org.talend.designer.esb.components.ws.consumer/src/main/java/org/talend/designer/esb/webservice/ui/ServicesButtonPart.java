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
package org.talend.designer.esb.webservice.ui;

import java.util.EventListener;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.ui.dialog.RepositoryReviewDialog;

interface ServiceSelectionListener extends EventListener {

    void serviceNodeSelected(RepositoryNode serviceNode);
}

/**
 * Provide a dialog to select a service node from Services. (separated from {@link WebServiceUI}
 */
public class ServicesButtonPart extends AbstractButtonPart<ServiceSelectionListener> {

    public ServicesButtonPart(ServiceSelectionListener eventListener) {
        super(eventListener);
    }

    @Override
    protected void buttonSelected(SelectionEvent e) {
        RepositoryReviewDialog dialog = new RepositoryReviewDialog(getShell(), ERepositoryObjectType.METADATA,
                "SERVICES:OPERATION") {

            @Override
            protected boolean isSelectionValid(SelectionChangedEvent event) {
                IStructuredSelection selection = (IStructuredSelection) event.getSelection();
                if (selection.size() == 1) {
                    return true;
                }
                return false;
            }

            @Override
            protected Control createDialogArea(Composite parent) {
                return createDialogArea(parent, "org.talend.rcp.perspective");
            }
        };

        int open = dialog.open();
        if (open == Dialog.OK) {
            RepositoryNode result = dialog.getResult();
            if (result != null) {
                listener.serviceNodeSelected(result);
            }
        }
    }

    @Override
    protected String getMessageKey() {
        return "WebServiceUI.Services";
    }

    @Override
    protected Image getImage() {
        return getImageFromBundle("org.talend.repository.services", "icons/services.png");
    }

}
