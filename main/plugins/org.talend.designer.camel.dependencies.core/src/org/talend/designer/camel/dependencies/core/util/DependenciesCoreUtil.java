package org.talend.designer.camel.dependencies.core.util;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.eclipse.osgi.util.ManifestElement;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.designer.camel.dependencies.core.Messages;
import org.talend.designer.camel.dependencies.core.model.BundleClasspath;
import org.talend.designer.camel.dependencies.core.model.ExportPackage;
import org.talend.designer.camel.dependencies.core.model.ImportPackage;
import org.talend.designer.camel.dependencies.core.model.ManifestItem;
import org.talend.designer.camel.dependencies.core.model.RequireBundle;

public class DependenciesCoreUtil {

    @Deprecated
    private static final String DELIMITER = "\\|"; //$NON-NLS-1$

    @Deprecated
    private static final String ID_PREFIX = "org.talend.designer.camel.dependencies.core."; //$NON-NLS-1$

    @Deprecated
    private static final String BUNDLE_CLASSPATH_ID = ID_PREFIX + "bundleClasspath"; //$NON-NLS-1$

    @Deprecated
    private static final String IMPORT_PACKAGE_ID = ID_PREFIX + "importPackage"; //$NON-NLS-1$

    @Deprecated
    private static final String REQUIRE_BUNDLE_ID = ID_PREFIX + "requireBundle"; //$NON-NLS-1$

    @Deprecated
    private static final String EXPORT_PACKAGE_ID = ID_PREFIX + "exportPackage"; //$NON-NLS-1$

    private static final char ITEM_SEPARATOR = ',';

    public static void saveToMap(Map<Object, Object> map, Collection<BundleClasspath> bundleClasspaths,
            Collection<ImportPackage> importPackages, Collection<RequireBundle> requiredBundles,
            Collection<ExportPackage> exportPackages) {
        map.remove(BUNDLE_CLASSPATH_ID);
        String result = toManifestString(bundleClasspaths);
        if (result.isEmpty()) {
            map.remove(ManifestItem.BUNDLE_CLASSPATH);
        } else {
            map.put(ManifestItem.BUNDLE_CLASSPATH, result);
        }

        map.remove(IMPORT_PACKAGE_ID);
        result = toManifestString(importPackages);
        if (result.isEmpty()) {
            map.remove(ManifestItem.IMPORT_PACKAGE);
        } else {
            map.put(ManifestItem.IMPORT_PACKAGE, result);
        }

        map.remove(REQUIRE_BUNDLE_ID);
        result = toManifestString(requiredBundles);
        if (result.isEmpty()) {
            map.remove(ManifestItem.REQUIRE_BUNDLE);
        } else {
            map.put(ManifestItem.REQUIRE_BUNDLE, result);
        }

        map.remove(EXPORT_PACKAGE_ID);
        result = toManifestString(exportPackages);
        if (result.isEmpty()) {
            map.remove(ManifestItem.EXPORT_PACKAGE);
        } else {
            map.put(ManifestItem.EXPORT_PACKAGE, result);
        }
    }

    public static Collection<ImportPackage> getStoredImportPackages(Map<?, ?> map) {
        Collection<ImportPackage> list = (Collection<ImportPackage>) getManifestItems(map, ManifestItem.IMPORT_PACKAGE);
        if (null == list) {
            list = new ArrayList<>();
            for (String s : getDependencies(map, IMPORT_PACKAGE_ID)) {
                if (!s.isEmpty()) {
                    ImportPackage importPackage = new ImportPackage();
                    parse(importPackage, s);
                    importPackage.setDescription(
                            MessageFormat.format(Messages.DependenciesCoreUtil_userDefined, ManifestItem.IMPORT_PACKAGE));
                    list.add(importPackage);
                }
            }
        }
        return list;
    }

    public static Collection<BundleClasspath> getStoredBundleClasspaths(Map<?, ?> map) {
        Collection<BundleClasspath> list = (Collection<BundleClasspath>) getManifestItems(map, ManifestItem.BUNDLE_CLASSPATH);
        if (null == list) {
            list = new ArrayList<>();
            for (String s : getDependencies(map, BUNDLE_CLASSPATH_ID)) {
                if (!s.isEmpty()) {
                    BundleClasspath bcp = new BundleClasspath();
                    String[] split = s.split(";"); //$NON-NLS-1$
                    bcp.setName(split[0]);
                    if (split.length > 1) {
                        bcp.setOptional(!Boolean.parseBoolean(split[1]));
                    }
                    // support old routes with null name for unchecked items
                    if (null != bcp.getName() && !"null".equals(bcp.getName()) && !bcp.isOptional()) {
                        list.add(bcp);
                    }
                }
            }
        }
        return list;
    }

