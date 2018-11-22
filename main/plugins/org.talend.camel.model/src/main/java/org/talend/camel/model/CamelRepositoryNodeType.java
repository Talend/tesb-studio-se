// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.camel.model;

import org.talend.core.model.repository.ERepositoryObjectType;

/**
 * DOC guanglong.du class global comment. Detailled comment
 */
public interface CamelRepositoryNodeType {

    ERepositoryObjectType repositoryRoutesType = ERepositoryObjectType.PROCESS_ROUTE;

    ERepositoryObjectType repositoryRouteletType = ERepositoryObjectType.PROCESS_ROUTELET;

    ERepositoryObjectType repositoryBeansType = ERepositoryObjectType.valueOf(ERepositoryObjectType.class, "BEANS"); //$NON-NLS-1$

    ERepositoryObjectType repositoryRouteResourceType = ERepositoryObjectType.valueOf(ERepositoryObjectType.class,
            "ROUTE_RESOURCES"); //$NON-NLS-1$

    ERepositoryObjectType repositoryDocumentationType = ERepositoryObjectType.valueOf(ERepositoryObjectType.class, "ROUTE_DOC"); //$NON-NLS-1$

    ERepositoryObjectType repositoryRouteDesinsType = ERepositoryObjectType.valueOf("ROUTE_DESIGNS"); //$NON-NLS-1$

}
