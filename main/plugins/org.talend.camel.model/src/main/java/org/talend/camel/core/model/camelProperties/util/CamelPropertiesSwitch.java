/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package org.talend.camel.core.model.camelProperties.util;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.util.Switch;
import org.talend.camel.core.model.camelProperties.*;
import org.talend.camel.core.model.camelProperties.BeanItem;
import org.talend.camel.core.model.camelProperties.CamelProcessItem;
import org.talend.camel.core.model.camelProperties.CamelPropertiesPackage;
import org.talend.camel.core.model.camelProperties.RouteDocumentItem;
import org.talend.camel.core.model.camelProperties.RouteResourceItem;
import org.talend.camel.core.model.camelProperties.RouteletProcessItem;
import org.talend.core.model.properties.DocumentationItem;
import org.talend.core.model.properties.FileItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.RoutineItem;
import org.talend.core.model.properties.RoutinesJarItem;

/**
 * <!-- begin-user-doc --> The <b>Switch</b> for the model's inheritance
 * hierarchy. It supports the call {@link #doSwitch(EObject) doSwitch(object)}
 * to invoke the <code>caseXXX</code> method for each class of the model,
 * starting with the actual class of the object and proceeding up the
 * inheritance hierarchy until a non-null result is returned, which is the
 * result of the switch. <!-- end-user-doc -->
 * @see org.talend.camel.core.model.camelProperties.CamelPropertiesPackage
 * @generated
 */
public class CamelPropertiesSwitch<T> extends Switch<T>{
    /**
     * The cached model package
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected static CamelPropertiesPackage modelPackage;

    /**
     * Creates an instance of the switch.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public CamelPropertiesSwitch() {
        if (modelPackage == null) {
            modelPackage = CamelPropertiesPackage.eINSTANCE;
        }
    }

    /**
     * Checks whether this is a switch for the given package.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @param ePackage the package in question.
     * @return whether this is a switch for the given package.
     * @generated
     */
	@Override
	protected boolean isSwitchFor(EPackage ePackage) {
        return ePackage == modelPackage;
    }

    public T doSwitch(EObject theEObject) {
        return doSwitch(theEObject.eClass().getClassifierID(), theEObject);
    }
    
	/**
     * Calls <code>caseXXX</code> for each class of the model until one returns a non null result; it yields that result.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the first non-null result returned by a <code>caseXXX</code> call.
     * @generated
     */
	@Override
	protected T doSwitch(int classifierID, EObject theEObject) {
        switch (classifierID) {
            case CamelPropertiesPackage.BEAN_ITEM: {
                BeanItem beanItem = (BeanItem)theEObject;
                T result = caseBeanItem(beanItem);
                if (result == null) result = caseRoutineItem(beanItem);
                if (result == null) result = caseFileItem(beanItem);
                if (result == null) result = caseItem(beanItem);
                if (result == null) result = defaultCase(theEObject);
                return result;
            }
            case CamelPropertiesPackage.BEANS_JAR_ITEM: {
                BeansJarItem beansJarItem = (BeansJarItem)theEObject;
                T result = caseBeansJarItem(beansJarItem);
                if (result == null) result = caseRoutinesJarItem(beansJarItem);
                if (result == null) result = caseItem(beansJarItem);
                if (result == null) result = defaultCase(theEObject);
                return result;
            }
            case CamelPropertiesPackage.CAMEL_PROCESS_ITEM: {
                CamelProcessItem camelProcessItem = (CamelProcessItem)theEObject;
                T result = caseCamelProcessItem(camelProcessItem);
                if (result == null) result = caseProcessItem(camelProcessItem);
                if (result == null) result = caseItem(camelProcessItem);
                if (result == null) result = defaultCase(theEObject);
                return result;
            }
            case CamelPropertiesPackage.ROUTE_RESOURCE_ITEM: {
                RouteResourceItem routeResourceItem = (RouteResourceItem)theEObject;
                T result = caseRouteResourceItem(routeResourceItem);
                if (result == null) result = caseFileItem(routeResourceItem);
                if (result == null) result = caseItem(routeResourceItem);
                if (result == null) result = defaultCase(theEObject);
                return result;
            }
            case CamelPropertiesPackage.ROUTE_DOCUMENT_ITEM: {
                RouteDocumentItem routeDocumentItem = (RouteDocumentItem)theEObject;
                T result = caseRouteDocumentItem(routeDocumentItem);
                if (result == null) result = caseDocumentationItem(routeDocumentItem);
                if (result == null) result = caseFileItem(routeDocumentItem);
                if (result == null) result = caseItem(routeDocumentItem);
                if (result == null) result = defaultCase(theEObject);
                return result;
            }
            case CamelPropertiesPackage.ROUTELET_PROCESS_ITEM: {
                RouteletProcessItem routeletProcessItem = (RouteletProcessItem)theEObject;
                T result = caseRouteletProcessItem(routeletProcessItem);
                if (result == null) result = caseProcessItem(routeletProcessItem);
                if (result == null) result = caseItem(routeletProcessItem);
                if (result == null) result = defaultCase(theEObject);
                return result;
            }
            default: return defaultCase(theEObject);
        }
    }

