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
package org.talend.esb.tooling.component.provider;

import org.talend.designer.codegen.additionaljet.AbstractJetFileProvider;

public class ESBJetFileProvider extends AbstractJetFileProvider {

    /* (non-Javadoc)
     * @see org.talend.designer.codegen.additionaljet.AbstractJetFileProvider#getBundleId()
     */
    @Override
    protected String getBundleId() {
        return "org.talend.designer.esb.components.ws.consumer"; //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see org.talend.designer.codegen.additionaljet.AbstractJetFileProvider#getJetPath()
     */
    @Override
    protected String getJetPath() {
        return "additional"; //$NON-NLS-1$
    }

}
