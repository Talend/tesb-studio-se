// ============================================================================
//
// Copyright (C) 2006-2021 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.repository.view.route.viewer.tester;

import java.util.HashMap;
import java.util.Map;

import org.talend.camel.model.CamelRepositoryNodeType;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.repository.tester.AbstractNodeTypeTester;

/**
 * DOC ggu class global comment. Detailled comment
 */
public class RouteNodeTester extends AbstractNodeTypeTester {

    @SuppressWarnings("serial")
    private static final Map<String, ERepositoryObjectType> PROPERTY_MAPPING = new HashMap<String, ERepositoryObjectType>() {
        {
            put("isRoutesTopNode", CamelRepositoryNodeType.repositoryRoutesType); //$NON-NLS-1$
            put("isBeans", CamelRepositoryNodeType.repositoryBeansType); //$NON-NLS-1$
            put("isBeansJar", ERepositoryObjectType.BEANSJAR); //$NON-NLS-1$
        }
    };

    @Override
    protected Map<String, ERepositoryObjectType> getPropertyMapping() {
        return PROPERTY_MAPPING;
    }

}
