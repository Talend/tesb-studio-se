// ============================================================================
//
// Copyright (C) 2006-2015 Talend Inc. - www.talend.com
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

import java.util.Collection;
import java.util.HashSet;

import org.talend.core.model.repository.ERepositoryObjectType;

/**
 * DOC guanglong.du class global comment. Detailled comment
 */
public interface CamelRepositoryNodeType {

    ERepositoryObjectType repositoryRoutesType = ERepositoryObjectType.valueOf(ERepositoryObjectType.class, "ROUTES"); //$NON-NLS-1$

    ERepositoryObjectType repositoryBeansType = ERepositoryObjectType.valueOf(ERepositoryObjectType.class, "BEANS"); //$NON-NLS-1$

    ERepositoryObjectType repositoryRouteResourceType = ERepositoryObjectType.valueOf(ERepositoryObjectType.class,
        "ROUTE_RESOURCES"); //$NON-NLS-1$

    ERepositoryObjectType repositoryDocumentationsType = ERepositoryObjectType.valueOf(ERepositoryObjectType.class,
        "ROUTE_DOCS"); //$NON-NLS-1$

    ERepositoryObjectType repositoryDocumentationType = ERepositoryObjectType.valueOf(ERepositoryObjectType.class,
        "ROUTE_DOC"); //$NON-NLS-1$

    ERepositoryObjectType repositoryRouteletType = ERepositoryObjectType.valueOf(ERepositoryObjectType.class,
        "ROUTELET"); //$NON-NLS-1$

    // repository type and folder name Map
    Collection<ERepositoryObjectType> AllRouteRespositoryTypes = new HashSet<ERepositoryObjectType>() {
        {
            add(repositoryBeansType);
            add(repositoryRouteResourceType);
            add(repositoryRoutesType);
            add(repositoryDocumentationsType);
            add(repositoryDocumentationType);
            add(repositoryRouteletType);
        }
    };

}
