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
package org.talend.resources.export.service.setting.repository;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.graphics.Image;
import org.talend.core.runtime.projectsetting.IProjectSettingTemplateConstants;
import org.talend.designer.maven.model.TalendMavenConstants;
import org.talend.designer.maven.ui.setting.repository.node.RepositoryPreferenceNode;
import org.talend.designer.maven.ui.utils.DesignerMavenUiHelper;
import org.talend.repository.model.RepositoryNode;
import org.talend.resources.export.service.setting.repository.node.ServicesKarafBundleRepositorySettingNode;
import org.talend.resources.export.service.setting.repository.node.ServicesKarafFeatureRepositorySettingNode;
import org.talend.resources.export.service.setting.repository.node.ServicesKarafParentRepositorySettingNode;
import org.talend.resources.export.service.setting.repository.node.ServicesKarafPomRepositorySettingNode;
import org.talend.resources.export.service.setting.repository.node.ServicesKarafRepositorySettingNode;
import org.talend.resources.export.setting.KarafRepositoryMavenSetting;

/**
 * DOC ggu class global comment. Detailled comment
 */
public class ServiceKarafRepositoryMavenSetting extends KarafRepositoryMavenSetting {

    @Override
    protected RepositoryPreferenceNode createKarafFolderNode(String id, ILabelProvider labelProvider, RepositoryNode node) {
        ServicesKarafRepositorySettingNode servicesKarafNode = null;

        if (labelProvider == null) {
            servicesKarafNode = new ServicesKarafRepositorySettingNode(id, node);
        } else {
            String label = labelProvider.getText(node);
            ImageDescriptor imageDesc = null;
            Image image = labelProvider.getImage(node);
            if (image != null) {
                imageDesc = ImageDescriptor.createFromImageData(image.getImageData());
            }
            servicesKarafNode = new ServicesKarafRepositorySettingNode(id, label, imageDesc, node);
        }
        return servicesKarafNode;

    }

    @Override
    protected List<IPreferenceNode> createKarafChildrenNodes(IFolder nodeFolder, RepositoryNode node, String parentId,
            boolean checkExist) {
        return createServicesKarafChildrenNodes(nodeFolder, node, parentId, checkExist);
    }

    public static List<IPreferenceNode> createServicesKarafChildrenNodes(IFolder nodeFolder, RepositoryNode node,
            String parentId, boolean checkExist) {
        List<IPreferenceNode> childrenNodes = new ArrayList<IPreferenceNode>();

        // if have existed the pom and assembly
        if (!checkExist
                || DesignerMavenUiHelper.existMavenSetting(nodeFolder, TalendMavenConstants.POM_FILE_NAME,
                        IProjectSettingTemplateConstants.MAVEN_KARAF_BUILD_PARENT_FILE_NAME,
                        IProjectSettingTemplateConstants.MAVEN_CONTROL_BUILD_BUNDLE_FILE_NAME,
                        IProjectSettingTemplateConstants.MAVEN_KARAF_BUILD_FEATURE_FILE_NAME)) {

            IFile pomTemplateFile = nodeFolder.getFile(TalendMavenConstants.POM_FILE_NAME);
            IFile parentTemplateFile = nodeFolder.getFile(IProjectSettingTemplateConstants.MAVEN_KARAF_BUILD_PARENT_FILE_NAME);
            IFile bundleTemplateFile = nodeFolder.getFile(IProjectSettingTemplateConstants.MAVEN_CONTROL_BUILD_BUNDLE_FILE_NAME);
            IFile featureTemplateFile = nodeFolder.getFile(IProjectSettingTemplateConstants.MAVEN_KARAF_BUILD_FEATURE_FILE_NAME);

            String pomId = DesignerMavenUiHelper.buildRepositoryPreferenceNodeId(parentId, pomTemplateFile);
            String parentPomId = DesignerMavenUiHelper.buildRepositoryPreferenceNodeId(parentId, parentTemplateFile);
            String bundlePomId = DesignerMavenUiHelper.buildRepositoryPreferenceNodeId(parentId, bundleTemplateFile);
            String featurePomId = DesignerMavenUiHelper.buildRepositoryPreferenceNodeId(parentId, featureTemplateFile);

            ServicesKarafPomRepositorySettingNode pomNode = new ServicesKarafPomRepositorySettingNode(pomId, pomTemplateFile);
            ServicesKarafParentRepositorySettingNode parentNode = new ServicesKarafParentRepositorySettingNode(parentPomId,
                    parentTemplateFile);
            ServicesKarafBundleRepositorySettingNode bundleNode = new ServicesKarafBundleRepositorySettingNode(bundlePomId,
                    bundleTemplateFile);
            ServicesKarafFeatureRepositorySettingNode featureNode = new ServicesKarafFeatureRepositorySettingNode(featurePomId,
                    featureTemplateFile);

            childrenNodes.add(pomNode);
            childrenNodes.add(parentNode);
            childrenNodes.add(bundleNode);
            childrenNodes.add(featureNode);
        }
        return childrenNodes;
    }

}
