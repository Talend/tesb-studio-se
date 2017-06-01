package org.talend.designer.esb.components.router.provider;

import org.talend.designer.codegen.additionaljet.AbstractJetFileProvider;

public class RouterComponentsJetFileProvider extends AbstractJetFileProvider {

    /* (non-Javadoc)
     * @see org.talend.designer.codegen.additionaljet.AbstractJetFileProvider#getBundleId()
     */
    @Override
    protected String getBundleId() {
        return "org.talend.designer.esb.components.router"; //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see org.talend.designer.codegen.additionaljet.AbstractJetFileProvider#getJetPath()
     */
    @Override
    protected String getJetPath() {
        return "additional"; //$NON-NLS-1$
    }
}
