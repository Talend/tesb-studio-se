/**
 * <copyright> </copyright>
 * 
 * $Id$
 */
package org.talend.repository.services.model.services.impl;

import java.util.Collection;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.EMap;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.util.EObjectResolvingEList;
import org.eclipse.emf.ecore.util.EcoreEMap;
import org.eclipse.emf.ecore.util.InternalEList;
import org.talend.core.model.metadata.builder.connection.impl.AbstractMetadataObjectImpl;
import org.talend.repository.services.model.services.ServiceOperation;
import org.talend.repository.services.model.services.ServicePort;
import org.talend.repository.services.model.services.ServicesPackage;

/**
 * <!-- begin-user-doc --> An implementation of the model object '<em><b>Service Port</b></em>'. <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.talend.repository.services.model.services.impl.ServicePortImpl#getServiceOperation <em>Service Operation</em>}</li>
 *   <li>{@link org.talend.repository.services.model.services.impl.ServicePortImpl#getAdditionalInfo <em>Additional Info</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class ServicePortImpl extends AbstractMetadataObjectImpl implements ServicePort {

    /**
     * The cached value of the '{@link #getServiceOperation() <em>Service Operation</em>}' reference list. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @see #getServiceOperation()
     * @generated
     * @ordered
     */
    protected EList<ServiceOperation> serviceOperation;

    /**
     * The cached value of the '{@link #getAdditionalInfo() <em>Additional Info</em>}' map.
     * <!-- begin-user-doc --> <!--
     * end-user-doc -->
     * @see #getAdditionalInfo()
     * @generated
     * @ordered
     */
    protected EMap<String, String> additionalInfo;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    protected ServicePortImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    @Override
    protected EClass eStaticClass() {
        return ServicesPackage.Literals.SERVICE_PORT;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    public EList<ServiceOperation> getServiceOperation() {
        if (serviceOperation == null) {
            serviceOperation = new EObjectResolvingEList<ServiceOperation>(ServiceOperation.class, this, ServicesPackage.SERVICE_PORT__SERVICE_OPERATION);
        }
        return serviceOperation;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    public EMap<String, String> getAdditionalInfo() {
        if (additionalInfo == null) {
            additionalInfo = new EcoreEMap<String,String>(ServicesPackage.Literals.ADDITIONAL_INFO_MAP, AdditionalInfoMapImpl.class, this, ServicesPackage.SERVICE_PORT__ADDITIONAL_INFO);
        }
        return additionalInfo;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    @Override
    public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
        switch (featureID) {
            case ServicesPackage.SERVICE_PORT__ADDITIONAL_INFO:
                return ((InternalEList<?>)getAdditionalInfo()).basicRemove(otherEnd, msgs);
        }
        return super.eInverseRemove(otherEnd, featureID, msgs);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    @Override
    public Object eGet(int featureID, boolean resolve, boolean coreType) {
        switch (featureID) {
            case ServicesPackage.SERVICE_PORT__SERVICE_OPERATION:
                return getServiceOperation();
            case ServicesPackage.SERVICE_PORT__ADDITIONAL_INFO:
                if (coreType) return getAdditionalInfo();
                else return getAdditionalInfo().map();
        }
        return super.eGet(featureID, resolve, coreType);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    @SuppressWarnings("unchecked")
    @Override
    public void eSet(int featureID, Object newValue) {
        switch (featureID) {
            case ServicesPackage.SERVICE_PORT__SERVICE_OPERATION:
                getServiceOperation().clear();
                getServiceOperation().addAll((Collection<? extends ServiceOperation>)newValue);
                return;
            case ServicesPackage.SERVICE_PORT__ADDITIONAL_INFO:
                ((EStructuralFeature.Setting)getAdditionalInfo()).set(newValue);
                return;
        }
        super.eSet(featureID, newValue);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    @Override
    public void eUnset(int featureID) {
        switch (featureID) {
            case ServicesPackage.SERVICE_PORT__SERVICE_OPERATION:
                getServiceOperation().clear();
                return;
            case ServicesPackage.SERVICE_PORT__ADDITIONAL_INFO:
                getAdditionalInfo().clear();
                return;
        }
        super.eUnset(featureID);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    @Override
    public boolean eIsSet(int featureID) {
        switch (featureID) {
            case ServicesPackage.SERVICE_PORT__SERVICE_OPERATION:
                return serviceOperation != null && !serviceOperation.isEmpty();
            case ServicesPackage.SERVICE_PORT__ADDITIONAL_INFO:
                return additionalInfo != null && !additionalInfo.isEmpty();
        }
        return super.eIsSet(featureID);
    }

    public boolean isReadOnly() {
        return false;
    }

} // ServicePortImpl
