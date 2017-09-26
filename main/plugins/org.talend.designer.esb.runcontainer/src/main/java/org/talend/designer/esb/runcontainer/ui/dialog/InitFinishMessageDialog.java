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

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

public class InitFinishMessageDialog extends MessageDialog {

    private String[] bundlesName;

    public InitFinishMessageDialog(Shell parentShell, String[] bundlesName) {
        super(parentShell, "Initialize Finished", null, "", MessageDialog.INFORMATION,
                new String[] { IDialogConstants.OK_LABEL }, 0);
        this.bundlesName = bundlesName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.dialogs.IconAndMessageDialog#createMessageArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createMessageArea(Composite composite) {
        // create image
        Image image = getImage();
        if (image != null) {
            imageLabel = new Label(composite, SWT.NULL);
            image.setBackground(imageLabel.getBackground());
            imageLabel.setImage(image);
            GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.BEGINNING).applyTo(imageLabel);
        }
        Link link = new Link(composite, SWT.WRAP);
        link.setText("Local runtime serview has been started, totally installed bundles: <a href=\"#\">" + bundlesName.length
                + "</a>.");

        link.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                ElementListSelectionDialog report = new ElementListSelectionDialog(getShell(), new LabelProvider());
                report.setTitle("Installed bundles:");
                report.setMessage("Search bundle (? = any character, * = any string):");
                report.setElements(bundlesName);
                report.open();
            }
        });
        return composite;
    }

}
