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
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.Wizard;
import org.talend.camel.core.model.camelProperties.CamelProcessItem;
import org.talend.camel.core.model.camelProperties.CamelPropertiesFactory;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.ui.runtime.image.ECoreImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.core.CorePlugin;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.core.ui.editor.JobEditorInput;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;
import org.talend.designer.core.ui.wizards.NewProcessWizardPage;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.IRepositoryService;
import org.talend.repository.model.RepositoryNode;

/**
 * Wizard for the creation of a new project. <br/>
 * 
 * $Id: NewProcessWizard.java 46332 2010-08-05 06:48:56Z cli $
 * 
 */
public class SaveAsRoutesWizard extends Wizard {

    /** Main page. */
    private NewProcessWizardPage mainPage;

    /** Created project. */
    private CamelProcessItem camelProcessItem;

    private Property property;

    private IPath path;

    private IProxyRepositoryFactory repositoryFactory;

    private JobEditorInput jobEditorInput;

    private CamelProcessItem oldCamelProcessItem;

    private Property oldProperty;

    private boolean isUpdate;

    public SaveAsRoutesWizard(JobEditorInput jobEditorInput) {

        this.jobEditorInput = jobEditorInput;

        RepositoryNode repositoryNode = jobEditorInput.getRepositoryNode();
        // see: RepositoryEditorInput.setRepositoryNode(IRepositoryNode repositoryNode)
        if (repositoryNode == null) {
            repositoryNode = (RepositoryNode) CorePlugin.getDefault().getRepositoryService()
                    .getRepositoryNode(jobEditorInput.getItem().getProperty().getId(), false);
        }

        IRepositoryService service = DesignerPlugin.getDefault().getRepositoryService();
        this.path = service.getRepositoryPath(repositoryNode);

        this.oldCamelProcessItem = (CamelProcessItem) jobEditorInput.getItem();
        oldProperty = this.oldCamelProcessItem.getProperty();

        this.property = PropertiesFactory.eINSTANCE.createProperty();

        assginVlaues(this.property, oldProperty);

        camelProcessItem = CamelPropertiesFactory.eINSTANCE.createCamelProcessItem();

        camelProcessItem.setProperty(property);

        repositoryFactory = service.getProxyRepositoryFactory();

        setDefaultPageImageDescriptor(ImageProvider.getImageDesc(ECoreImage.PROCESS_WIZ));
    }

    @Override
    public void addPages() {
        mainPage = new NewProcessWizardPage(property, path);
        mainPage.initializeSaveAs(oldProperty.getLabel(), oldProperty.getVersion(), true);

        // overwrite it.
        mainPage.setTitle("Save As");
        mainPage.setDescription("Save as another new routes.");

        addPage(mainPage);
        setWindowTitle("Save As");
    }

    @Override
    public boolean performFinish() {

        boolean ok = false;
        try {

            IProcess2 loadedProcess = jobEditorInput.getLoadedProcess();
            ProcessType processType = loadedProcess.saveXmlFile();

            isUpdate = isUpdate();

            if (isUpdate) {
                oldCamelProcessItem.setProcess(processType);

                assginVlaues(oldProperty, property);

                repositoryFactory.save(oldCamelProcessItem);

                // assign value
                camelProcessItem = oldCamelProcessItem;
            } else {
                camelProcessItem.setProcess(processType);

                property.setId(repositoryFactory.getNextId());
                // don't need to add depended routines.

                repositoryFactory.create(camelProcessItem, mainPage.getDestinationPath());
            }
            ok = true;

        } catch (Exception e) {
            MessageDialog.openError(getShell(), "Error", "Job could not be saved: " + e.getMessage());
            ExceptionHandler.process(e);
        }

        return ok;
    }

    public CamelProcessItem getProcess() {
        return this.camelProcessItem;
    }

    public boolean isUpdateOperation() {
        return this.isUpdate;
    }

    // if name is different, it will create a new routes, if name is the same, means to update the routes(version or
    // description...)
    private boolean isUpdate() {
        if (oldProperty.getLabel().trim().equalsIgnoreCase(property.getLabel().trim())) {
            return true;
        } else {
            return false;
        }
    }

    // left = right
    private void assginVlaues(Property leftProperty, Property rightProperty) {
        // 6 fields, don't contains the "locker" and "path". and author , they are the same.
        leftProperty.setLabel(rightProperty.getLabel());
        leftProperty.setPurpose(rightProperty.getPurpose());
        leftProperty.setDescription(rightProperty.getDescription());
        // same author as old one.
        leftProperty.setAuthor(rightProperty.getAuthor());
        leftProperty.setVersion(rightProperty.getVersion());
        leftProperty.setStatusCode(rightProperty.getStatusCode());
    }
}
