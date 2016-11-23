// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.repository.services.ui;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.wizard.Wizard;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.model.RepositoryNodeUtilities;
import org.talend.repository.services.model.services.ServiceItem;

public class OpenWSDLWizard extends Wizard {

    private OpenWSDLPage wsdlPage;

    private RepositoryNode repositoryNode;

    public OpenWSDLWizard(RepositoryNode repositoryNode) {
        this.repositoryNode = repositoryNode;
        setWindowTitle("Edit WSDL");
        setNeedsProgressMonitor(true);
    }

    @Override
    public void addPages() {
        super.addPages();

        IPath pathToSave;
        switch (repositoryNode.getType()) {
        case SIMPLE_FOLDER:
        case REPOSITORY_ELEMENT:
            pathToSave = RepositoryNodeUtilities.getPath(repositoryNode);
            break;
        default:
            pathToSave = new Path(""); //$NON-NLS-1$
            break;
        }

        wsdlPage = new OpenWSDLPage(repositoryNode, pathToSave, (ServiceItem) repositoryNode.getObject().getProperty().getItem(),
                false);
        addPage(wsdlPage);
    }

    @Override
    public boolean performFinish() {
        return wsdlPage.finish();
    }

}
