// ============================================================================
//
// Talend Community Edition
//
// Copyright (C) 2006-2018 Talend – www.talend.com
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

import org.apache.commons.lang3.StringUtils;
import org.eclipse.emf.common.util.EList;
import org.talend.camel.core.model.camelProperties.BeanItem;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.general.ModuleNeeded;
import org.talend.core.model.migration.AbstractItemMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.ui.component.ComponentsFactoryProvider;
import org.talend.designer.core.model.utils.emf.component.IMPORTType;
import org.talend.librariesmanager.model.ModulesNeededProvider;

/**
 * Update core libraries version to default for beans, should run before login
 *
 */
public class UpdateBeansDefaultLibrariesMigrationTask extends AbstractItemMigrationTask {

    private static final String CAMEL_CXF_PREFIX = "camel-cxf-";

    private static final String camelPrefix = "camel-core-";

    private static String camelVersionSubString;

    private static String camelVersion;

    @Override
    public List<ERepositoryObjectType> getTypes() {
        List<ERepositoryObjectType> toReturn = new ArrayList<ERepositoryObjectType>();
        toReturn.add(ERepositoryObjectType.valueOf("Beans"));
        return toReturn;
    }

    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2019, 5, 20, 0, 0, 0);
        return gc.getTime();
    }

    @Override
    public ExecutionResult execute(Item item) {
        if (item instanceof BeanItem) {
            if (camelVersion == null) {
                IComponent component = ComponentsFactoryProvider.getInstance().get("cTimer", "CAMEL");
                for (ModuleNeeded mn : component.getModulesNeeded()) {
                    if (mn.getModuleName().startsWith(camelPrefix)) {
                        camelVersionSubString = mn.getModuleName().substring(camelPrefix.length());
                        camelVersion = camelVersionSubString.substring(0, camelVersionSubString.lastIndexOf(".jar"));
                        break;
                    }
                }
            }
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
        EList imports = beanItem.getImports();

        for (Object imp : imports) {

            if (imp instanceof IMPORTType) {
                IMPORTType importType = (IMPORTType) imp;

                String impName = importType.getMODULE().substring(importType.getMODULE().lastIndexOf('-') + 1);
                if (StringUtils.startsWith(importType.getMODULE(), CAMEL_CXF_PREFIX) && "TESB.jar".equals(impName)) {
                    importType.setMODULE(CAMEL_CXF_PREFIX + camelVersionSubString);
                    importType.setMVN("mvn:org.talend.libraries/" + CAMEL_CXF_PREFIX + camelVersion + "/6.0.0-SNAPSHOT/jar");
                }
            }

            for (ModuleNeeded defaultNeed : ModulesNeededProvider.getModulesNeededForBeans()) {
                String moduleName = defaultNeed.getId();

                if (imp instanceof IMPORTType) {
                    IMPORTType importType = (IMPORTType) imp;

                    if (importType.getMODULE().indexOf('-') > 0) {
                        String impName = importType.getMODULE().substring(0, importType.getMODULE().lastIndexOf('-'));
                        if (moduleName.equals(impName) && !importType.getMODULE().equals(defaultNeed.getModuleName())) {
                            importType.setMODULE(defaultNeed.getModuleName());
                            importType.setMESSAGE(defaultNeed.getInformationMsg());
                            importType.setMVN(defaultNeed.getMavenUri());
                        }
                    }
                }
            }
        }
    }
}
