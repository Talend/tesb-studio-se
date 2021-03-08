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
package org.talend.resources.export.service.setting.repository.page;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFolder;
import org.eclipse.jface.preference.IPreferenceNode;
import org.talend.core.runtime.projectsetting.IProjectSettingPreferenceConstants;
import org.talend.core.runtime.projectsetting.IProjectSettingTemplateConstants;
import org.talend.designer.maven.model.TalendMavenConstants;
import org.talend.repository.model.RepositoryNode;
import org.talend.resources.export.projectsetting.AbstractKarafRepositorySettingPage;
import org.talend.resources.export.service.setting.repository.ServiceKarafRepositoryMavenSetting;

/**
 * DOC ggu class global comment. Detailled comment
 */
public class ServicesKarafRepositorySettingPage extends AbstractKarafRepositorySettingPage {

    protected static final String ID_MAVEN_SERVICES_KARAF_PROJECT_SETTING = "projectsetting.ServicesKaraf"; //$NON-NLS-1$

    public ServicesKarafRepositorySettingPage(RepositoryNode node) {
        super(node);
    }

    @Override
    protected String getDefaultProjectSettingId() {
        return ID_MAVEN_SERVICES_KARAF_PROJECT_SETTING;
    }

    @Override
    protected Map<String, String> getProjectSettingKeyWithFileNamesMap() {
        Map<String, String> projectSettingKeyWithFileNamesMap = new HashMap<String, String>(
                super.getProjectSettingKeyWithFileNamesMap());

        projectSettingKeyWithFileNamesMap.put(IProjectSettingPreferenceConstants.TEMPLATE_SERVICES_KARAF_POM,
                TalendMavenConstants.POM_FILE_NAME);
        projectSettingKeyWithFileNamesMap.put(IProjectSettingPreferenceConstants.TEMPLATE_SERVICES_KARAF_PARENT,
                IProjectSettingTemplateConstants.MAVEN_KARAF_BUILD_PARENT_FILE_NAME);
        projectSettingKeyWithFileNamesMap.put(IProjectSettingPreferenceConstants.TEMPLATE_SERVICES_KARAF_BUNDLE,
                IProjectSettingTemplateConstants.MAVEN_CONTROL_BUILD_BUNDLE_FILE_NAME);
        projectSettingKeyWithFileNamesMap.put(IProjectSettingPreferenceConstants.TEMPLATE_SERVICES_KARAF_FEATURE,
                IProjectSettingTemplateConstants.MAVEN_KARAF_BUILD_FEATURE_FILE_NAME);

        return projectSettingKeyWithFileNamesMap;
    }

    @Override
    protected List<IPreferenceNode> createMavenChildrenNodes(IFolder nodeFolder) {
        List<IPreferenceNode> routesKarafChildrenNodes = ServiceKarafRepositoryMavenSetting.createServicesKarafChildrenNodes(
                nodeFolder, getNode(), getPrefNodeId(), false);
        return routesKarafChildrenNodes;
    }

}
