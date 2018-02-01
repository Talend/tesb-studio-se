// ============================================================================
//
// Talend Community Edition
//
// Copyright (C) 2006-2013 Talend – www.talend.com
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
//
// ============================================================================
package org.talend.repository.services.export;

import java.util.Collections;
import java.util.Map;

import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.runtime.process.IBuildJobHandler;
import org.talend.core.runtime.repository.build.IBuildExportHandler;
import org.talend.core.runtime.repository.build.RepositoryObjectTypeBuildProvider;
import org.talend.repository.services.export.handler.BuildDataServiceHandler;
import org.talend.repository.services.model.services.ServiceItem;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.JobScriptsManager.ExportChoice;

/**
 * DOC yyan class global comment. Detailled comment
 *
 */
public class BuildDataServiceProvider extends RepositoryObjectTypeBuildProvider {

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.runtime.repository.build.RepositoryObjectTypeBuildProvider#getObjectType()
     */
    @Override
    protected ERepositoryObjectType getObjectType() {
        return ERepositoryObjectType.getType("Services");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.runtime.repository.build.RepositoryObjectTypeBuildProvider#valid(java.util.Map)
     */
    @Override
    public boolean valid(Map<String, Object> parameters) {
        if (parameters == null || parameters.isEmpty()) {
            return false;
        }

        ERepositoryObjectType type = null;

        Object object = parameters.get(SERVICE);
        if (object != null && object instanceof Property) {
            type = ERepositoryObjectType.getType((Property) object);
        }

        if (type != null && type.getType().equals("SERVICES")) {
            return true;
        }

        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.runtime.repository.build.AbstractBuildProvider#createBuildExportHandler(java.util.Map)
     */
    @Override
    public IBuildExportHandler createBuildExportHandler(Map<String, Object> parameters) {
        if (parameters == null || parameters.isEmpty()) {
            return null;
        }
        final Object item = parameters.get(ITEM);
        if (item == null || !(item instanceof ProcessItem)) {
            return null;
        }
        final Object version = parameters.get(VERSION);
        if (version == null) {
            return null;
        }
        final Object contextGroup = parameters.get(CONTEXT_GROUP);
        if (contextGroup == null) {
            return null;
        }
        Object choiceOption = parameters.get(CHOICE_OPTION);
        if (choiceOption == null) {
            choiceOption = Collections.emptyMap();
        }
        if (!(choiceOption instanceof Map)) {
            return null;
        }
        IBuildJobHandler buildHandler = new BuildDataServiceHandler((ServiceItem) item, version.toString(),
                contextGroup.toString(), (Map<ExportChoice, Object>) choiceOption);
        return buildHandler;
    }
}
