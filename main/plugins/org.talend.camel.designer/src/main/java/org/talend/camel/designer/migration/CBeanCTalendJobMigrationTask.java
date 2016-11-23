package org.talend.camel.designer.migration;

import java.util.Date;
import java.util.GregorianCalendar;

import org.eclipse.emf.common.util.EList;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.TalendFileFactory;

public class CBeanCTalendJobMigrationTask extends AbstractRouteItemComponentMigrationTask {

    @Override
    public String getComponentNameRegex() {
        return "cBean|cTalendJob";
    }

    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2012, 9, 18, 14, 00, 00);
        return gc.getTime();
    }

    @Override
    protected boolean execute(NodeType node) throws Exception {
        if (node.getComponentName().equals("cBean")) {
            return migrateCBean(node);
        }
        return migrateCTalendJob(node);
    }

    private boolean migrateCBean(NodeType currentNode) {
        EList elementParameter = currentNode.getElementParameter();
        boolean isNewElement = false;
        for (Object obj : elementParameter) {
            if (!(obj instanceof ElementParameterType)) {
                continue;
            }
            ElementParameterType param = (ElementParameterType) obj;
            String name = param.getName();
            if ("FROM_REGISTRY".equals(name) || "FROM_CLASS".equals(name)) {
                isNewElement = true;
                break;
            }
        }
        if (isNewElement) {
            return false;
        }

        ElementParameterType fromClass = TalendFileFactory.eINSTANCE.createElementParameterType();
        fromClass.setName("FROM_CLASS");
        fromClass.setValue("true");
        elementParameter.add(fromClass);

        ElementParameterType fromRegistry = TalendFileFactory.eINSTANCE.createElementParameterType();
        fromRegistry.setName("FROM_REGISTRY");
        fromRegistry.setValue("false");
        elementParameter.add(fromRegistry);
        return true;
    }

    private boolean migrateCTalendJob(NodeType currentNode) {
        EList elementParameter = currentNode.getElementParameter();
        boolean isNewElement = false;
        for (Object obj : elementParameter) {
            if (!(obj instanceof ElementParameterType)) {
                continue;
            }
            ElementParameterType param = (ElementParameterType) obj;
            String name = param.getName();
            if ("FROM_REPOSITORY_JOB".equals(name) || "FROM_EXTERNAL_JAR".equals(name)) {
                isNewElement = true;
                break;
            }
        }
        if (isNewElement) {
            return false;
        }

        ElementParameterType fromExternal = TalendFileFactory.eINSTANCE.createElementParameterType();
        fromExternal.setName("FROM_EXTERNAL_JAR");
        fromExternal.setValue("true");
        elementParameter.add(fromExternal);

        ElementParameterType fromRepository = TalendFileFactory.eINSTANCE.createElementParameterType();
        fromRepository.setName("FROM_REPOSITORY_JOB");
        fromRepository.setValue("false");
        elementParameter.add(fromRepository);
        return true;
    }

}