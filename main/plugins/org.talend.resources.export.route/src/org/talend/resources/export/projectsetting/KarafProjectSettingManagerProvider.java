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
package org.talend.resources.export.projectsetting;

import org.talend.core.runtime.projectsetting.ProjectPreferenceManager;
import org.talend.designer.maven.setting.project.IProjectSettingManagerProvider;
import org.talend.resources.export.ExportRouteResourcesPlugin;

/**
 * DOC ggu class global comment. Detailled comment
 */
public class KarafProjectSettingManagerProvider implements IProjectSettingManagerProvider {

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.maven.setting.project.IProjectSettingManagerProvider#getProjectSettingManager()
     */
    @Override
    public ProjectPreferenceManager getProjectSettingManager() {
        return ExportRouteResourcesPlugin.getDefault().getProjectPreferenceManager();
    }

}
