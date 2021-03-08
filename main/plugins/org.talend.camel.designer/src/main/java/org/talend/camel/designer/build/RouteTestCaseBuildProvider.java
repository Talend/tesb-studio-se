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
package org.talend.camel.designer.build;

import java.util.Collections;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.talend.core.model.components.ComponentCategory;
import org.talend.core.runtime.repository.build.IMavenPomCreator;
import org.talend.core.runtime.repository.build.TestCaseBuildProvider;
import org.talend.designer.runprocess.IProcessor;

/**
 * DOC ggu class global comment. Detailled comment
 */
public class RouteTestCaseBuildProvider extends TestCaseBuildProvider {

    @Override
    public boolean valid(Map<String, Object> parameters) {
        final boolean valid = super.valid(parameters);
        if (valid) {
            final Object processor = parameters.get(PROCESSOR);
            if (processor != null
                    && processor instanceof IProcessor
                    // same as CreateMavenTestPom in constructor
                    && (ComponentCategory.CATEGORY_4_CAMEL.getName().equals(((IProcessor) processor).getProcess()
                            .getComponentsType()))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public IMavenPomCreator createPomCreator(Map<String, Object> parameters) {
        if (parameters == null || parameters.isEmpty()) {
            return null;
        }
        final Object processor = parameters.get(PROCESSOR);
        if (processor == null || !(processor instanceof IProcessor)) {
            return null;
        }
        final Object pomFile = parameters.get(FILE_POM);
        if (pomFile == null || !(pomFile instanceof IFile)) {
            return null;
        }
        Object argumentsMap = parameters.get(ARGUMENTS_MAP);
        if (argumentsMap == null) {
            argumentsMap = Collections.emptyMap();
        }
        if (!(argumentsMap instanceof Map)) {
            return null;
        }
        Object overwrite = parameters.get(OVERWRITE_POM);
        if (overwrite == null) {
            overwrite = Boolean.FALSE;
        }
        CreateRouteMavenTestPom creator = new CreateRouteMavenTestPom((IProcessor) processor, (IFile) pomFile);
        creator.setArgumentsMap((Map<String, Object>) argumentsMap);
        creator.setOverwrite(Boolean.parseBoolean(overwrite.toString()));
        return creator;
    }
}
