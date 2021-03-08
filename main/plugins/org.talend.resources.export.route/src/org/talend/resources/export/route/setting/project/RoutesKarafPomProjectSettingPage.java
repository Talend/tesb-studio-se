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
package org.talend.resources.export.route.setting.project;

import org.talend.core.runtime.projectsetting.IProjectSettingPreferenceConstants;
import org.talend.resources.export.i18n.Messages;
import org.talend.resources.export.projectsetting.AbstractKarafMavenScriptProjectSettingPage;

/**
 * DOC ggu class global comment. Detailled comment
 */
public class RoutesKarafPomProjectSettingPage extends AbstractKarafMavenScriptProjectSettingPage {

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.maven.ui.projectsetting.AbstractMavenScriptProjectSettingPage#getPreferenceKey()
     */
    @Override
    protected String getPreferenceKey() {
        return IProjectSettingPreferenceConstants.TEMPLATE_ROUTES_KARAF_POM;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.maven.ui.projectsetting.AbstractMavenScriptProjectSettingPage#getHeadTitle()
     */
    @Override
    protected String getHeadTitle() {
        return Messages.getString("RoutesKarafPomProjectSettingPage_Title"); //$NON-NLS-1$
    }

}
