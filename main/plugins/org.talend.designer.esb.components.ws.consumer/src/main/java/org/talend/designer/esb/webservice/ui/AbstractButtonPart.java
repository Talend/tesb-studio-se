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

import java.net.URL;
import java.util.EventListener;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.osgi.framework.Bundle;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.ui.runtime.image.IImage;
import org.talend.commons.ui.runtime.image.ImageProvider;

public abstract class AbstractButtonPart<T extends EventListener> extends AbstractWebServiceUIPart<T> {

    public AbstractButtonPart(T eventListener) {
        super(eventListener);
    }

    protected Button button;

    @Override
    public final Control createControl(Composite parent) {
        button = new Button(parent, SWT.PUSH | SWT.CENTER);
        String messageKey = getMessageKey();
        if (messageKey != null) {
            button.setText(Messages.getString(messageKey));
        }
        Image image = getImage();
        if (image != null) {
            button.setImage(image);
        }
        button.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                buttonSelected(e);
            }
        });
        return button;
    }

    protected abstract void buttonSelected(SelectionEvent e);

    protected abstract String getMessageKey();

    /**
     * Gets the image. Can use {@link #getImageFromBundle(String, String)} or {@link #getImageFromIImage(IImage)}.
     * 
     * @return the image
     */
    protected abstract Image getImage();

    /**
     * Gets the image from bundle. Will return null without any Exception when failed.
     * 
     * @param bundle the bundle
     * @param path the path
     * @return the image from bundle
     */
    protected static Image getImageFromBundle(String bundleId, String path) {
        try {
            Bundle bundle = Platform.getBundle(bundleId);
            URL resource = bundle.getResource(path);
            return ImageDescriptor.createFromURL(resource).createImage();
        } catch (Exception e) {
            // ignore when get icon failed.
            ExceptionHandler.process(e);
        }
        return null;
    }

    protected static Image getImageFromIImage(IImage iImage) {
        return ImageProvider.getImage(iImage);
    }

    protected Shell getShell() {
        return button.getShell();
    }
}
