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
package org.talend.resources.export.route.setting.repository.page;

import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.swt.widgets.Composite;
import org.talend.designer.maven.ui.setting.repository.RepositoryMavenSettingStore;
import org.talend.resources.export.route.setting.project.RoutesKarafBundleProjectSettingPage;

/**
 * DOC ggu class global comment. Detailled comment
 */
public class RoutesKarafBundleRepositorySettingPage extends RoutesKarafBundleProjectSettingPage {

    private IFile scriptFile;

    public RoutesKarafBundleRepositorySettingPage(IFile scriptFile) {
        super();
        this.scriptFile = scriptFile;
    }

    public void createControl(Composite parent) {
        noDefaultAndApplyButton();
        this.setTitle(this.getHeadTitle());
        super.createControl(parent);
    }

    @Override
    protected IPreferenceStore doGetPreferenceStore() {
        return new RepositoryMavenSettingStore(this.getPreferenceKey(), this.scriptFile);
    }

    @Override
    public void load() throws IOException {
        if (getPreferenceStore() instanceof PreferenceStore) {
            ((PreferenceStore) getPreferenceStore()).load();
        }
        super.load();
    }

    @Override
    public void save() throws IOException {
        if (getPreferenceStore() instanceof PreferenceStore) {
            ((PreferenceStore) getPreferenceStore()).save();
        }
        super.save();
    }

}
