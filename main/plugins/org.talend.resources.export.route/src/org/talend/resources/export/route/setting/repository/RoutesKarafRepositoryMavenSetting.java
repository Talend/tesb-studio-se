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
package org.talend.resources.export.route.setting.repository;

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
import org.talend.resources.export.route.setting.repository.node.RoutesKarafBundleRepositorySettingNode;
import org.talend.resources.export.route.setting.repository.node.RoutesKarafFeatureRepositorySettingNode;
import org.talend.resources.export.route.setting.repository.node.RoutesKarafParentRepositorySettingNode;
import org.talend.resources.export.route.setting.repository.node.RoutesKarafPomRepositorySettingNode;
import org.talend.resources.export.route.setting.repository.node.RoutesKarafRepositorySettingNode;
import org.talend.resources.export.setting.KarafRepositoryMavenSetting;

/**
 * DOC ggu class global comment. Detailled comment
 */
public class RoutesKarafRepositoryMavenSetting extends KarafRepositoryMavenSetting {

    @Override
    protected RepositoryPreferenceNode createKarafFolderNode(String id, ILabelProvider labelProvider, RepositoryNode node) {
        RoutesKarafRepositorySettingNode routesKarafNode = null;

        if (labelProvider == null) {
            routesKarafNode = new RoutesKarafRepositorySettingNode(id, node);
        } else {
            String label = labelProvider.getText(node);
            ImageDescriptor imageDesc = null;
            Image image = labelProvider.getImage(node);
            if (image != null) {
                imageDesc = ImageDescriptor.createFromImageData(image.getImageData());
            }
            routesKarafNode = new RoutesKarafRepositorySettingNode(id, label, imageDesc, node);
        }
        return routesKarafNode;
    }

    @Override
    protected List<IPreferenceNode> createKarafChildrenNodes(IFolder nodeFolder, RepositoryNode node, String parentId,
            boolean checkExist) {
        return createRoutesKarafChildrenNodes(nodeFolder, node, parentId, checkExist);
    }

    public static List<IPreferenceNode> createRoutesKarafChildrenNodes(IFolder nodeFolder, RepositoryNode node, String parentId,
            boolean checkExist) {
        List<IPreferenceNode> childrenNodes = new ArrayList<IPreferenceNode>();

        // if have existed the pom and assembly
        if (!checkExist
                || DesignerMavenUiHelper.existMavenSetting(nodeFolder, TalendMavenConstants.POM_FILE_NAME,
                        IProjectSettingTemplateConstants.MAVEN_KARAF_BUILD_PARENT_FILE_NAME,
                        IProjectSettingTemplateConstants.MAVEN_KARAF_BUILD_BUNDLE_FILE_NAME,
                        IProjectSettingTemplateConstants.MAVEN_KARAF_BUILD_FEATURE_FILE_NAME)) {

            IFile pomTemplateFile = nodeFolder.getFile(TalendMavenConstants.POM_FILE_NAME);
            IFile parentTemplateFile = nodeFolder.getFile(IProjectSettingTemplateConstants.MAVEN_KARAF_BUILD_PARENT_FILE_NAME);
            IFile bundleTemplateFile = nodeFolder.getFile(IProjectSettingTemplateConstants.MAVEN_KARAF_BUILD_BUNDLE_FILE_NAME);
            IFile featureTemplateFile = nodeFolder.getFile(IProjectSettingTemplateConstants.MAVEN_KARAF_BUILD_FEATURE_FILE_NAME);

            String pomId = DesignerMavenUiHelper.buildRepositoryPreferenceNodeId(parentId, pomTemplateFile);
            String parentPomId = DesignerMavenUiHelper.buildRepositoryPreferenceNodeId(parentId, parentTemplateFile);
            String bundlePomId = DesignerMavenUiHelper.buildRepositoryPreferenceNodeId(parentId, bundleTemplateFile);
            String featurePomId = DesignerMavenUiHelper.buildRepositoryPreferenceNodeId(parentId, featureTemplateFile);

            RoutesKarafPomRepositorySettingNode pomNode = new RoutesKarafPomRepositorySettingNode(pomId, pomTemplateFile);
            RoutesKarafParentRepositorySettingNode parentNode = new RoutesKarafParentRepositorySettingNode(parentPomId,
                    parentTemplateFile);
            RoutesKarafBundleRepositorySettingNode bundleNode = new RoutesKarafBundleRepositorySettingNode(bundlePomId,
                    bundleTemplateFile);
            RoutesKarafFeatureRepositorySettingNode featureNode = new RoutesKarafFeatureRepositorySettingNode(featurePomId,
                    featureTemplateFile);

            childrenNodes.add(pomNode);
            childrenNodes.add(parentNode);
            childrenNodes.add(bundleNode);
            childrenNodes.add(featureNode);
        }
        return childrenNodes;
    }

}
