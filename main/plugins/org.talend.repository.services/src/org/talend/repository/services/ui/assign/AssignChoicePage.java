package org.talend.repository.services.ui.assign;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.talend.repository.services.Messages;

public class AssignChoicePage extends WizardPage {

    private Button assignJob;

    private Button newJob;

    private AssignJobPage assignJobPage;

    private IWizardPage createJobPage;

    protected AssignChoicePage(String pageName, AssignJobPage assignJobPage, IWizardPage createJobPage) {
        super(pageName);
        this.assignJobPage = assignJobPage;
        this.createJobPage = createJobPage;
    }

    @Override
    public void createControl(Composite parent) {
        setTitle(Messages.AssignChoicePage_title);
        setDescription(Messages.AssignChoicePage_message);

        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(1, false));

        newJob = new Button(composite, SWT.RADIO);
        newJob.setText(Messages.AssignChoicePage_newJobLabel);

        assignJob = new Button(composite, SWT.RADIO);
        assignJob.setText(Messages.AssignChoicePage_assignJobLabel);

        newJob.setSelection(true);

        setControl(composite);
    }

    @Override
    public IWizardPage getNextPage() {
        if (assignJob.getSelection()) {
            return assignJobPage;
        } else
            return createJobPage;
    }

}
