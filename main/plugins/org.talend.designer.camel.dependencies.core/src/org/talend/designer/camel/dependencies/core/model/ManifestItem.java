package org.talend.designer.camel.dependencies.core.model;

import java.util.Collection;
import java.util.HashSet;

import org.osgi.framework.Constants;

public abstract class ManifestItem {

    public static final String IMPORT_PACKAGE = Constants.IMPORT_PACKAGE;

    public static final String REQUIRE_BUNDLE = Constants.REQUIRE_BUNDLE;

    public static final String EXPORT_PACKAGE = Constants.EXPORT_PACKAGE;

    public static final String BUNDLE_CLASSPATH = Constants.BUNDLE_CLASSPATH;

    public static final String RESOLUTION_DIRECTIVE_OPTIONAL = Constants.RESOLUTION_DIRECTIVE + ":=" //$NON-NLS-1$
            + Constants.RESOLUTION_OPTIONAL;

    private static final char ATTRIBUTE_SEPARATOR = ';';

    private String name;

    private String version;

    private boolean isOptional;

    private boolean isBuiltIn;

    private String description;

    private Collection<String> relativeComponents = new HashSet<>();

    public static ManifestItem newItem(String header) {
        switch (header) {
        case IMPORT_PACKAGE:
            return new ImportPackage();
        case REQUIRE_BUNDLE:
            return new RequireBundle();
        case EXPORT_PACKAGE:
            return new ExportPackage();
        case BUNDLE_CLASSPATH:
            return new BundleClasspath();
        default:
            return null;
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public boolean isOptional() {
        return isOptional;
    }

    public void setOptional(boolean isOptional) {
        this.isOptional = isOptional;
    }

    public void setBuiltIn(boolean isBuiltIn) {
        this.isBuiltIn = isBuiltIn;
    }

    public boolean isBuiltIn() {
        return isBuiltIn;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void addRelativeComponent(String componentUniqueName) {
        relativeComponents.add(componentUniqueName);
    }

    public String getDescription() {
        if (relativeComponents.isEmpty() && description == null) {
            return ""; //$NON-NLS-1$
        } else if (relativeComponents.isEmpty()) {
            return description;
        } else {
            return "Relative Components: " + relativeComponents.toString();
        }
    }

    public String toManifestString() {
        StringBuilder sb = new StringBuilder(name);
        if (version != null) {
            sb.append(ATTRIBUTE_SEPARATOR);
            sb.append(getVersionAttribute());
            sb.append("=\""); //$NON-NLS-1$
            sb.append(version);
            sb.append('"');
        }
        if (isOptional) {
            sb.append(ATTRIBUTE_SEPARATOR).append(RESOLUTION_DIRECTIVE_OPTIONAL);
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        String label = name;
        if (null != version) {
            label += ' ' + (Character.isDigit(version.charAt(0)) ? '(' + version + ')' : version);
        }
        return label;
    }

    /**
     * only care about the name, ignore others
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj != null && getClass() == obj.getClass()) {
            return name != null && name.equals(((ManifestItem) obj).getName());
        }
        return false;
    }

    /**
     * only care about name
     */
    @Override
    public int hashCode() {
        return name == null ? 0 : name.hashCode();
    }

    abstract public String getVersionAttribute();

    abstract public String getHeader();

}
