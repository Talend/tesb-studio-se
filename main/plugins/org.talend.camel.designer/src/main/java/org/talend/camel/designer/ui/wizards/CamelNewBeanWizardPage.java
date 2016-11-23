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
package org.talend.camel.designer.ui.wizards;

import org.eclipse.core.runtime.IPath;
import org.talend.camel.designer.i18n.Messages;
import org.talend.camel.model.CamelRepositoryNodeType;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.repository.ui.wizards.routines.NewRoutineWizardPage;

/**
 * Page for new project details.
 */
public class CamelNewBeanWizardPage extends NewRoutineWizardPage {

    public CamelNewBeanWizardPage(Property property, IPath destinationPath) {
        super(property, destinationPath);

        setTitle(Messages.getString("NewBeanWizardPage.title")); //$NON-NLS-1$
        setDescription(Messages.getString("NewBeanWizard.description")); //$NON-NLS-1$
    }

    @Override
    public ERepositoryObjectType getRepositoryObjectType() {
        return CamelRepositoryNodeType.repositoryBeansType;
    }

}
