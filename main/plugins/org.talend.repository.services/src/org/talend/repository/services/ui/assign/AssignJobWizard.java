package org.talend.repository.services.ui.assign;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Composite;
import org.talend.designer.core.ui.wizards.NewProcessWizard;
import org.talend.repository.services.Messages;
import org.talend.repository.services.action.AssignJobAction;
import org.talend.repository.services.action.CreateNewJobAction;

public class AssignJobWizard extends Wizard {

    private AssignJobAction assignJobAction;

    private CreateNewJobAction newJobAction;

    private static String CHOICE_PAGE = "Choice_Page";

    private static String ASSIGN_JOB = "Assign_Job";

    private AssignJobPage assignJobPage;

    private IWizardPage createJobPage;

    private NewProcessWizard processWizard;

    private AssignChoicePage assignChoicePage;

    public AssignJobWizard(AssignJobAction assignJobAction, CreateNewJobAction newJobAction) {
        this.assignJobAction = assignJobAction;
        this.newJobAction = newJobAction;
        setWindowTitle(Messages.AssignJobWizard_windowTitle);
    }

    @Override
    public void addPages() {

        assignJobPage = new AssignJobPage(ASSIGN_JOB, assignJobAction);

        processWizard = newJobAction.getNewProcessWizard();
        if (processWizard != null) {
            processWizard.setContainer(getContainer());
            processWizard.addPages();
            createJobPage = processWizard.getPages()[0];
        }
        assignChoicePage = new AssignChoicePage(CHOICE_PAGE, assignJobPage, createJobPage);
        addPage(assignChoicePage);
        addPage(assignJobPage);
        addPage(createJobPage);
    }

    @Override
    public void createPageControls(Composite pageContainer) {
    }

    @Override
    public boolean canFinish() {
        if (assignChoicePage == getContainer().getCurrentPage()) {
            return false;
        }
        return super.canFinish();
    }

    @Override
    public boolean performFinish() {
        IWizardPage currentPage = getContainer().getCurrentPage();
        if (currentPage == assignJobPage) {
            return assignJobPage.finish();
        } else if (currentPage == createJobPage && processWizard != null) {
            processWizard.performFinish();
            return newJobAction.createNewProcess(processWizard);

        }
        return false;
    }

    @Override
    public IWizardPage getNextPage(IWizardPage page) {
        if (page == assignChoicePage) {
            return assignChoicePage.getNextPage();
        }
        return null;
    }

}
