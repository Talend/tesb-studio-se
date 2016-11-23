package org.talend.designer.camel.dependencies.core.ext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.talend.core.model.process.INode;
import org.talend.designer.camel.dependencies.core.model.BundleClasspath;
import org.talend.designer.camel.dependencies.core.model.ImportPackage;
import org.talend.designer.camel.dependencies.core.model.ManifestItem;
import org.talend.designer.camel.dependencies.core.model.RequireBundle;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;

public class ExtensionPointsReader {

    private static final String IMPORT_PACKAGE = "importPackage"; //$NON-NLS-1$

    private static final String REQUIRE_BUNDLE = "requireBundle"; //$NON-NLS-1$

    private static final String NAME = "name"; //$NON-NLS-1$

    private static final String COMPONENT = "component"; //$NON-NLS-1$

    private static final String ATTRIBUTE_VALUE = "attributeValue"; //$NON-NLS-1$

    private static final String PREDICATE = "predicate"; //$NON-NLS-1$

    private static final String ATTRIBUTE_NAME = "attributeName"; //$NON-NLS-1$

    private static final String PARAMETER = "parameter"; //$NON-NLS-1$

    private static final String OPTIONAL = "optional"; //$NON-NLS-1$

    private static final String COMPONENT_NAME = "componentName"; //$NON-NLS-1$

    private static final String REQUIRE_BUNDLE_EXT = "org.talend.designer.camel.dependencies.requireBundle"; //$NON-NLS-1$

    private static final String IMPORT_PACKAGE_EXT = "org.talend.designer.camel.dependencies.importPackage"; //$NON-NLS-1$

    private static final String BUNDLE_CLASSPATH_EXT = "org.talend.designer.camel.dependencies.bundleClasspath"; //$NON-NLS-1$

    public static ExtensionPointsReader INSTANCE = new ExtensionPointsReader();

    private final Map<String, Collection<ExBundleClasspath>> componentBundleClasspaths = new HashMap<>();

    private final Map<String, Collection<ExImportPackage>> componentImportPackages = new HashMap<>();

    private final Map<String, Collection<ExRequireBundle>> componentRequireBundles = new HashMap<>();

    private final Collection<ExRequireBundle> requireBundlesForAll = new ArrayList<>();

    private final Collection<ExImportPackage> importPackagesForAll = new ArrayList<>();

    private ExtensionPointsReader() {
        readRegisteredBundleClasspaths();
        readRegisteredImportPackages();
        readRegisteredRequireBundles();
    }

    private void readRegisteredBundleClasspaths() {
        final IConfigurationElement[] configurationElements = Platform.getExtensionRegistry()
                .getConfigurationElementsFor(BUNDLE_CLASSPATH_EXT);
        for (IConfigurationElement e : configurationElements) {
            final String cmpName = e.getAttribute(NAME);
            final ExBundleClasspath bc = new ExBundleClasspath(e.getAttribute(PARAMETER),
                    Boolean.parseBoolean(e.getAttribute(OPTIONAL)));
            parsePredicates(bc, e);

            Collection<ExBundleClasspath> attributeSet = componentBundleClasspaths.get(cmpName);
            if (attributeSet == null) {
                attributeSet = new ArrayList<>();
                componentBundleClasspaths.put(cmpName, attributeSet);
            }
            attributeSet.add(bc);
        }
    }

    private void readRegisteredImportPackages() {
        final IConfigurationElement[] configurationElements = Platform.getExtensionRegistry()
                .getConfigurationElementsFor(IMPORT_PACKAGE_EXT);
        for (IConfigurationElement e : configurationElements) {
            final String name = e.getName();
            if (COMPONENT.equals(name)) {
                final String cmpName = e.getAttribute(COMPONENT_NAME);
                Collection<ExImportPackage> packageSet = componentImportPackages.get(cmpName);
                if (packageSet == null) {
                    packageSet = new ArrayList<>();
                    componentImportPackages.put(cmpName, packageSet);
                }
                for (IConfigurationElement p : e.getChildren(IMPORT_PACKAGE)) {
                    packageSet.add(createImportPackageFrom(p));
                }
            } else {
                importPackagesForAll.add(createImportPackageFrom(e));
            }
        }
    }

