package org.talend.camel.designer.ui.wizards;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.talend.camel.designer.i18n.Messages;

public class AssignChoicePage extends WizardPage {

    private Button assignJob;

    private Button newJob;

    protected AssignChoicePage(String pageName) {
        super(pageName);
    }

    @Override
    public void createControl(Composite parent) {
        setTitle(Messages.getString("AssignChoicePage_title"));//$NON-NLS-1$
        setDescription(Messages.getString("AssignChoicePage_message"));//$NON-NLS-1$

        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(1, false));

        newJob = new Button(composite, SWT.RADIO);
        newJob.setText(Messages.getString("AssignChoicePage_newJobLabel"));//$NON-NLS-1$

        assignJob = new Button(composite, SWT.RADIO);
        assignJob.setText(Messages.getString("AssignChoicePage_assignJobLabel"));//$NON-NLS-1$

        newJob.setSelection(true);

        setControl(composite);
    }

    public boolean isAssignJob() {
        return assignJob.getSelection();
    }

    public boolean isCreateJob() {
        return newJob.getSelection();
    }

}
