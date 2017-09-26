// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.repository.services.ui.viewer.tester;

import java.util.HashMap;
import java.util.Map;

import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.repository.services.utils.ESBRepositoryNodeType;
import org.talend.repository.tester.AbstractNodeTypeTester;

/**
 * DOC ggu class global comment. Detailled comment
 */
public class ServicesNodeTester extends AbstractNodeTypeTester {

    @SuppressWarnings("serial")
    private static final Map<String, ERepositoryObjectType> PROPERTY_MAPPING = new HashMap<String, ERepositoryObjectType>() {
        {
            put("isServicesNode", ESBRepositoryNodeType.SERVICES); //$NON-NLS-1$
            put("isServicesPortNode", ESBRepositoryNodeType.SERVICEPORT); //$NON-NLS-1$
            put("isServicesOperationNode", ESBRepositoryNodeType.SERVICESOPERATION); //$NON-NLS-1$
        }
    };

    @Override
    protected Map<String, ERepositoryObjectType> getPropertyMapping() {
        return PROPERTY_MAPPING;
    }

}
