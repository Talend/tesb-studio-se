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
package org.talend.designer.esb.components.ws.crest;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;

/**
 * DOC dsergent class global comment. Detailled comment
 */
public class MessageDialogWithLink extends MessageDialog {

    private String linkUrl;

    /**
     * DOC dsergent MessageDialogWithLink constructor comment.
     * 
     * @param parentShell
     * @param dialogTitle
     * @param dialogTitleImage
     * @param dialogMessage
     * @param dialogImageType
     * @param dialogButtonLabels
     * @param defaultIndex
     */
    public MessageDialogWithLink(Shell parentShell, String dialogTitle, Image dialogTitleImage, String dialogMessage,
            String linkUrl, int dialogImageType, String[] dialogButtonLabels, int defaultIndex) {
        super(parentShell, dialogTitle, dialogTitleImage, dialogMessage, dialogImageType, dialogButtonLabels, defaultIndex);

        this.linkUrl = linkUrl;
    }

    @Override
    protected Control createMessageArea(Composite composite) {
        Image image = getImage();
        if (image != null) {
            imageLabel = new Label(composite, SWT.NULL);
            image.setBackground(imageLabel.getBackground());
            imageLabel.setImage(image);
            GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.BEGINNING).applyTo(imageLabel);
        }

        if (message != null) {
            Link link = new Link(composite, getMessageLabelStyle());
            link.setText(message);
            GridDataFactory.fillDefaults().align(SWT.FILL, SWT.BEGINNING).grab(true, false)
                    .hint(convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH), SWT.DEFAULT).applyTo(link);

            link.addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    Program.launch(linkUrl);
                }
            });
        }
        return composite;
    }

    public static boolean openConfirm(Shell shell, String title, String messageWithLink, String linkUrl) {
        return new MessageDialogWithLink(shell, title, null, messageWithLink, linkUrl, MessageDialog.CONFIRM,
                new String[] { "OK", "Cancel" }, 0).open() == MessageDialog.OK;
    }

    public static void openError(Shell shell, String title, String messageWithLink, String linkUrl) {
        new MessageDialogWithLink(shell, title, null, messageWithLink, linkUrl, MessageDialog.ERROR, new String[] { "OK" }, 0)
                .open();
    }

}
