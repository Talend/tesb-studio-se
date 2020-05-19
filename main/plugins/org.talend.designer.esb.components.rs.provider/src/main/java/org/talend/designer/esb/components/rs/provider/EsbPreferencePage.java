// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
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

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

public class EsbPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    public EsbPreferencePage() {
        super(GRID);
    }

    @Override
    public void init(IWorkbench workbench) {
        setPreferenceStore(new ScopedPreferenceStore(InstanceScope.INSTANCE, Activator.PLUGIN_ID));
    }

    @Override
    protected void createFieldEditors() {
        StringFieldEditor localRestServiceUri = new StringFieldEditor(Activator.REST_URI_PREFERENCE,
                Messages.EsbPreferencePage_DEFAULT_REST_ENDPOINT_URI, getFieldEditorParent());
        localRestServiceUri.setEmptyStringAllowed(false);
        addField(localRestServiceUri);

        StringFieldEditor defaultServiceNamespace = new StringFieldEditor(Activator.DEFAULT_SL_NAMESPACE_PREF,
                Messages.EsbPreferencePage_SL_NAMESPACE, getFieldEditorParent());
        defaultServiceNamespace.setEmptyStringAllowed(false);
        addField(defaultServiceNamespace);
        
        BooleanFieldEditor ignoreSLStudio = new BooleanFieldEditor(Activator.IGNORE_SL_STUDIO,
                Messages.EsbPreferencePage_IGNORE_SL_STUDIO, getFieldEditorParent());
        addField(ignoreSLStudio);
    }

    @Override
    public boolean performOk() {
        Activator.getDefault().loadCustomProperty();
        return super.performOk();
    }
}
