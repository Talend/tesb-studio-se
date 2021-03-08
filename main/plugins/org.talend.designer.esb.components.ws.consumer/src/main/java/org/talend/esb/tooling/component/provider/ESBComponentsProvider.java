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
package org.talend.esb.tooling.component.provider;

import java.io.File;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.talend.core.model.components.AbstractComponentsProvider;
import org.talend.designer.esb.webservice.WebServiceComponentPlugin;

public class ESBComponentsProvider extends AbstractComponentsProvider {

    private File providedLocation = null;

    @Override
    protected File getExternalComponentsLocation() {
        if (null == providedLocation) {
            WebServiceComponentPlugin plugin = WebServiceComponentPlugin.getDefault();
            try {
                URL url = FileLocator.find(plugin.getBundle(), new Path("components"), null); //$NON-NLS-1$
                url = FileLocator.toFileURL(url);
                providedLocation = new File(url.getPath());
            } catch (Exception e) {
                plugin.getLog().log(WebServiceComponentPlugin.getStatus(null, e));
            }
        }
        return providedLocation;
    }

}
