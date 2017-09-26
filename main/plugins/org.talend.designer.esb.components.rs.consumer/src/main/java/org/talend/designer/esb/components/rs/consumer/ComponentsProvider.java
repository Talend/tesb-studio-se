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
package org.talend.designer.esb.components.rs.consumer;

import java.io.File;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.talend.core.model.components.AbstractComponentsProvider;

public class ComponentsProvider extends AbstractComponentsProvider {

    private File providedLocation;

    @Override
    protected File getExternalComponentsLocation() {
        if (null == providedLocation) {
            final Activator plugin = Activator.getDefault();
            URL url = FileLocator.find(plugin.getBundle(), new Path("components"), null); //$NON-NLS-1$
            try {
                url = FileLocator.toFileURL(url);
                providedLocation = new File(url.getPath());
            } catch (Exception e) {
                plugin.getLog().log(Activator.getStatus(null, e));
            }
        }
        return providedLocation;
    }

}
