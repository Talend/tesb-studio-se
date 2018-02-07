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
package org.talend.designer.esb.components.ws.trestrequest;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
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

    private boolean askForEndpoint = false;

    private IPreferenceStore prefs;

    private String messageWithLink;

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
            String messageWithLink, String linkUrl, int dialogImageType, String[] dialogButtonLabels, int defaultIndex,
            boolean askForEndpoint) {
        super(parentShell, dialogTitle, dialogTitleImage, dialogMessage, dialogImageType, dialogButtonLabels, defaultIndex);

        this.message = dialogMessage;
        this.messageWithLink = messageWithLink;
        this.linkUrl = linkUrl;
        this.askForEndpoint = askForEndpoint;

        this.prefs = TRESTRequestPlugin.getDefault().getPreferenceStore();
    }

    @Override
    protected Control createMessageArea(Composite composite) {

        Composite cpoMain = new Composite(composite, SWT.NONE);

        GridLayout gdlCpoMain = new GridLayout(2, false);
        cpoMain.setLayout(gdlCpoMain);

        Image image = getImage();

        if (image != null) {
            imageLabel = new Label(cpoMain, SWT.NULL);
            image.setBackground(imageLabel.getBackground());
            imageLabel.setImage(image);
            GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.BEGINNING).applyTo(imageLabel);
        }

        Composite cpoRight = new Composite(cpoMain, SWT.NONE);
        GridLayout gdlCpoRight = new GridLayout(1, false);
        gdlCpoRight.verticalSpacing = 20;
        cpoRight.setLayout(gdlCpoRight);
        GridDataFactory.fillDefaults().grab(true, false).applyTo(cpoRight);

        if (StringUtils.isNotBlank(message)) {

            Label labelMessage = new Label(cpoRight, getMessageLabelStyle());
            labelMessage.setText(message);
            GridDataFactory.fillDefaults().align(SWT.FILL, SWT.BEGINNING).grab(true, false)
                    .hint(convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH), SWT.DEFAULT)
                    .applyTo(labelMessage);
        }

        if (askForEndpoint) {

            boolean keepEndpointValue = prefs.getBoolean(TRESTRequestConstants.PREF_KEEP_ENDPOINT);

            final Button chkKeepEndpoint = new Button(cpoRight, SWT.CHECK);
            chkKeepEndpoint.setText("Keep existing endpoint configuration (do not override)");
            chkKeepEndpoint.setSelection(keepEndpointValue);

            GridDataFactory.fillDefaults().align(SWT.FILL, SWT.BEGINNING).grab(true, false)
                    .hint(convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH), SWT.DEFAULT)
                    .applyTo(chkKeepEndpoint);

            chkKeepEndpoint.addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    prefs.putValue(TRESTRequestConstants.PREF_KEEP_ENDPOINT, String.valueOf(chkKeepEndpoint.getSelection()));

                }
            });

        }

        if (StringUtils.isNotBlank(messageWithLink)) {

            Link link = new Link(cpoRight, getMessageLabelStyle());
            link.setText(messageWithLink);
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

    public static boolean openConfirm(Shell shell, String title, String dialogMessage, String messageWithLink, String linkUrl,
            boolean askForEndpoint) {
        return new MessageDialogWithLink(shell, title, null, dialogMessage, messageWithLink, linkUrl, MessageDialog.CONFIRM,
                new String[] { "OK", "Cancel" }, 0, askForEndpoint).open() == MessageDialog.OK;
    }

    public static void openError(Shell shell, String title, String dialogMessage, String messageWithLink, String linkUrl,
            boolean askForEndpoint) {
        new MessageDialogWithLink(shell, title, null, dialogMessage, messageWithLink, linkUrl, MessageDialog.ERROR,
                new String[] { "OK" }, 0, askForEndpoint).open();
    }

}
