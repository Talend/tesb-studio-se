package org.talend.designer.camel.dependencies.core.ext;

import java.util.Collection;
import java.util.HashSet;

import org.talend.core.model.process.INode;
import org.talend.designer.camel.dependencies.core.model.ImportPackage;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;

class ExImportPackage extends ExManifestItem<ImportPackage> {

    private static final String SEPARATOR = ","; //$NON-NLS-1$

    private final Collection<ImportPackage> importPackages = new HashSet<>();

    ExImportPackage(String names, boolean isOptional) {
        for (String name : names.split(SEPARATOR)) {
            final ImportPackage importPackage = new ImportPackage();
            importPackage.setBuiltIn(true);
            importPackage.setName(name.trim());
            importPackage.setOptional(isOptional);
            importPackages.add(importPackage);
        }
    }

    @Override
    protected Collection<ImportPackage> to(NodeType t) {
        return importPackages;
    }

    @Override
    protected Collection<ImportPackage> to(INode node) {
        return importPackages;
    }

}