    public static Collection<RequireBundle> getStoredRequireBundles(Map<?, ?> map) {
        Collection<RequireBundle> list = (Collection<RequireBundle>) getManifestItems(map, ManifestItem.REQUIRE_BUNDLE);
        if (null == list) {
            list = new ArrayList<>();
            for (String s : getDependencies(map, REQUIRE_BUNDLE_ID)) {
                if (!s.isEmpty()) {
                    RequireBundle requireBundle = new RequireBundle();
                    parse(requireBundle, s);
                    requireBundle.setDescription(
                            MessageFormat.format(Messages.DependenciesCoreUtil_userDefined, ManifestItem.REQUIRE_BUNDLE));
                    list.add(requireBundle);
                }
            }
        }
        return list;
    }

    public static Collection<ExportPackage> getStoredExportPackages(Map<?, ?> map) {
        Collection<ExportPackage> list = (Collection<ExportPackage>) getManifestItems(map, ManifestItem.EXPORT_PACKAGE);
        if (null == list) {
            list = new ArrayList<>();
            for (String s : getDependencies(map, EXPORT_PACKAGE_ID)) {
                if (!s.isEmpty()) {
                    ExportPackage exportPackage = new ExportPackage();
                    parse(exportPackage, s);
                    exportPackage.setDescription(
                            MessageFormat.format(Messages.DependenciesCoreUtil_userDefined, ManifestItem.EXPORT_PACKAGE));
                    list.add(exportPackage);
                }
            }
        }
        return list;
    }

    @Deprecated
    private static String[] getDependencies(Map<?, ?> map, String key) {
        Object stored = map.get(key);
        if (stored != null) {
            return stored.toString().split(DELIMITER);
        }
        return new String[] {};
    }

    @Deprecated
    private static void parse(ManifestItem dependencyItem, String data) {
        final String[] split = data.split(";"); //$NON-NLS-1$
        dependencyItem.setName(split[0]);
        for (int i = 1; i < split.length; i++) {
            final String s = split[i];
            if (s.startsWith(dependencyItem.getVersionAttribute())) {
                dependencyItem.setVersion(s.substring(s.indexOf('"') + 1, s.lastIndexOf('"')));
            } else if (ManifestItem.RESOLUTION_DIRECTIVE_OPTIONAL.equals(s)) {
                dependencyItem.setOptional(true);
            }
        }
    }

    private static Collection<? extends ManifestItem> getManifestItems(Map<?, ?> map, String header) {
        final Object data = map.get(header);
        if (null != data) {
            final Collection<ManifestItem> list = new ArrayList<>();
            final String s = data.toString();
            if (!s.isEmpty()) {
                try {
                    for (ManifestElement me : ManifestElement.parseHeader(header, data.toString())) {
                        final ManifestItem item = ManifestItem.newItem(header);
                        item.setName(me.getValue());
                        item.setVersion(me.getAttribute(item.getVersionAttribute()));
                        item.setOptional(Constants.RESOLUTION_OPTIONAL.equals(me.getDirective(Constants.RESOLUTION_DIRECTIVE)));
                        item.setDescription(MessageFormat.format(Messages.DependenciesCoreUtil_userDefined, header));
                        list.add(item);
                    }
                } catch (BundleException e) {
                    ExceptionHandler.process(e);
                }
            }
            return list;
        }
        return null;
    }

    public static String toManifestString(Collection<? extends ManifestItem> manifestItems, char separator,
            boolean includeBuiltIn) {
        final StringBuilder sb = new StringBuilder();
        for (ManifestItem manifestItem : manifestItems) {
            if (includeBuiltIn || !manifestItem.isBuiltIn()) {
                final String text = manifestItem.toManifestString();
                if (null != text) {
                    if (sb.length() != 0) {
                        sb.append(separator);
                    }
                    sb.append(text);
                }
            }
        }
        return sb.toString();
    }

    private static String toManifestString(Collection<? extends ManifestItem> manifestItems) {
        return toManifestString(manifestItems, ITEM_SEPARATOR, false);
    }
}
