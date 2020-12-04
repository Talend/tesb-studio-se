// ============================================================================
//
// Copyright (C) 2006-2020 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.camel.designer.ui.wizards;

import org.eclipse.core.runtime.IPath;
import org.talend.core.model.properties.Property;
import org.talend.core.model.routines.RoutinesUtil;

public class CamelNewInnerBeanWizard extends CamelNewBeanWizard {

    public CamelNewInnerBeanWizard(IPath path) {
        super(path);
    }

    @Override
    protected void addAdditionalProperties(Property property) {
        RoutinesUtil.setInnerCodes(property, true);
    }

}
