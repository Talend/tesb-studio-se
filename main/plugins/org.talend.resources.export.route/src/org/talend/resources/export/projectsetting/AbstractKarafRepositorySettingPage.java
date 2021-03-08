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

import org.talend.core.runtime.projectsetting.ProjectPreferenceManager;
import org.talend.designer.maven.ui.setting.repository.page.FolderMavenSettingPage;
import org.talend.repository.model.RepositoryNode;
import org.talend.resources.export.ExportRouteResourcesPlugin;
import org.talend.resources.export.i18n.Messages;

/**
 * DOC ggu class global comment. Detailled comment
 */
public abstract class AbstractKarafRepositorySettingPage extends FolderMavenSettingPage {

    public AbstractKarafRepositorySettingPage(RepositoryNode node) {
        super(node);
    }

    protected String createMessages(boolean created) {
        StringBuffer messages = new StringBuffer(200);
        // existed
        if (created) {
            messages.append(Messages.getString("RepositorySettingPage_ExistedMavenSettingMessage",//$NON-NLS-1$
                    buildLinks(getProcessFileNames())));
        } else {
            messages.append(Messages.getString("RepositorySettingPage_CreatingMavenSettingMessage", //$NON-NLS-1$
                    getProcessFileNames()));
            messages.append('\n');
            messages.append('\n');

            messages.append(Messages.getString("RepositorySettingPage_CreatingMavenSettingNote"));//$NON-NLS-1$
            messages.append(' ');
            String mvnProjectSettingLinkStr = "<a href=\"" + getDefaultProjectSettingId() + "\">Default</a>";//$NON-NLS-1$ //$NON-NLS-2$
            messages.append(Messages.getString("RepositorySettingPage_CreatingMavenSettingNoteMessage", //$NON-NLS-1$
                    mvnProjectSettingLinkStr));
        }
        messages.append('\n');

        return messages.toString();
    }

    @Override
    protected ProjectPreferenceManager getProjectSettingManager() {
        return ExportRouteResourcesPlugin.getDefault().getProjectPreferenceManager();
    }

}
