// ============================================================================
//
// Copyright (C) 2006-2021 Talend Inc. - www.talend.com
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

/**
 * DOC Administrator class global comment. Detailled comment
 */
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

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.repository.IRepositoryViewObject#getId()
     */
    public String getId() {
        return serviceOperation.getId();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.repository.IRepositoryViewObject#getLabel()
     */
    public String getLabel() {
        return serviceOperation.getLabel();
    }

    public String getName() {
        return serviceOperation.getName();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.repository.IRepositoryViewObject#getVersion()
     */
    public String getVersion() {
        return viewObject.getVersion();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.repository.IRepositoryViewObject#getAuthor()
     */
    public User getAuthor() {
        return viewObject.getAuthor();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.repository.IRepositoryViewObject#getStatusCode()
     */
    public String getStatusCode() {
        return viewObject.getStatusCode();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.repository.IRepositoryViewObject#getCreationDate()
     */
    public Date getCreationDate() {
        return viewObject.getCreationDate();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.repository.IRepositoryViewObject#getDescription()
     */
    public String getDescription() {
        return this.errorTooltip == null?viewObject.getDescription():this.errorTooltip;
    }

    public void setDescription(String description){
    	this.errorTooltip = description;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.repository.IRepositoryViewObject#getModificationDate()
     */
    public Date getModificationDate() {
        return viewObject.getModificationDate();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.repository.IRepositoryViewObject#getPurpose()
     */
    public String getPurpose() {
        return viewObject.getPurpose();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.repository.IRepositoryViewObject#getRepositoryObjectType()
     */
    public ERepositoryObjectType getRepositoryObjectType() {
        return ERepositoryObjectType.SERVICESOPERATION;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.repository.IRepositoryViewObject#getChildren()
     */
    public List<IRepositoryViewObject> getChildren() {
        return viewObject.getChildren();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.core.model.repository.IRepositoryViewObject#setRepositoryNode(org.talend.repository.model.IRepositoryNode
     * )
     */
    public void setRepositoryNode(IRepositoryNode node) {
        viewObject.setRepositoryNode(node);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.repository.IRepositoryViewObject#getRepositoryNode()
     */
    public IRepositoryNode getRepositoryNode() {
        return viewObject.getRepositoryNode();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.repository.IRepositoryViewObject#isDeleted()
     */
    public boolean isDeleted() {
        return viewObject.isDeleted();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.repository.IRepositoryViewObject#getProjectLabel()
     */
    public String getProjectLabel() {
        return viewObject.getProjectLabel();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.repository.IRepositoryViewObject#getPath()
     */
    public String getPath() {
        return viewObject.getPath();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.repository.IRepositoryViewObject#getRepositoryStatus()
     */
    public ERepositoryStatus getRepositoryStatus() {
        return viewObject.getRepositoryStatus();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.repository.IRepositoryViewObject#getInformationStatus()
     */
    public ERepositoryStatus getInformationStatus() {
        return this.infoStatus==null?viewObject.getInformationStatus():infoStatus;
    }


    public void setInformationStatus(ERepositoryStatus infoStatus) {
		this.infoStatus = infoStatus;
	}

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.repository.model.ISubRepositoryObject#removeFromParent()
     */
    public void removeFromParent() {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.repository.model.ISubRepositoryObject#getProperty()
     */
    public Property getProperty() {
        return viewObject.getProperty();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.repository.IRepositoryViewObject#isModified()
     */
    public boolean isModified() {
        return viewObject.isModified();
    }

}
