package org.talend.designer.camel.dependencies.core.ext;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.designer.camel.dependencies.core.model.ManifestItem;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.ElementValueType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;

abstract class ExManifestItem<T extends ManifestItem> {

    private static final String SEPARATOR = ":"; //$NON-NLS-1$

    private final Map<String, String> predicates = new HashMap<>();

    void addPredicate(String name, String value) {
        predicates.put(name, value);
    }

    private boolean satisfy(final NodeType nodeType) {
        if (predicates.isEmpty()) {
            return true;
        }
        final Collection<?> parameters = nodeType.getElementParameter();
        for (Map.Entry<String, String> predicate : predicates.entrySet()) {
            String attributeName = predicate.getKey();
            final String attributeValue = predicate.getValue();

            final String[] segments = attributeName.split(SEPARATOR);
            String valueName = null;
            if (segments.length > 1) {
                attributeName = segments[0];
                valueName = segments[1];
            }
            for (Object o : parameters) {
                final ElementParameterType ept = (ElementParameterType) o;
                if (ept.getName().equals(attributeName)) {
                    if (null == valueName) {
                        if (!attributeValue.equals(ept.getValue())) {
                            return false;
                        }
                    } else {
                        boolean found = false;
                        for (Object e : ept.getElementValue()) {
                            final ElementValueType evt = (ElementValueType) e;
                            if (valueName.equals(evt.getElementRef()) && attributeValue.equals(evt.getValue())) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            return false;
                        }
                    }
                    break;
                }
            }
        }
        return true;
    }

    private boolean satisfy(final INode node) {
        if (predicates.isEmpty()) {
            return true;
        }
        for (Map.Entry<String, String> predicate : predicates.entrySet()) {
            String attributeName = predicate.getKey();
            final String attributeValue = predicate.getValue();

            final String[] segments = attributeName.split(SEPARATOR);
            String valueName = null;
            if (segments.length > 1) {
                attributeName = segments[0];
                valueName = segments[1];
            }
            final IElementParameter ep = node.getElementParameter(attributeName);
            if (null == valueName) {
                if (!attributeValue.equals(ep.getValue().toString())) {
                    return false;
                }
            } else {
                boolean found = false;
                for (Map<String, String> ev : (Collection<Map<String, String>>) ep.getValue()) {
                    if (attributeValue.equals(ev.get(valueName))) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    return false;
                }
            }
        }
        return true;
    }

    public Collection<T> toTargets(final NodeType nodeType) {
        if (satisfy(nodeType)) {
            return to(nodeType);
        }
        return Collections.emptyList();
    }

    public Collection<T> toTargets(final INode node) {
        if (satisfy(node)) {
            return to(node);
        }
        return Collections.emptyList();
    }

    protected abstract Collection<T> to(final NodeType nodeType);

    protected abstract Collection<T> to(final INode node);

}
