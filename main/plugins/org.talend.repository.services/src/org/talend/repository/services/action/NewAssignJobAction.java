package org.talend.repository.services.action;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.WizardDialog;
import org.talend.commons.ui.runtime.image.ECoreImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.repository.services.Messages;
import org.talend.repository.services.ui.assign.AssignJobWizard;
import org.talend.repository.services.ui.assign.AssignJobWizardDialog;
import org.talend.repository.ui.actions.AContextualAction;

public class NewAssignJobAction extends AContextualAction {

    private AssignJobAction assignJobAction;

    private CreateNewJobAction newJobAction;

    public NewAssignJobAction() {
        super();
        this.setText(Messages.NewAssignJobAction_actionText);
        this.setToolTipText(Messages.NewAssignJobAction_actionTooltip);
        this.setImageDescriptor(ImageProvider.getImageDesc(ECoreImage.PROCESS_ICON));
        assignJobAction = new AssignJobAction();
        newJobAction = new CreateNewJobAction();
    }

    @Override
    public void init(TreeViewer viewer, IStructuredSelection selection) {
        assignJobAction.init(viewer, selection);
        newJobAction.init(viewer, selection);
        setEnabled(assignJobAction.isEnabled());
    }

    @Override
    protected void doRun() {
        AssignJobWizard assignJobWizard = new AssignJobWizard(assignJobAction, newJobAction);
        WizardDialog wizardDialog = new AssignJobWizardDialog(getWorkbench().getActiveWorkbenchWindow().getShell(),
                assignJobWizard);
        wizardDialog.open();
    }

}
