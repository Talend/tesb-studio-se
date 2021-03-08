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

import org.eclipse.jface.preference.IPreferenceStore;
import org.talend.designer.maven.ui.setting.project.page.AbstractPersistentProjectSettingPage;
import org.talend.resources.export.ExportRouteResourcesPlugin;

/**
 * DOC ggu class global comment. Detailled comment
 */
public abstract class AbstractKarafMavenScriptProjectSettingPage extends AbstractPersistentProjectSettingPage {

    @Override
    protected IPreferenceStore doGetPreferenceStore() {
        return ExportRouteResourcesPlugin.getDefault().getProjectPreferenceManager().getPreferenceStore();
    }

}
