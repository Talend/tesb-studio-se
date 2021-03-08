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
package org.talend.resources.export.route.setting.repository.node;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.talend.repository.model.RepositoryNode;
import org.talend.resources.export.route.setting.repository.page.RoutesKarafRepositorySettingPage;
import org.talend.resources.export.setting.KarafRepositorySettingNode;

/**
 * DOC ggu class global comment. Detailled comment
 */
public class RoutesKarafRepositorySettingNode extends KarafRepositorySettingNode {

    public RoutesKarafRepositorySettingNode(String parentId, RepositoryNode node) {
        super(parentId, node);
    }

    public RoutesKarafRepositorySettingNode(String parentId, String baseLabel, ImageDescriptor imageDescriptor,
            RepositoryNode node) {
        super(parentId, baseLabel, imageDescriptor, node);
    }

    @Override
    protected PreferencePage createPreferencePage() {
        return new RoutesKarafRepositorySettingPage(this.getNode());
    }
}
