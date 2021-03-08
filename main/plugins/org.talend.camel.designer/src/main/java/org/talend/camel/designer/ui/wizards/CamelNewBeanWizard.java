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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.Wizard;
import org.talend.camel.core.model.camelProperties.BeanItem;
import org.talend.camel.core.model.camelProperties.CamelPropertiesFactory;
import org.talend.camel.designer.CamelDesignerPlugin;
import org.talend.camel.designer.i18n.Messages;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.RuntimeExceptionHandler;
import org.talend.commons.utils.VersionUtils;
import org.talend.core.CorePlugin;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.context.Context;
import org.talend.core.context.RepositoryContext;
import org.talend.core.model.general.ILibrariesService;
import org.talend.core.model.general.ModuleNeeded;
import org.talend.core.model.properties.ByteArray;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.core.model.properties.RoutineItem;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.designer.core.model.utils.emf.component.ComponentFactory;
import org.talend.designer.core.model.utils.emf.component.IMPORTType;
import org.talend.designer.maven.tools.MavenPomSynchronizer;
import org.talend.designer.runprocess.IRunProcessService;
import org.talend.librariesmanager.model.ModulesNeededProvider;
import org.talend.repository.model.IProxyRepositoryFactory;

/**
 * Wizard for the creation of a new project. <br/>
 *
 * $Id: NewProcessWizard.java 52559 2010-12-13 04:14:06Z nrousseau $
 *
 */
public class CamelNewBeanWizard extends Wizard {

    /** Main page. */
    private CamelNewBeanWizardPage mainPage;

    /** Created project. */
    protected BeanItem beanItem;

    private Property property;

    private IPath path;

    /**
     * Constructs a new NewProjectWizard.
     *
     * @param author Project author.
     * @param server
     * @param password
     */
    public CamelNewBeanWizard(IPath path) {
        super();
        this.path = path;

        property = PropertiesFactory.eINSTANCE.createProperty();
        property.setAuthor(((RepositoryContext) CorePlugin.getContext().getProperty(Context.REPOSITORY_CONTEXT_KEY)).getUser());
        property.setVersion(VersionUtils.DEFAULT_VERSION);
        property.setStatusCode(""); //$NON-NLS-1$
        addAdditionalProperties(property);

        beanItem = CamelPropertiesFactory.eINSTANCE.createBeanItem();

        beanItem.setProperty(property);

        ILibrariesService service = CorePlugin.getDefault().getLibrariesService();
        URL url = service.getBeanTemplate();
        ByteArray byteArray = PropertiesFactory.eINSTANCE.createByteArray();
        InputStream stream = null;
        try {
            stream = url.openStream();
            byte[] innerContent = new byte[stream.available()];
            stream.read(innerContent);
            stream.close();
            byteArray.setInnerContent(innerContent);
        } catch (IOException e) {
            RuntimeExceptionHandler.process(e);
        }

        beanItem.setContent(byteArray);

        addDefaultModulesForBeans();
    }

    protected void addAdditionalProperties(Property property) {
    }

    protected void createProperty(Property property) {
        property = PropertiesFactory.eINSTANCE.createProperty();
        property.setAuthor(((RepositoryContext) CorePlugin.getContext().getProperty(Context.REPOSITORY_CONTEXT_KEY)).getUser());
        property.setVersion(VersionUtils.DEFAULT_VERSION);
        property.setStatusCode(""); //$NON-NLS-1$
    }

    private void refreshLibrariesListForTheBean() {
	    GlobalServiceRegister globalServiceRegister = GlobalServiceRegister.getDefault();
	    
        if (globalServiceRegister.isServiceRegistered(IRunProcessService.class)) {
	        IRunProcessService runProcessService = globalServiceRegister.getService(IRunProcessService.class);
	
	        MavenPomSynchronizer.addChangeLibrariesListener();
	        
	        runProcessService.updateLibraries((RoutineItem) beanItem.getProperty().getItem());
    	}
    }

	private void addDefaultModulesForBeans() {
        List<ModuleNeeded> importNeedsList = ModulesNeededProvider.getModulesNeededForBeans();

        for (ModuleNeeded model : importNeedsList) {
            IMPORTType importType = ComponentFactory.eINSTANCE.createIMPORTType();
            importType.setMODULE(model.getModuleName());
            importType.setMESSAGE(model.getInformationMsg());
            importType.setREQUIRED(model.isRequired());
            importType.setMVN(model.getMavenUri());
            beanItem.getImports().add(importType);
        }
    }

    /**
     * @see org.eclipse.jface.wizard.Wizard#addPages()
     */
    @Override
    public void addPages() {
        mainPage = new CamelNewBeanWizardPage(property, path);
        addPage(mainPage);
        setWindowTitle(mainPage.getTitle());
        setDefaultPageImageDescriptor(CamelDesignerPlugin.getImageDescriptor(CamelDesignerPlugin.BEAN_WIZ_ICON));
    }

    /**
     * @see org.eclipse.jface.wizard.Wizard#performFinish()
     */
    @Override
    public boolean performFinish() {
        IProxyRepositoryFactory repositoryFactory = ProxyRepositoryFactory.getInstance();
        try {
            property.setId(repositoryFactory.getNextId());

            // http://jira.talendforge.org/browse/TESB-5000 LiXiaopeng
            property.setLabel(property.getDisplayName());
            // repositoryFactory.create(routineItem, mainPage.getDestinationPath());
            repositoryFactory.create(beanItem, mainPage.getDestinationPath());
        } catch (PersistenceException e) {
            MessageDialog.openError(getShell(), Messages.getString("NewBeanWizard.failureTitle"), ""); //$NON-NLS-1$ //$NON-NLS-2$
            ExceptionHandler.process(e);
        }
        
        if (beanItem != null) {
        	refreshLibrariesListForTheBean();
        	return true;
        }

        return false;
    }

    /**
     * Getter for project.
     *
     * @return the project
     */
    public BeanItem getBean() {
        return this.beanItem;
    }
}
