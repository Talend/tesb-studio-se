package org.talend.designer.camel.dependencies.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeSet;

import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.ElementParameterParser;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.relationship.RelationshipItemBuilder;
import org.talend.core.model.utils.JavaResourcesHelper;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.designer.camel.dependencies.core.ext.ExtensionPointsReader;
import org.talend.designer.camel.dependencies.core.model.BundleClasspath;
import org.talend.designer.camel.dependencies.core.model.ExportPackage;
import org.talend.designer.camel.dependencies.core.model.ImportPackage;
import org.talend.designer.camel.dependencies.core.model.ManifestItem;
import org.talend.designer.camel.dependencies.core.model.RequireBundle;
import org.talend.designer.camel.dependencies.core.util.DependenciesCoreUtil;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.utils.emf.talendfile.ConnectionType;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;

public class DependenciesResolver {

    /**
     * builtIn items will be sorted automatically and can't be re-sorted Non-BuiltIn items no sort and can be re-sorted
     */
    private static final Comparator<ManifestItem> SORTER = new Comparator<ManifestItem>() {

        @Override
        public int compare(ManifestItem e1, ManifestItem e2) {
            return e1.toString().compareTo(e2.toString());
        }
    };

    private Collection<ImportPackage> importPackages = new TreeSet<>(SORTER);

    private Collection<RequireBundle> requireBundles = new TreeSet<>(SORTER);

    private Collection<BundleClasspath> bundleClasspaths = new TreeSet<>(SORTER);

    private final Collection<ExportPackage> exportPackages = new ArrayList<>();

    private Collection<BundleClasspath> userBundleClasspaths;

    public DependenciesResolver(final ProcessItem item) {
        for (ImportPackage importPackage : ExtensionPointsReader.INSTANCE.getImportPackages((NodeType) null)) {
            importPackage.setDescription(Messages.ExDependenciesResolver_commonImportPackage);
            importPackages.add(importPackage);
        }
        for (RequireBundle requireBundle : ExtensionPointsReader.INSTANCE.getRequireBundles((NodeType) null)) {
            requireBundle.setDescription(Messages.ExDependenciesResolver_commonRequireBundle);
            requireBundles.add(requireBundle);
        }

        final Map<?, ?> additionProperties = item.getProperty().getAdditionalProperties().map();
        userBundleClasspaths = DependenciesCoreUtil.getStoredBundleClasspaths(additionProperties);

        handleAllNodeTypes(item.getProcess().getNode());
        handleAllConnectionTypes(item.getProcess().getConnection());

        final Collection<ImportPackage> customImportPackages = new ArrayList<>(importPackages);
        customImportPackages.addAll(DependenciesCoreUtil.getStoredImportPackages(additionProperties));
        importPackages = customImportPackages;

        final Collection<RequireBundle> customRequireBundles = new ArrayList<>(requireBundles);
        customRequireBundles.addAll(DependenciesCoreUtil.getStoredRequireBundles(additionProperties));
        requireBundles = customRequireBundles;

        String version = item.getProperty().getVersion();
        if (RelationshipItemBuilder.LATEST_VERSION.equals(version)) {
            try {
                version = ProxyRepositoryFactory.getInstance().getLastVersion(item.getProperty().getId()).getVersion();
            } catch (PersistenceException e) {
                ExceptionHandler.process(e);
            }
        }
        final ExportPackage exportPackage = new ExportPackage();
        exportPackage.setName(JavaResourcesHelper.getJobClassPackageName(item));
        exportPackage.setBuiltIn(true);
        exportPackage.setDescription(Messages.ExDependenciesResolver_generatedPackage);
        exportPackages.add(exportPackage);
        exportPackages.addAll(DependenciesCoreUtil.getStoredExportPackages(additionProperties));
    }

    public DependenciesResolver(final IProcess2 process) {
        for (ImportPackage importPackage : ExtensionPointsReader.INSTANCE.getImportPackages((INode) null)) {
            importPackage.setDescription(Messages.ExDependenciesResolver_commonImportPackage);
            importPackages.add(importPackage);
        }
        for (RequireBundle requireBundle : ExtensionPointsReader.INSTANCE.getRequireBundles((INode) null)) {
            requireBundle.setDescription(Messages.ExDependenciesResolver_commonRequireBundle);
            requireBundles.add(requireBundle);
        }

        final Map<?, ?> additionProperties = process.getAdditionalProperties();
        userBundleClasspaths = DependenciesCoreUtil.getStoredBundleClasspaths(additionProperties);

        handleAllNodes(process.getGraphicalNodes());

        Collection<ImportPackage> customImportPackages = new ArrayList<>(importPackages);
        customImportPackages.addAll(DependenciesCoreUtil.getStoredImportPackages(additionProperties));
        importPackages = customImportPackages;

        Collection<RequireBundle> customRequireBundles = new ArrayList<>(requireBundles);
        customRequireBundles.addAll(DependenciesCoreUtil.getStoredRequireBundles(additionProperties));
        requireBundles = customRequireBundles;

        String version = process.getVersion();
        if (RelationshipItemBuilder.LATEST_VERSION.equals(version)) {
            try {
                version = ProxyRepositoryFactory.getInstance().getLastVersion(process.getId()).getVersion();
            } catch (PersistenceException e) {
                ExceptionHandler.process(e);
            }
        }
        final ExportPackage exportPackage = new ExportPackage();
        exportPackage.setName(JavaResourcesHelper.getJobClassPackageName(process.getProperty().getItem()));
        exportPackage.setBuiltIn(true);
        exportPackage.setDescription(Messages.ExDependenciesResolver_generatedPackage);
        exportPackages.add(exportPackage);
        exportPackages.addAll(DependenciesCoreUtil.getStoredExportPackages(additionProperties));
    }

