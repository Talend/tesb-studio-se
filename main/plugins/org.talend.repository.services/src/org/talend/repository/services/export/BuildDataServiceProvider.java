// ============================================================================
//
// Talend Community Edition
//
// Copyright (C) 2006-2021 Talend – www.talend.com
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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.IPath;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.repository.utils.ItemResourceUtil;
import org.talend.core.runtime.process.IBuildJobHandler;
import org.talend.core.runtime.repository.build.IBuildExportHandler;
import org.talend.core.runtime.repository.build.IMavenPomCreator;
import org.talend.core.runtime.repository.build.RepositoryObjectTypeBuildProvider;
import org.talend.designer.maven.tools.creator.CreateMavenStandardJobOSGiPom;
import org.talend.designer.runprocess.IProcessor;
import org.talend.repository.services.maven.CreateMavenDataServicePom;
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
        if (type == null) {
            object = parameters.get(ITEM);
            if (object != null && object instanceof ServiceItem) {
                Property property = ((Item) object).getProperty();
                if (property != null) {
                    type = ERepositoryObjectType.getType(property);
                }
            }
        }
        if (type != null && type.getType().equals("SERVICES")) {
            return true;
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
        final Object item = parameters.get(ITEM);
        if (item == null || !(item instanceof Item)) {
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
        Object assemblyFile = parameters.get(FILE_ASSEMBLY);

        CreateMavenDataServicePom creator = new CreateMavenDataServicePom((IProcessor) processor, (IFile) pomFile);

        creator.setArgumentsMap((Map<String, Object>) argumentsMap);
        creator.setOverwrite(Boolean.parseBoolean(overwrite.toString()));
        creator.setAssemblyFile((IFile) assemblyFile);

        final Property itemProperty = ((Item) item).getProperty();
        IPath itemLocationPath = ItemResourceUtil.getItemLocationPath(itemProperty);
        IFolder objectTypeFolder = ItemResourceUtil.getObjectTypeFolder(itemProperty);
        if (itemLocationPath != null && objectTypeFolder != null) {
            IPath itemRelativePath = itemLocationPath.removeLastSegments(1).makeRelativeTo(objectTypeFolder.getLocation());
            creator.setObjectTypeFolder(objectTypeFolder);
            creator.setItemRelativePath(itemRelativePath);
        }

        return creator;
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
        if (item == null || !(item instanceof ServiceItem)) {
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
