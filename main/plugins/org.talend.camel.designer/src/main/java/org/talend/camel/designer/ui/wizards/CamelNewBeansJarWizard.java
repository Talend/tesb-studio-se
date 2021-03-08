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
package org.talend.camel.designer.ui.wizards;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.Wizard;
import org.talend.camel.core.model.camelProperties.BeansJarItem;
import org.talend.camel.core.model.camelProperties.CamelPropertiesFactory;
import org.talend.camel.designer.CamelDesignerPlugin;
import org.talend.camel.designer.i18n.Messages;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.utils.VersionUtils;
import org.talend.commons.utils.workbench.resources.ResourceUtils;
import org.talend.core.CorePlugin;
import org.talend.core.context.Context;
import org.talend.core.context.RepositoryContext;
import org.talend.core.model.general.Project;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.designer.maven.tools.CodesJarM2CacheManager;
import org.talend.repository.ProjectManager;
import org.talend.repository.model.IProxyRepositoryFactory;

public class CamelNewBeansJarWizard extends Wizard {

    private CamelNewBeansJarWizardPage mainPage;

    private BeansJarItem beansJarItem;

    private Property property;

    private IPath path;

    public CamelNewBeansJarWizard(IPath path) {
        super();
        this.path = path;
        this.property = PropertiesFactory.eINSTANCE.createProperty();
        this.property
                .setAuthor(((RepositoryContext) CorePlugin.getContext().getProperty(Context.REPOSITORY_CONTEXT_KEY)).getUser());
        this.property.setVersion(VersionUtils.DEFAULT_VERSION);
        this.property.setStatusCode(""); //$NON-NLS-1$

        beansJarItem = CamelPropertiesFactory.eINSTANCE.createBeansJarItem();
        beansJarItem.setProperty(property);
        beansJarItem.setRoutinesJarType(PropertiesFactory.eINSTANCE.createRoutinesJarType());
    }

    @Override
    public void addPages() {
        mainPage = new CamelNewBeansJarWizardPage(property, path);
        addPage(mainPage);
        setWindowTitle(mainPage.getTitle());
        setDefaultPageImageDescriptor(CamelDesignerPlugin.getImageDescriptor(CamelDesignerPlugin.BEAN_WIZ_ICON));
    }

    @Override
    public boolean performFinish() {
        IProxyRepositoryFactory repositoryFactory = ProxyRepositoryFactory.getInstance();
        try {
            property.setId(repositoryFactory.getNextId());
            property.setLabel(property.getDisplayName());
            repositoryFactory.create(beansJarItem, mainPage.getDestinationPath());
            Project project = ProjectManager.getInstance().getCurrentProject();
            IFolder folder = ResourceUtils
                    .getFolder(ResourceUtils.getProject(project),
                            ERepositoryObjectType.getFolderName(ERepositoryObjectType.BEANSJAR), true)
                    .getFolder(property.getLabel());
            if (!folder.exists()) {
                ResourceUtils.createFolder(folder);
            }
            CodesJarM2CacheManager.updateCodesJarProject(property);
        } catch (Exception e) {
            MessageDialog.openError(getShell(), Messages.getString("NewBeanWizard.failureTitle"), ""); //$NON-NLS-1$ //$NON-NLS-2$
            ExceptionHandler.process(e);
        }

        return beansJarItem != null;
    }

    public BeansJarItem getBeansJar() {
        return beansJarItem;
    }
}
