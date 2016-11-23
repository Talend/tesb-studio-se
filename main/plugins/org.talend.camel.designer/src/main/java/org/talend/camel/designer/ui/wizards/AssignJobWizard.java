package org.talend.camel.designer.ui.wizards;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Composite;
import org.talend.camel.designer.i18n.Messages;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.RepositoryManager;
import org.talend.core.runtime.CoreRuntimePlugin;
import org.talend.designer.core.model.utils.emf.talendfile.ContextType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;
import org.talend.designer.core.model.utils.emf.talendfile.TalendFileFactory;
import org.talend.designer.core.ui.wizards.NewProcessWizard;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.ui.views.IRepositoryView;

public class AssignJobWizard extends Wizard {

    private static String CHOICE_PAGE = "Choice_Page";

    private static String ASSIGN_JOB = "Assign_Job";

    private AssignJobPage assignJobPage;

    private NewProcessWizard processWizard;

    private AssignChoicePage assignChoicePage;

    private String selectedProcessId;

    public String getSelectedProcessId() {
        return selectedProcessId;
    }

    public AssignJobWizard() {
        setWindowTitle(Messages.getString("AssignJobWizard_windowTitle"));//$NON-NLS-1$
    }

    @Override
    public void addPages() {
        assignJobPage = new AssignJobPage(ASSIGN_JOB);

        assignChoicePage = new AssignChoicePage(CHOICE_PAGE);
        addPage(assignChoicePage);
        addPage(assignJobPage);
    }

    @Override
    public void createPageControls(Composite pageContainer) {
    }

    @Override
    public boolean canFinish() {
        if (assignChoicePage == getContainer().getCurrentPage()) {
            return false;
        }
        if (getContainer().getCurrentPage() == assignJobPage) {
            return assignJobPage.isPageComplete();
        } else if (processWizard != null && getContainer().getCurrentPage() == processWizard.getPages()[0]) {
            return processWizard.getPages()[0].isPageComplete();
        }
        return super.canFinish();
    }

    @Override
    public boolean performFinish() {
        IWizardPage currentPage = getContainer().getCurrentPage();
        if (currentPage == assignJobPage) {
            if (assignJobPage.finish()) {
                selectedProcessId = assignJobPage.getId();
                return true;
            }
            return false;
        } else if (processWizard != null && currentPage == processWizard.getPages()[0]) {
            if (processWizard.performFinish()) {
                addRouteComponents(processWizard.getProcess().getProcess());
                saveCreatedProcess(processWizard.getProcess());
                selectedProcessId = processWizard.getProcess().getProperty().getId();

                // refresh after created
                IRepositoryView repositoryView = RepositoryManager.getRepositoryView();
                IRepositoryNode processNodes = repositoryView.getRoot().getRootRepositoryNode(ERepositoryObjectType.PROCESS);
                if (processNodes instanceof RepositoryNode) {
                    repositoryView.refreshAllChildNodes((RepositoryNode) processNodes);
                }

                return true;
            }
            return false;
        }
        return true;
    }

    @Override
    public IWizardPage getNextPage(IWizardPage page) {
        if (page == assignChoicePage) {
            if (assignChoicePage.isAssignJob()) {
                return assignJobPage;
            } else if (assignChoicePage.isCreateJob()) {
                if (processWizard != null) {
                    processWizard.dispose();
                }
                processWizard = new NewProcessWizard(null);
                processWizard.setContainer(getContainer());
                processWizard.addPages();
                IWizardPage p = processWizard.getPages()[0];
                p.setWizard(this);
                return p;
            }
        }
        return null;
    }

    private void saveCreatedProcess(ProcessItem processItem) {
        IProxyRepositoryFactory proxyRepositoryFactory = CoreRuntimePlugin.getInstance().getProxyRepositoryFactory();
        try {
            proxyRepositoryFactory.save(processItem);
        } catch (PersistenceException e) {
            e.printStackTrace();
        }
    }

    private void addRouteComponents(ProcessType process) {
        NodeType ntRouteInput = TalendFileFactory.eINSTANCE.createNodeType();
        ntRouteInput.setComponentName("tRouteInput");
        ntRouteInput.setPosX(100);
        ntRouteInput.setPosY(100);
        process.getNode().add(ntRouteInput);

        NodeType ntRouteOutput = TalendFileFactory.eINSTANCE.createNodeType();
        ntRouteOutput.setComponentName("tRouteOutput");
        ntRouteOutput.setPosX(400);
        ntRouteOutput.setPosY(100);
        process.getNode().add(ntRouteOutput);

        process.setDefaultContext("Default");
        ContextType defContext = TalendFileFactory.eINSTANCE.createContextType();
        defContext.setName("Default");
        defContext.setConfirmationNeeded(false);
        process.getContext().add(defContext);
    }

}
