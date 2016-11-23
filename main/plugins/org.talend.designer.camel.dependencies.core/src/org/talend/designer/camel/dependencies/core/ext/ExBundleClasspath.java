package org.talend.designer.camel.dependencies.core.ext;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import org.eclipse.emf.common.util.EList;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.designer.camel.dependencies.core.model.BundleClasspath;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.ElementValueType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;

class ExBundleClasspath extends ExManifestItem<BundleClasspath> {

    private static final String SEPARATOR = ","; //$NON-NLS-1$

    private final String name;

    private final boolean isOptional;

    ExBundleClasspath(String name, boolean isOptional) {
        this.name = name;
        this.isOptional = isOptional;
    }

    @Override
    protected Collection<BundleClasspath> to(NodeType t) {
        final Collection<BundleClasspath> bundleClasspaths = new HashSet<>();
        for (Object e : t.getElementParameter()) {
            final ElementParameterType p = (ElementParameterType) e;
            if (name.equals(p.getName())) {
                final EList<?> elementValue = p.getElementValue();
                if (elementValue.isEmpty()) {
                    String evtValue = p.getValue();
                    if (evtValue != null) {
                        if (evtValue.startsWith("\"")) { //$NON-NLS-1$
                            evtValue = evtValue.substring(1);
                        }
                        if (evtValue.endsWith("\"")) { //$NON-NLS-1$
                            evtValue = evtValue.substring(0, evtValue.length() - 1);
                        }
                        if (!evtValue.isEmpty()) {
                            bundleClasspaths.add(createBundleClasspath(evtValue));
                        }
                    }
                } else {
                    for (Object pv : p.getElementValue()) {
                        ElementValueType evt = (ElementValueType) pv;
                        bundleClasspaths.add(createBundleClasspath(evt.getValue()));
                    }
                }
                return bundleClasspaths;
            }
        }
        // no param - libs itself
        for (String lib : name.split(SEPARATOR)) {
            bundleClasspaths.add(createBundleClasspath(lib));
        }
        return bundleClasspaths;
    }

    @Override
    protected Collection<BundleClasspath> to(INode node) {
        final Collection<BundleClasspath> bundleClasspaths = new HashSet<>();
        final IElementParameter ep = node.getElementParameter(name);
        if (null != ep) {
            final Object elementValue = ep.getValue();
            if (elementValue instanceof String) {
                String evtValue = (String) elementValue;
                if (evtValue != null) {
                    if (evtValue.startsWith("\"")) { //$NON-NLS-1$
                        evtValue = evtValue.substring(1);
                    }
                    if (evtValue.endsWith("\"")) { //$NON-NLS-1$
                        evtValue = evtValue.substring(0, evtValue.length() - 1);
                    }
                    if (!evtValue.isEmpty()) {
                        bundleClasspaths.add(createBundleClasspath(evtValue));
                    }
                }
            } else {
                for (Map<String, String> ev : (Collection<Map<String, String>>) ep.getValue()) {
                    bundleClasspaths.add(createBundleClasspath(ev.values().iterator().next()));
                }
            }
            return bundleClasspaths;
        }
        // no param - libs itself
        for (String lib : name.split(SEPARATOR)) {
            bundleClasspaths.add(createBundleClasspath(lib));
        }
        return bundleClasspaths;
    }

    private BundleClasspath createBundleClasspath(String lib) {
        final BundleClasspath bundleClasspath = new BundleClasspath();
        bundleClasspath.setBuiltIn(!isOptional);
        bundleClasspath.setName(lib);
        return bundleClasspath;
    }

}
