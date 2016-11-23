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
import org.talend.repository.services.model.services.ServiceOperation;

public class OperationRepositoryObject implements IRepositoryViewObject {

    protected final IRepositoryViewObject viewObject;

    protected ServiceOperation serviceOperation;

    protected ERepositoryStatus infoStatus;

    protected String errorTooltip;

    public IRepositoryViewObject getViewObject() {
        return this.viewObject;
    }

    public OperationRepositoryObject(IRepositoryViewObject repObj, ServiceOperation serviceOperation) {
        this.viewObject = repObj;
        this.serviceOperation = serviceOperation;
    }

    @Override
    public String getId() {
        return serviceOperation.getId();
    }

    @Override
    public String getLabel() {
        return serviceOperation.getLabel();
    }

    public String getName() {
        return serviceOperation.getName();
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
        return this.errorTooltip == null ? viewObject.getDescription() : this.errorTooltip;
    }

    public void setDescription(String description) {
        this.errorTooltip = description;
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
        return ERepositoryObjectType.SERVICESOPERATION;
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
        return this.infoStatus == null ? viewObject.getInformationStatus() : infoStatus;
    }

    public void setInformationStatus(ERepositoryStatus infoStatus) {
        this.infoStatus = infoStatus;
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
