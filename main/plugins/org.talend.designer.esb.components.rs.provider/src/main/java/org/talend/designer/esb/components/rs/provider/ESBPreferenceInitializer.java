package org.talend.designer.esb.components.rs.provider;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

public class ESBPreferenceInitializer extends AbstractPreferenceInitializer {

    public ESBPreferenceInitializer() {
    }

    @Override
    public void initializeDefaultPreferences() {
        IPreferenceStore store = Activator.getDefault().getPreferenceStore();
        store.setDefault(Activator.REST_URI_PREFERENCE, Activator.REST_URI_DEFAULT);
        store.setDefault(Activator.DEFAULT_SL_NAMESPACE_PREF, Activator.DEFAULT_SL_NAMESPACE_DEFAULT);
    }

}
