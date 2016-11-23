// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.esb.components.rs.provider;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class EsbPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    public EsbPreferencePage() {
        super(GRID);
    }

    @Override
    public void init(IWorkbench workbench) {
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
    }

    @Override
    protected void createFieldEditors() {
        StringFieldEditor localRestServiceUri = new StringFieldEditor(Activator.REST_URI_PREFERENCE,
                Messages.getString("esb.preferences.rest.service.uri.default"), getFieldEditorParent());
        localRestServiceUri.setEmptyStringAllowed(false);
        addField(localRestServiceUri);

        StringFieldEditor defaultServiceNamespace = new StringFieldEditor(Activator.DEFAULT_SL_NAMESPACE_PREF,
                Messages.EsbPreferencePage_SL_NAMESPACE, getFieldEditorParent());
        defaultServiceNamespace.setEmptyStringAllowed(false);
        addField(defaultServiceNamespace);
    }

    @Override
    public boolean performOk() {
        boolean result = super.performOk();
        Activator.getDefault().loadCustomProperty();
        return result;
    }
}
