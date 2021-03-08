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
package org.talend.resources.export.setting;

import org.eclipse.jface.resource.ImageDescriptor;
import org.talend.designer.maven.ui.setting.repository.node.RepositoryPreferenceNode;
import org.talend.designer.maven.ui.utils.DesignerMavenUiHelper;
import org.talend.repository.model.RepositoryNode;
import org.talend.resources.export.i18n.Messages;

/**
 * DOC ggu class global comment. Detailled comment
 */
public abstract class KarafRepositorySettingNode extends RepositoryPreferenceNode {

    public KarafRepositorySettingNode(String parentId, RepositoryNode node) {
        this(parentId, null, null, node);
    }

    @SuppressWarnings("nls")
    public KarafRepositorySettingNode(String parentId, String baseLabel, ImageDescriptor imageDescriptor, RepositoryNode node) {
        super(DesignerMavenUiHelper.buildRepositoryPreferenceNodeId(parentId, "Karaf"), buildKarafLabel(baseLabel),
                imageDescriptor, node);
    }

    @SuppressWarnings("nls")
    private static String buildKarafLabel(String baseLabel) {
        String label = Messages.getString("RepositorySettingNode_Label");
        if (baseLabel != null && baseLabel.length() > 0) {
            label = baseLabel + " - (" + label + ')';
        }
        return label;
    }

}
