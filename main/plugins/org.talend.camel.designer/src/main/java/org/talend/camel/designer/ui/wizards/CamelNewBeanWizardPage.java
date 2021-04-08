// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
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
 * Page for new project details. <br/>
 *
 * $Id: NewProcessWizardPage.java 52559 2010-12-13 04:14:06Z nrousseau $
 *
 */
public class CamelNewBeanWizardPage extends NewRoutineWizardPage {

    public CamelNewBeanWizardPage(Property property, IPath destinationPath) {
        super(property, destinationPath); //$NON-NLS-1$

        setTitle(Messages.getString("NewBeanWizardPage.title")); //$NON-NLS-1$
        setDescription(Messages.getString("NewBeanWizard.description")); //$NON-NLS-1$
    }

    public ERepositoryObjectType getRepositoryObjectType() {
        return CamelRepositoryNodeType.repositoryBeansType;
    }

    @Override
    protected Property getJarPropery() {
        return ((CamelNewInnerBeanWizard) getWizard()).getJarProperty();
    }

}
