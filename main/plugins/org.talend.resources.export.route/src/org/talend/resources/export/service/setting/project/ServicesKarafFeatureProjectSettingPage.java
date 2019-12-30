// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.resources.export.service.setting.project;

import org.talend.core.runtime.projectsetting.IProjectSettingPreferenceConstants;
import org.talend.resources.export.i18n.Messages;
import org.talend.resources.export.projectsetting.AbstractKarafMavenScriptProjectSettingPage;

/**
 * DOC ggu class global comment. Detailled comment
 */
public class ServicesKarafFeatureProjectSettingPage extends AbstractKarafMavenScriptProjectSettingPage {

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.maven.ui.projectsetting.AbstractMavenScriptProjectSettingPage#getPreferenceKey()
     */
    @Override
    protected String getPreferenceKey() {
        return IProjectSettingPreferenceConstants.TEMPLATE_SERVICES_KARAF_FEATURE;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.maven.ui.projectsetting.AbstractMavenScriptProjectSettingPage#getHeadTitle()
     */
    @Override
    protected String getHeadTitle() {
        return Messages.getString("ServicesKarafFeatureProjectSettingPage_Title"); //$NON-NLS-1$
    }

}