    private static boolean isActivate(final Collection<ElementParameterType> parameters) {
        for (ElementParameterType cpType : parameters) {
            if ("ACTIVATE".equals(cpType.getName())) { //$NON-NLS-1$
                return Boolean.parseBoolean(cpType.getValue());
            }
        }
        return true;
    }

    /**
     * most of the datas of a node are coming from extension point except the cTalendJob
     */
    private void handleAllNodeTypes(final Collection<NodeType> nodes) {
        for (NodeType n : nodes) {
            if (isActivate(n.getElementParameter())) {
                handleNode(n);
            }
        }
    }

    private void handleAllNodes(final Collection<? extends INode> nodes) {
        for (INode n : nodes) {
            if (n.isActivate()) {
                handleNode(n);
                handleAllConnections(n.getOutgoingConnections());
            }
        }
    }

    private static <T extends ManifestItem> T addItem(final Collection<T> items, final T newItem) {
        if (!items.add(newItem)) {
            for (T obj : items) {
                if (newItem.equals(obj)) {
                    return obj;
                }
            }
        }
        return newItem;
    }

    private void handleNode(final NodeType n) {
        final String uniqueName = ElementParameterParser.getUNIQUENAME(n);
        for (ImportPackage importPackage : ExtensionPointsReader.INSTANCE.getImportPackages(n)) {
            addItem(importPackages, importPackage).addRelativeComponent(uniqueName);
        }
        for (RequireBundle requireBundle : ExtensionPointsReader.INSTANCE.getRequireBundles(n)) {
            addItem(requireBundles, requireBundle).addRelativeComponent(uniqueName);
        }
        for (BundleClasspath bcp : ExtensionPointsReader.INSTANCE.getBundleClasspaths(n)) {
            bcp = addItem(bundleClasspaths, bcp);
            bcp.addRelativeComponent(uniqueName);
            if (!bcp.isBuiltIn()) {
                bcp.setOptional(!userBundleClasspaths.contains(bcp));
            }
        }
    }

    private void handleNode(final INode n) {
        final String uniqueName = n.getUniqueName();
        for (ImportPackage importPackage : ExtensionPointsReader.INSTANCE.getImportPackages(n)) {
            addItem(importPackages, importPackage).addRelativeComponent(uniqueName);
        }
        for (RequireBundle requireBundle : ExtensionPointsReader.INSTANCE.getRequireBundles(n)) {
            addItem(requireBundles, requireBundle).addRelativeComponent(uniqueName);
        }
        for (BundleClasspath bcp : ExtensionPointsReader.INSTANCE.getBundleClasspaths(n)) {
            bcp = addItem(bundleClasspaths, bcp);
            bcp.addRelativeComponent(uniqueName);
            if (!bcp.isBuiltIn()) {
                bcp.setOptional(!userBundleClasspaths.contains(bcp));
            }
        }
    }

    /**
     * special for ROUTE_WHEN connection case we need to handle it specially according the selected language
     */
    private void handleAllConnectionTypes(Collection<ConnectionType> connections) {
        for (ConnectionType connection : connections) {
            if (isActivate(connection.getElementParameter())
                    && EConnectionType.ROUTE_WHEN.getName().equals(connection.getConnectorName())) {
                final String languageName = handleROUTEWHENconnection(connection.getElementParameter());
                if (languageName != null) {
                    for (ImportPackage importPackage : ExtensionPointsReader.INSTANCE.getImportPackages(languageName)) {
                        addItem(importPackages, importPackage).addRelativeComponent(connection.getLabel());
                    }
                }
            }
        }
    }

    private void handleAllConnections(Collection<? extends IConnection> connections) {
        for (IConnection connection : connections) {
            if (connection.isActivate() && EConnectionType.ROUTE_WHEN == connection.getLineStyle()) {
                final IElementParameter ep = connection.getElementParameter(EParameterName.ROUTETYPE.getName());
                if (null != ep) {
                    for (ImportPackage importPackage : ExtensionPointsReader.INSTANCE
                            .getImportPackages(ep.getValue().toString())) {
                        addItem(importPackages, importPackage).addRelativeComponent(connection.getName());
                    }
                }
            }
        }
    }

    private static String handleROUTEWHENconnection(final Collection<ElementParameterType> connectionParameters) {
        for (ElementParameterType ept : connectionParameters) {
            if (EParameterName.ROUTETYPE.getName().equals(ept.getName())) {
                return ept.getValue();
            }
        }
        return null;
    }

    public Collection<BundleClasspath> getBundleClasspaths() {
        return bundleClasspaths;
    }

    public Collection<RequireBundle> getRequireBundles() {
        return requireBundles;
    }

    public Collection<ImportPackage> getImportPackages() {
        return importPackages;
    }

    public Collection<ExportPackage> getExportPackages() {
        return exportPackages;
    }

    public String getManifestBundleClasspath(char separator) {
        return DependenciesCoreUtil.toManifestString(bundleClasspaths, separator, true);
    }

    public String getManifestRequireBundle(char separator) {
        return DependenciesCoreUtil.toManifestString(requireBundles, separator, true);
    }

    public String getManifestImportPackage(char separator) {
        return DependenciesCoreUtil.toManifestString(importPackages, separator, true);
    }

    public String getManifestExportPackage(char separator) {
        return DependenciesCoreUtil.toManifestString(exportPackages, separator, true);
    }
}
