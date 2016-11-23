package org.talend.designer.esb.webservice.ws.wsdlinfo;

import javax.xml.namespace.QName;

public class ParameterInfo {

    public static final QName MULTIPART = new QName("*multipart*");

    private QName name;

    public String getDisplayName() {
        if (null != name) {
            return name.getLocalPart();
        }
        return ""; //$NON-NLS-1$
    }

    public void setName(QName name) {
        this.name = name;
    }

}
