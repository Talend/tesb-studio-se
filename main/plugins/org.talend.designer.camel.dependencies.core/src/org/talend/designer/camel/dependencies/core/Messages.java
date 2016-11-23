package org.talend.designer.camel.dependencies.core;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.talend.designer.camel.dependencies.core.messages"; //$NON-NLS-1$

    public static String DependenciesCoreUtil_userDefined;

    public static String ExDependenciesResolver_commonImportPackage;

    public static String ExDependenciesResolver_commonRequireBundle;

    public static String ExDependenciesResolver_generatedPackage;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
