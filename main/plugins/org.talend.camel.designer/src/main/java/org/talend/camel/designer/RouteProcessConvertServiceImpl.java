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
package org.talend.camel.designer;

import org.talend.camel.core.model.camelProperties.CamelPropertiesPackage;
import org.talend.camel.core.model.camelProperties.impl.CamelProcessItemImpl;
import org.talend.camel.designer.ui.editor.MicroServiceProcess;
import org.talend.camel.designer.ui.editor.RouteProcess;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.properties.Item;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.designer.core.convert.IProcessConvertService;
import org.talend.designer.core.convert.ProcessConverterType;
import org.talend.repository.model.RepositoryNode;

public class RouteProcessConvertServiceImpl implements IProcessConvertService {

    @Override
    public IProcess getProcessFromItem(Item item, boolean loadScreenshots) {
        if (item.eClass() == CamelPropertiesPackage.Literals.CAMEL_PROCESS_ITEM) {
            RouteProcess process = new RouteProcess(item.getProperty());
            if (item instanceof CamelProcessItemImpl) {
                CamelProcessItemImpl camelProcessItemImpl = (CamelProcessItemImpl) item;
                if (camelProcessItemImpl.isExportMicroService()) {
                    process = new MicroServiceProcess(item.getProperty());
                }
            }
            process.loadXmlFile(loadScreenshots);
            return process;
        }
        return null;
    }

    @Override
    public Item convertToProcess(Item item, IRepositoryViewObject repViewObject) {
        return null;
    }

    @Override
    public Item convertFromProcess(Item item, IRepositoryViewObject repViewObject) {
        return null;
    }

    @Override
    public boolean isOriginalItemDeleted() {
        return false;
    }

    @Override
    public boolean isNewItemCreated() {
        return false;
    }

    @Override
    public ProcessConverterType getConverterType() {
        return ProcessConverterType.CONVERTER_FOR_ROUTE;
    }

    @Override
    public Item convertFromProcess(Item item, IRepositoryViewObject repViewObject, RepositoryNode targetNode) {
        return null;
    }

    @Override
    public Item convertToProcess(Item item, IRepositoryViewObject repViewObject, RepositoryNode targetNode) {
        return null;
    }

}
