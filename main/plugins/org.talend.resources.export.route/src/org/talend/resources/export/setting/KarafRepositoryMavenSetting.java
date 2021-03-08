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

import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.viewers.ILabelProvider;
import org.talend.designer.maven.ui.setting.repository.RepositoryMavenSetting;
import org.talend.designer.maven.ui.setting.repository.node.RepositoryPreferenceNode;
import org.talend.designer.maven.ui.utils.DesignerMavenUiHelper;
import org.talend.repository.model.RepositoryNode;

/**
 * DOC ggu class global comment. Detailled comment
 */
public abstract class KarafRepositoryMavenSetting extends RepositoryMavenSetting {

    protected static final boolean STANDALONE_CATEGORY = false;

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.maven.ui.setting.repository.RepositoryMavenSetting#createFolderNode(java.lang.String,
     * org.eclipse.jface.viewers.ILabelProvider, org.talend.repository.model.RepositoryNode)
     */
    @Override
    protected IPreferenceNode createFolderNode(String id, ILabelProvider labelProvider, RepositoryNode node) {
        if (STANDALONE_CATEGORY) {
            return super.createFolderNode(id, labelProvider, node);
        } else {
            return createKarafFolderNode(id, labelProvider, node);
        }
    }

    protected abstract RepositoryPreferenceNode createKarafFolderNode(String id, ILabelProvider labelProvider, RepositoryNode node);

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.designer.maven.ui.setting.repository.RepositoryMavenSetting#createMavenScriptsChildren(org.eclipse
     * .jface.preference.IPreferenceNode, org.talend.repository.model.RepositoryNode)
     */
    @Override
    public void createMavenScriptsChildren(IPreferenceNode parentNode, RepositoryNode node) {
        if (STANDALONE_CATEGORY) {
            RepositoryPreferenceNode karafNode = createKarafFolderNode(parentNode.getId(), null, node);
            parentNode.add(karafNode);

            parentNode = karafNode;
        }

        IFolder nodeFolder = DesignerMavenUiHelper.getNodeFolder(node);
        List<IPreferenceNode> routesKarafChildrenNodes = createKarafChildrenNodes(nodeFolder, node, parentNode.getId(), true);
        for (IPreferenceNode n : routesKarafChildrenNodes) {
            parentNode.add(n);
        }
    }

    protected abstract List<IPreferenceNode> createKarafChildrenNodes(IFolder nodeFolder, RepositoryNode node, String parentId,
            boolean checkExist);

}
