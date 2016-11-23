// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.repository.services.utils;

import java.util.Date;
import java.util.List;

import org.talend.commons.runtime.model.repository.ERepositoryStatus;
import org.talend.core.model.properties.Property;
import org.talend.core.model.properties.User;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.services.model.services.ServicePort;

public class PortRepositoryObject implements IRepositoryViewObject {

    protected final IRepositoryViewObject viewObject;

    protected ServicePort servicePort;

    public IRepositoryViewObject getViewObject() {
        return this.viewObject;
    }

    public PortRepositoryObject(IRepositoryViewObject repObj, ServicePort servicePort) {
        this.viewObject = repObj;
        this.servicePort = servicePort;
    }

    @Override
    public String getId() {
        return servicePort.getId();
    }

    @Override
    public String getLabel() {
        return servicePort.getName();
    }

    @Override
    public String getVersion() {
        return viewObject.getVersion();
    }

    @Override
    public User getAuthor() {
        return viewObject.getAuthor();
    }

    @Override
    public String getStatusCode() {
        return viewObject.getStatusCode();
    }

    @Override
    public Date getCreationDate() {
        return viewObject.getCreationDate();
    }

    @Override
    public String getDescription() {
        return viewObject.getDescription();
    }

    @Override
    public Date getModificationDate() {
        return viewObject.getModificationDate();
    }

    @Override
    public String getPurpose() {
        return viewObject.getPurpose();
    }

    @Override
    public ERepositoryObjectType getRepositoryObjectType() {
        return ERepositoryObjectType.SERVICESPORT;
    }

    @Override
    public List<IRepositoryViewObject> getChildren() {
        return viewObject.getChildren();
    }

    @Override
    public void setRepositoryNode(IRepositoryNode node) {
        viewObject.setRepositoryNode(node);
    }

    @Override
    public IRepositoryNode getRepositoryNode() {
        return viewObject.getRepositoryNode();
    }

    @Override
    public boolean isDeleted() {
        return viewObject.isDeleted();
    }

    @Override
    public String getProjectLabel() {
        return viewObject.getProjectLabel();
    }

    @Override
    public String getPath() {
        return viewObject.getPath();
    }

    @Override
    public ERepositoryStatus getRepositoryStatus() {
        return viewObject.getRepositoryStatus();
    }

    @Override
    public ERepositoryStatus getInformationStatus() {
        return viewObject.getInformationStatus();
    }

    public void removeFromParent() {
        // TODO Auto-generated method stub
    }

    @Override
    public Property getProperty() {
        return viewObject.getProperty();
    }

    @Override
    public boolean isModified() {
        return viewObject.isModified();
    }

}
