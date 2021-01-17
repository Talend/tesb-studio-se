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
package org.talend.camel.designer.ui.view.handler.util;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.emf.common.util.EList;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.general.ModuleNeeded;
import org.talend.core.ui.component.ComponentsFactoryProvider;
import org.talend.designer.core.model.utils.emf.component.ComponentFactory;
import org.talend.designer.core.model.utils.emf.component.IMPORTType;
import org.talend.librariesmanager.model.ModulesNeededProvider;

/**
 * DOC jding  class global comment. Detailled comment
 */
public class BeansImportHandlerUtil {

    /**
     * Extract method from org.talend.camel.designer.ui.view.handler.BeanImportHandler.beforeCreatingItem(ImportItem)
     * DOC jding Comment method "arrangeBeansMoudulesImport".
     * 
     * @param imports
     */
    public static void arrangeBeansMoudulesImport(EList<IMPORTType> imports) {
        if (imports == null || imports.isEmpty()) {
            return;
        }

        IComponent component = ComponentsFactoryProvider.getInstance().get("cTimer", "CAMEL");

        String camelVersionSubString = "";
        String camelVersion = "";
        String camelPrefix = "camel-core-";

        for (ModuleNeeded mn : component.getModulesNeeded()) {
            if (mn.getModuleName().startsWith(camelPrefix)) {
                camelVersionSubString = mn.getModuleName().substring(camelPrefix.length());
                camelVersion = camelVersionSubString.substring(0, camelVersionSubString.lastIndexOf(".jar"));
                break;
            }
        }

        // add default modules to the list of modules that are being imported from external project
        List<ModuleNeeded> modulesNeededForBeans = ModulesNeededProvider.getModulesNeededForBeans();

        Set<String> importedModulesForBeansNames = new HashSet<String>();
        for (IMPORTType item : imports) {
            importedModulesForBeansNames.add(item.getMODULE());
        }

        for (ModuleNeeded model : modulesNeededForBeans) {
            if (!importedModulesForBeansNames.contains(model.getModuleName())) {
                IMPORTType importType = ComponentFactory.eINSTANCE.createIMPORTType();
                importType.setMODULE(model.getModuleName());
                importType.setMESSAGE(model.getInformationMsg());
                importType.setREQUIRED(model.isRequired());
                importType.setMVN(model.getMavenUri());
                imports.add(importType);
            }
        }

        String camelCxfPrefix = "camel-cxf-";

        for (Object imp : imports) {

            if (imp instanceof IMPORTType) {
                IMPORTType importType = (IMPORTType) imp;

                String impName = importType.getMODULE().substring(importType.getMODULE().lastIndexOf('-') + 1);
                if (StringUtils.startsWith(importType.getMODULE(), camelCxfPrefix) && "TESB.jar".equals(impName)) {
                    importType.setMODULE(camelCxfPrefix + camelVersionSubString);
                    importType.setMVN("mvn:org.talend.libraries/" + camelCxfPrefix + camelVersion + "/6.0.0-SNAPSHOT/jar");
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
                            // needResave = true;
                        }
                    }
                }
            }
        }
    }

}
