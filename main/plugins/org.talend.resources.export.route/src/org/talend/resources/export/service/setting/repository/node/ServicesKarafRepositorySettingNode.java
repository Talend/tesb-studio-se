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
package org.talend.resources.export.service.setting.repository.node;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.talend.repository.model.RepositoryNode;
import org.talend.resources.export.service.setting.repository.page.ServicesKarafRepositorySettingPage;
import org.talend.resources.export.setting.KarafRepositorySettingNode;

/**
 * DOC ggu class global comment. Detailled comment
 */
public class ServicesKarafRepositorySettingNode extends KarafRepositorySettingNode {

    public ServicesKarafRepositorySettingNode(String parentId, RepositoryNode node) {
        super(parentId, node);
    }

    public ServicesKarafRepositorySettingNode(String parentId, String baseLabel, ImageDescriptor imageDescriptor,
            RepositoryNode node) {
        super(parentId, baseLabel, imageDescriptor, node);
    }

    @Override
    protected PreferencePage createPreferencePage() {
        return new ServicesKarafRepositorySettingPage(this.getNode());
    }
}
