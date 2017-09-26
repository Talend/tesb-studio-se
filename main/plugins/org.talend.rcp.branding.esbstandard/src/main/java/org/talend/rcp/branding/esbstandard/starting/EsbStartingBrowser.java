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
package org.talend.rcp.branding.esbstandard.starting;

import java.io.IOException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Composite;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.ui.html.BrowserDynamicPartLocationListener;
import org.talend.rcp.intro.starting.StartingBrowser;

/**
 * DOC wchen class global comment. Detailled comment
 */
public class EsbStartingBrowser extends StartingBrowser {

    public static final String ID = "org.talend.rcp.branding.esbstandard.starting.EsbStartingBrowser";

    public EsbStartingBrowser() {
    }

    @Override
    public void createPartControl(Composite parent) {
        try {
            browser = new Browser(parent, SWT.NONE);
            browser.setText(EsbStartingHelper.getHelper().getHtmlContent());
            browser.addLocationListener(new BrowserDynamicPartLocationListener());
        } catch (IOException e) {
            ExceptionHandler.process(e);
        } catch (Throwable t) {

            Exception ex = new Exception("The internal web browser can not be access,the starting page won't be displayed");
            ExceptionHandler.process(ex);
        }
    }

}
