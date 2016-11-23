package org.talend.repository.services.ui.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.talend.repository.services.Activator;
import org.talend.repository.services.Messages;

public class ESBPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    public ESBPreferencePage() {
        super(GRID);
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
        setDescription(Messages.ESBPreferencePage_description);
    }

    @Override
    public void init(IWorkbench workbench) {

    }

    @Override
    protected void createFieldEditors() {
    }

}