    /**
     * Returns the result of interpreting the object as an instance of '<em>Bean Item</em>'.
     * <!-- begin-user-doc -->
     * This implementation returns null;
     * returning a non-null result will terminate the switch.
     * <!-- end-user-doc -->
     * @param object the target of the switch.
     * @return the result of interpreting the object as an instance of '<em>Bean Item</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
     */
    public T caseBeanItem(BeanItem object) {
        return null;
    }

    /**
     * Returns the result of interpreting the object as an instance of '<em>Beans Jar Item</em>'.
     * <!-- begin-user-doc -->
     * This implementation returns null;
     * returning a non-null result will terminate the switch.
     * <!-- end-user-doc -->
     * @param object the target of the switch.
     * @return the result of interpreting the object as an instance of '<em>Beans Jar Item</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
     */
    public T caseBeansJarItem(BeansJarItem object) {
        return null;
    }

    /**
     * Returns the result of interpreting the object as an instance of '<em>Camel Process Item</em>'.
     * <!-- begin-user-doc -->
     * This implementation returns null;
     * returning a non-null result will terminate the switch.
     * <!-- end-user-doc -->
     * @param object the target of the switch.
     * @return the result of interpreting the object as an instance of '<em>Camel Process Item</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
     */
    public T caseCamelProcessItem(CamelProcessItem object) {
        return null;
    }

    /**
     * Returns the result of interpreting the object as an instance of '<em>Route Resource Item</em>'.
     * <!-- begin-user-doc -->
     * This implementation returns null;
     * returning a non-null result will terminate the switch.
     * <!-- end-user-doc -->
     * @param object the target of the switch.
     * @return the result of interpreting the object as an instance of '<em>Route Resource Item</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
     */
    public T caseRouteResourceItem(RouteResourceItem object) {
        return null;
    }

    /**
     * Returns the result of interpreting the object as an instance of '<em>Route Document Item</em>'.
     * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
     * @param object the target of the switch.
     * @return the result of interpreting the object as an instance of '<em>Route Document Item</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
     */
	public T caseRouteDocumentItem(RouteDocumentItem object) {
        return null;
    }

				/**
     * Returns the result of interpreting the object as an instance of '<em>Routelet Process Item</em>'.
     * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
     * @param object the target of the switch.
     * @return the result of interpreting the object as an instance of '<em>Routelet Process Item</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
     */
	public T caseRouteletProcessItem(RouteletProcessItem object) {
        return null;
    }

				/**
     * Returns the result of interpreting the object as an instance of '<em>Item</em>'.
     * <!-- begin-user-doc -->
     * This implementation returns null;
     * returning a non-null result will terminate the switch.
     * <!-- end-user-doc -->
     * @param object the target of the switch.
     * @return the result of interpreting the object as an instance of '<em>Item</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
     */
    public T caseItem(Item object) {
        return null;
    }

    /**
     * Returns the result of interpreting the object as an instance of '<em>File Item</em>'.
     * <!-- begin-user-doc -->
     * This implementation returns null;
     * returning a non-null result will terminate the switch.
     * <!-- end-user-doc -->
     * @param object the target of the switch.
     * @return the result of interpreting the object as an instance of '<em>File Item</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
     */
    public T caseFileItem(FileItem object) {
        return null;
    }

    /**
     * Returns the result of interpreting the object as an instance of '<em>Routine Item</em>'.
     * <!-- begin-user-doc -->
     * This implementation returns null;
     * returning a non-null result will terminate the switch.
     * <!-- end-user-doc -->
     * @param object the target of the switch.
     * @return the result of interpreting the object as an instance of '<em>Routine Item</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
     */
    public T caseRoutineItem(RoutineItem object) {
        return null;
    }

    /**
     * Returns the result of interpreting the object as an instance of '<em>Routines Jar Item</em>'.
     * <!-- begin-user-doc -->
     * This implementation returns null;
     * returning a non-null result will terminate the switch.
     * <!-- end-user-doc -->
     * @param object the target of the switch.
     * @return the result of interpreting the object as an instance of '<em>Routines Jar Item</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
     */
    public T caseRoutinesJarItem(RoutinesJarItem object) {
        return null;
    }

    /**
     * Returns the result of interpreting the object as an instance of '<em>Process Item</em>'.
     * <!-- begin-user-doc -->
     * This implementation returns null;
     * returning a non-null result will terminate the switch.
     * <!-- end-user-doc -->
     * @param object the target of the switch.
     * @return the result of interpreting the object as an instance of '<em>Process Item</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
     */
    public T caseProcessItem(ProcessItem object) {
        return null;
    }

    /**
     * Returns the result of interpreting the object as an instance of '<em>Documentation Item</em>'.
     * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
     * @param object the target of the switch.
     * @return the result of interpreting the object as an instance of '<em>Documentation Item</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
     */
	public T caseDocumentationItem(DocumentationItem object) {
        return null;
    }

				/**
     * Returns the result of interpreting the object as an instance of '<em>EObject</em>'.
     * <!-- begin-user-doc -->
     * This implementation returns null;
     * returning a non-null result will terminate the switch, but this is the last case anyway.
     * <!-- end-user-doc -->
     * @param object the target of the switch.
     * @return the result of interpreting the object as an instance of '<em>EObject</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject)
     * @generated
     */
	@Override
	public T defaultCase(EObject object) {
        return null;
    }

} //CamelPropertiesSwitch
