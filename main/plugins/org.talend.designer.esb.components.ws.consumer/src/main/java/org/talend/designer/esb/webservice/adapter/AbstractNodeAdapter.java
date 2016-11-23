package org.talend.designer.esb.webservice.adapter;

import org.eclipse.core.runtime.IStatus;
import org.talend.designer.esb.webservice.ServiceSetting;
import org.talend.designer.esb.webservice.WebServiceNode;
import org.talend.designer.esb.webservice.ws.wsdlinfo.Function;

public abstract class AbstractNodeAdapter extends BaseNodeAdapter {

    private static enum SupportedComponent {
                                            tESBConsumer {

                                                @Override
                                                public AbstractNodeAdapter getParametersSetter(WebServiceNode wrappedNode) {
                                                    return new TESBConsumerNodeAdapter(wrappedNode);
                                                }
                                            },

                                            cCXF {

                                                @Override
                                                public AbstractNodeAdapter getParametersSetter(WebServiceNode wrappedNode) {
                                                    return new CCXFNodeAdapter(wrappedNode);
                                                }
                                            };

        public abstract AbstractNodeAdapter getParametersSetter(WebServiceNode wrappedNode);
    }

    AbstractNodeAdapter(WebServiceNode node) {
        super(node);
    }

    public static AbstractNodeAdapter getAdapter(WebServiceNode wrappedNode) {
        String componentName = wrappedNode.getComponent().getName();
        SupportedComponent comp = null;
        try {
            comp = SupportedComponent.valueOf(componentName);
            return comp.getParametersSetter(wrappedNode);
        } catch (Exception e) {
            throw new IllegalArgumentException("Cannot get NodeAdapter for unsupported component : " + componentName, e);
        }
    }

    /**
     * Sets the parameters for current node. Please use {@link #setParameter(String key, Object value)}.
     * 
     * @return the status
     */
    public abstract IStatus setNodeSetting(ServiceSetting setting);

    public abstract Function loadCurrentFunction();

    public abstract String getInitialWsdlLocation();

    /**
     * Specify is function required, for some component, may no need to specify a funcion.
     */
    public boolean isServiceOperationRequired() {
        return true;
    }

    public abstract boolean allowPopulateSchema();

    public boolean routeResourcesAvailable() {
        return false;
    }
}