    private ExImportPackage createImportPackageFrom(final IConfigurationElement p) {
        final ExImportPackage importPackage = new ExImportPackage(p.getAttribute(NAME),
                Boolean.parseBoolean(p.getAttribute(OPTIONAL)));
        parsePredicates(importPackage, p);
        return importPackage;
    }

    private void readRegisteredRequireBundles() {
        final IConfigurationElement[] configurationElements = Platform.getExtensionRegistry()
                .getConfigurationElementsFor(REQUIRE_BUNDLE_EXT);
        for (IConfigurationElement e : configurationElements) {
            final String name = e.getName();
            if (COMPONENT.equals(name)) {
                final String cmpName = e.getAttribute(COMPONENT_NAME);
                Collection<ExRequireBundle> bundleSet = componentRequireBundles.get(cmpName);
                if (bundleSet == null) {
                    bundleSet = new ArrayList<>();
                    componentRequireBundles.put(cmpName, bundleSet);
                }
                for (IConfigurationElement b : e.getChildren(REQUIRE_BUNDLE)) {
                    bundleSet.add(createRequireBundleFrom(b));
                }
            } else {
                requireBundlesForAll.add(createRequireBundleFrom(e));
            }
        }
    }

    private ExRequireBundle createRequireBundleFrom(final IConfigurationElement b) {
        final ExRequireBundle requireBundle = new ExRequireBundle(b.getAttribute(NAME),
                Boolean.parseBoolean(b.getAttribute(OPTIONAL)));
        parsePredicates(requireBundle, b);
        return requireBundle;
    }

    private static void parsePredicates(final ExManifestItem<?> abstractExPredicator, final IConfigurationElement element) {
        for (final IConfigurationElement pe : element.getChildren(PREDICATE)) {
            abstractExPredicator.addPredicate(pe.getAttribute(ATTRIBUTE_NAME), pe.getAttribute(ATTRIBUTE_VALUE));
        }
    }

    public Collection<ImportPackage> getImportPackages(String componentName) {
        return getManifestItems(componentImportPackages.get(componentName), (NodeType) null);
    }

    public Collection<ImportPackage> getImportPackages(final NodeType nodeType) {
        if (null != nodeType) {
            return getManifestItems(componentImportPackages.get(nodeType.getComponentName()), nodeType);
        }
        return getManifestItems(importPackagesForAll, nodeType);
    }

    public Collection<RequireBundle> getRequireBundles(final NodeType nodeType) {
        if (null != nodeType) {
            return getManifestItems(componentRequireBundles.get(nodeType.getComponentName()), nodeType);
        }
        return getManifestItems(requireBundlesForAll, nodeType);
    }

    public Collection<BundleClasspath> getBundleClasspaths(final NodeType nodeType) {
        return getManifestItems(componentBundleClasspaths.get(nodeType.getComponentName()), nodeType);
    }

    public Collection<ImportPackage> getImportPackages(final INode node) {
        if (null != node) {
            return getManifestItems(componentImportPackages.get(node.getComponent().getName()), node);
        }
        return getManifestItems(importPackagesForAll, node);
    }

    public Collection<RequireBundle> getRequireBundles(final INode node) {
        if (null != node) {
            return getManifestItems(componentRequireBundles.get(node.getComponent().getName()), node);
        }
        return getManifestItems(requireBundlesForAll, node);
    }

    public Collection<BundleClasspath> getBundleClasspaths(final INode node) {
        return getManifestItems(componentBundleClasspaths.get(node.getComponent().getName()), node);
    }

    private static <T extends ManifestItem> Collection<T> getManifestItems(
            final Collection<? extends ExManifestItem<T>> exManifestItems, final NodeType nodeType) {
        if (exManifestItems != null) {
            final Collection<T> result = new HashSet<>();
            for (ExManifestItem<T> ip : exManifestItems) {
                result.addAll(ip.toTargets(nodeType));
            }
            return result;
        }
        return Collections.emptyList();
    }

    private static <T extends ManifestItem> Collection<T> getManifestItems(
            final Collection<? extends ExManifestItem<T>> exManifestItems, final INode node) {
        if (exManifestItems != null) {
            final Collection<T> result = new HashSet<>();
            for (ExManifestItem<T> ip : exManifestItems) {
                result.addAll(ip.toTargets(node));
            }
            return result;
        }
        return Collections.emptyList();
    }

}
