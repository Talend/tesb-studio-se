// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.camel.designer.ui.view.handler;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.common.util.EList;
import org.talend.camel.core.model.camelProperties.BeanItem;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.general.ModuleNeeded;
import org.talend.core.ui.component.ComponentsFactoryProvider;
import org.talend.designer.core.model.utils.emf.component.ComponentFactory;
import org.talend.designer.core.model.utils.emf.component.IMPORTType;
import org.talend.librariesmanager.model.ModulesNeededProvider;
import org.talend.repository.items.importexport.handlers.imports.ImportRepTypeHandler;
import org.talend.repository.items.importexport.handlers.model.ImportItem;
import org.talend.repository.items.importexport.manager.ResourcesManager;

/**
 * DOC ggu class global comment. Detailled comment
 */
public class BeanImportHandler extends ImportRepTypeHandler {

    /**
     * DOC ggu BeanImportHandler constructor comment.
     */
    public BeanImportHandler() {
        super();
    }

    /*
     * Update import bean libraries version with the studio inside versions.(TESB-23162)
     *
     * @see
     * org.talend.repository.items.importexport.handlers.imports.ImportBasicHandler#afterImportingItems(org.eclipse.core
     * .runtime.IProgressMonitor, org.talend.repository.items.importexport.manager.ResourcesManager,
     * org.talend.repository.items.importexport.handlers.model.ImportItem)
     */
    @Override
    public void afterImportingItems(IProgressMonitor monitor, ResourcesManager resManager, ImportItem importItem) {

    }

    /*
     * Update import bean libraries version with the studio inside versions.(TESB-23162)
     */
    @Override
    protected void beforeCreatingItem(ImportItem importItem) {

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

        super.beforeCreatingItem(importItem);
        if (importItem != null && importItem.getItem() != null && importItem.getItem() instanceof BeanItem) {

            EList<IMPORTType> imports = ((BeanItem) importItem.getItem()).getImports();
            
            //add default modules to the list of modules that are being imported from external project
            List<ModuleNeeded> modulesNeededForBeans = ModulesNeededProvider.getModulesNeededForBeans();

            Set<String> importedModulesForBeansNames = (Set<String>) imports.stream().map(x -> ((IMPORTType)x).getMODULE())
            		.collect(Collectors.toSet());
            
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
}
