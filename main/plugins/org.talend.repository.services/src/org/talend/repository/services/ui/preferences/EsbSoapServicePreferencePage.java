package org.talend.repository.services.ui.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.talend.commons.ui.swt.advanced.dataeditor.LabelFieldEditor;
import org.talend.commons.ui.swt.preferences.CheckBoxFieldEditor;
import org.talend.repository.services.Activator;
import org.talend.repository.services.Messages;

public class EsbSoapServicePreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    public static final String ENABLE_WSDL_VALIDATION = "ENABLE_WSDL_VALIDATION"; //$NON-NLS-1$

    public EsbSoapServicePreferencePage() {
        super(GRID);
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
        setDescription(Messages.EsbSoapServicePreferencePage_description);
    }

    @Override
    public void init(IWorkbench workbench) {
        getPreferenceStore().setDefault(ENABLE_WSDL_VALIDATION, false);
    }

    @Override
    protected void createFieldEditors() {
        LabelFieldEditor separator = new LabelFieldEditor("", getFieldEditorParent()) { //$NON-NLS-1$

            private Label label = null;

            @Override
            public Label getLabelControl(Composite parent) {
                if (label == null) {
                    label = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
                }
                return label;
            };

            @Override
            protected void doFillIntoGrid(Composite parent, int numColumns) {
                super.doFillIntoGrid(parent, numColumns);
                Object layoutData = label.getLayoutData();
                if (layoutData instanceof GridData) {
                    ((GridData) layoutData).grabExcessHorizontalSpace = true;
                }

            }
        };
        addField(separator);
        addField(new CheckBoxFieldEditor(ENABLE_WSDL_VALIDATION, Messages.EsbSoapServicePreferencePage_enableWsdlValidation,
                getFieldEditorParent()));
    }

}
