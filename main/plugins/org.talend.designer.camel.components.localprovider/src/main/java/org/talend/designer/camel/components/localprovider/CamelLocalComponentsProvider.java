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
package org.talend.designer.camel.components.localprovider;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.core.model.components.AbstractComponentsProvider;

public class CamelLocalComponentsProvider extends AbstractComponentsProvider {

    @Override
    protected File getExternalComponentsLocation() {
        URL url = FileLocator.find(CamelComponentPlugin.getDefault().getBundle(), new Path("components"), null); //$NON-NLS-1$
        try {
            url = FileLocator.toFileURL(url);
            return new File(url.getPath());
        } catch (IOException e) {
            ExceptionHandler.process(e);
        }
        return null;
    }

}
