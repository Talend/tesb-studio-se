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
import org.talend.core.runtime.projectsetting.IProjectSettingPreferenceConstants;
import org.talend.core.runtime.projectsetting.IProjectSettingTemplateConstants;
import org.talend.designer.maven.model.TalendMavenConstants;
import org.talend.designer.maven.ui.setting.project.initializer.AbstractProjectPreferenceInitializer;
import org.talend.resources.export.ExportRouteResourcesPlugin;

/**
 * DOC ggu class global comment. Detailled comment
 */
public class ExportRouteServiceMavenScriptsPreferenceInitializer extends AbstractProjectPreferenceInitializer {

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.designer.maven.ui.projectsetting.initializer.AbstractProjectPreferenceInitializer#getPreferenceStore()
     */
    @Override
    protected IPreferenceStore getPreferenceStore() {
        return ExportRouteResourcesPlugin.getDefault().getProjectPreferenceManager().getPreferenceStore();
    }

    @Override
    protected void initializeFields(IPreferenceStore preferenceStore) {
        super.initializeFields(preferenceStore);

        // routes
        setDefault(preferenceStore, IProjectSettingPreferenceConstants.TEMPLATE_ROUTES_KARAF_POM,
                IProjectSettingTemplateConstants.PATH_ROUTE + '/' + TalendMavenConstants.POM_FILE_NAME);
        setDefault(preferenceStore, IProjectSettingPreferenceConstants.TEMPLATE_ROUTES_KARAF_PARENT,
                IProjectSettingTemplateConstants.PATH_ROUTE + '/'
                        + IProjectSettingTemplateConstants.MAVEN_KARAF_BUILD_PARENT_FILE_NAME);
        setDefault(preferenceStore, IProjectSettingPreferenceConstants.TEMPLATE_ROUTES_KARAF_BUNDLE,
                IProjectSettingTemplateConstants.PATH_ROUTE + '/'
                        + IProjectSettingTemplateConstants.MAVEN_KARAF_BUILD_BUNDLE_FILE_NAME);
        setDefault(preferenceStore, IProjectSettingPreferenceConstants.TEMPLATE_ROUTES_KARAF_FEATURE,
                IProjectSettingTemplateConstants.PATH_ROUTE + '/'
                        + IProjectSettingTemplateConstants.MAVEN_KARAF_BUILD_FEATURE_FILE_NAME);

        // services
        setDefault(preferenceStore, IProjectSettingPreferenceConstants.TEMPLATE_SERVICES_KARAF_POM,
                IProjectSettingTemplateConstants.PATH_SERVICES + '/' + TalendMavenConstants.POM_FILE_NAME);
        setDefault(preferenceStore, IProjectSettingPreferenceConstants.TEMPLATE_SERVICES_KARAF_PARENT,
                IProjectSettingTemplateConstants.PATH_SERVICES + '/'
                        + IProjectSettingTemplateConstants.MAVEN_KARAF_BUILD_PARENT_FILE_NAME);
        setDefault(preferenceStore, IProjectSettingPreferenceConstants.TEMPLATE_SERVICES_KARAF_BUNDLE,
                IProjectSettingTemplateConstants.PATH_SERVICES + '/'
                        + IProjectSettingTemplateConstants.MAVEN_CONTROL_BUILD_BUNDLE_FILE_NAME);
        setDefault(preferenceStore, IProjectSettingPreferenceConstants.TEMPLATE_SERVICES_KARAF_FEATURE,
                IProjectSettingTemplateConstants.PATH_SERVICES + '/'
                        + IProjectSettingTemplateConstants.MAVEN_KARAF_BUILD_FEATURE_FILE_NAME);

    }

}
