// ============================================================================
//
// Talend Community Edition
//
// Copyright (C) 2006-2019 Talend – www.talend.com
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
package org.talend.camel.designer.migration;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.talend.camel.core.model.camelProperties.BeanItem;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.model.general.ModuleNeeded;
import org.talend.core.model.migration.AbstractItemMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.designer.core.model.utils.emf.component.ComponentFactory;
import org.talend.designer.core.model.utils.emf.component.IMPORTType;
import org.talend.librariesmanager.model.ModulesNeededProvider;

/**
 * DOC yyan class global comment. Add esb libraries by default for bean TESB-21419
 *
 */
public class AddBeansDefaultLibrariesMigrationTask extends AbstractItemMigrationTask {

    @Override
    public List<ERepositoryObjectType> getTypes() {
        List<ERepositoryObjectType> toReturn = new ArrayList<ERepositoryObjectType>();
        toReturn.add(ERepositoryObjectType.BEANS);
        return toReturn;
    }

    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2017, 3, 25, 0, 0, 0);
        return gc.getTime();
    }

    @Override
    public ExecutionResult execute(Item item) {
        if (item instanceof BeanItem) {
            BeanItem beanItem = (BeanItem) item;
            addModulesNeededForBeans(beanItem);
            try {
                ProxyRepositoryFactory.getInstance().save(beanItem);
            } catch (PersistenceException e) {
                ExceptionHandler.process(e);
                return ExecutionResult.FAILURE;
            }
            return ExecutionResult.SUCCESS_NO_ALERT;
        } else {
            return ExecutionResult.NOTHING_TO_DO;
        }
    }

    private void addModulesNeededForBeans(BeanItem beanItem) {
        List<ModuleNeeded> importNeedsList = ModulesNeededProvider.getModulesNeededForBeans();

        for (ModuleNeeded model : importNeedsList) {
            IMPORTType importType = ComponentFactory.eINSTANCE.createIMPORTType();
            importType.setMODULE(model.getModuleName());
            importType.setMESSAGE(model.getInformationMsg());
            importType.setREQUIRED(model.isRequired());
            importType.setMVN(model.getMavenUri());
            beanItem.getImports().add(importType);
        }
    }
}
