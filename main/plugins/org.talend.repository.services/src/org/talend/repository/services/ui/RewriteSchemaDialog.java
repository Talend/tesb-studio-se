// ============================================================================
//
// Copyright (C) 2006-2021 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.repository.services.ui;

import java.util.Collection;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.talend.core.model.properties.XmlFileConnectionItem;

/**
 * DOC hwang class global comment. Detailled comment
 */
public class RewriteSchemaDialog extends Dialog {

    private Collection<XmlFileConnectionItem> xmlObjs;

    private XmlTableForm tableForm;

    /**
     * DOC hwang RewriteSchemaDialog constructor comment.
     *
     * @param parentShell
     */
    public RewriteSchemaDialog(Shell parentShell, Collection<XmlFileConnectionItem> xmlObjs) {
        super(parentShell);
        this.xmlObjs = xmlObjs;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        tableForm = new XmlTableForm(container, xmlObjs);
        tableForm.setLayoutData(new GridData(GridData.FILL_BOTH));
        tableForm.setListener(new XmlTableForm.ICompleteListener() {
            public void setComplete(boolean complete) {
                getButton(IDialogConstants.OK_ID).setEnabled(complete);
            }
        });
        return container;
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("Import WSDL Schemas");
    }

    @Override
    protected boolean isResizable() {
        return true;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        super.createButtonsForButtonBar(parent);
        getButton(IDialogConstants.OK_ID).setEnabled(false);
    }

    @Override
    protected void okPressed() {
        xmlObjs = tableForm.getSelectionItems();
        super.okPressed();
    }

    public Collection<XmlFileConnectionItem> getSelectionTables() {
        return xmlObjs;
    }

}
