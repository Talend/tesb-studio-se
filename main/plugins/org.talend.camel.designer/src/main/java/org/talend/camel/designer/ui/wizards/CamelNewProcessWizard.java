// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
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
import org.talend.camel.core.model.camelProperties.CamelProcessItem;
import org.talend.camel.core.model.camelProperties.CamelPropertiesFactory;
import org.talend.camel.designer.i18n.Messages;
import org.talend.camel.designer.util.CamelSpringUtil;
import org.talend.camel.model.CamelRepositoryNodeType;
import org.talend.commons.ui.runtime.image.ECoreImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.designer.core.ui.wizards.NewProcessWizard;
import org.talend.designer.core.ui.wizards.NewProcessWizardPage;

public class CamelNewProcessWizard extends NewProcessWizard {

    public CamelNewProcessWizard(IPath path) {
        super(path);

        setDefaultPageImageDescriptor(ImageProvider.getImageDesc(ECoreImage.ROUTES_ICON));
    }

    @Override
    protected ProcessItem createNewProcessItem() {
        return CamelPropertiesFactory.eINSTANCE.createCamelProcessItem();
    }

    @Override
    protected NewProcessWizardPage createWizardPage(Property property, IPath destinationPath) {
        final NewProcessWizardPage page = new NewProcessWizardPage(property, destinationPath) {
            @Override
            public ERepositoryObjectType getRepositoryObjectType() {
                return CamelRepositoryNodeType.repositoryRoutesType;
            }
        };
        page.setTitle(Messages.getString("NewProcessWizard.title")); //$NON-NLS-1$
        page.setDescription(Messages.getString("NewProcessWizard.description")); //$NON-NLS-1$
        return page;
    }

    @Override
    protected void createProcessItem() {
        //ADDED for TESB-7887 By GangLiu
        getProcess().setSpringContent(CamelSpringUtil.getDefaultContent(getProcess()));
        super.createProcessItem();
    }

    @Override
    public CamelProcessItem getProcess() {
        return (CamelProcessItem) super.getProcess();
    }
}
