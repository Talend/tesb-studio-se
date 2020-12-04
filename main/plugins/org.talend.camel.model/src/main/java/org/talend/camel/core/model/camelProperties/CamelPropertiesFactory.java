/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package org.talend.camel.core.model.camelProperties;

import org.eclipse.emf.ecore.EFactory;

/**
 * <!-- begin-user-doc --> The <b>Factory</b> for the model. It provides a
 * create method for each non-abstract class of the model. <!-- end-user-doc -->
 * @see org.talend.camel.core.model.camelProperties.CamelPropertiesPackage
 * @generated
 */
public interface CamelPropertiesFactory extends EFactory {
    /**
     * The singleton instance of the factory.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    CamelPropertiesFactory eINSTANCE = org.talend.camel.core.model.camelProperties.impl.CamelPropertiesFactoryImpl.init();

    /**
     * Returns a new object of class '<em>Bean Item</em>'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return a new object of class '<em>Bean Item</em>'.
     * @generated
     */
    BeanItem createBeanItem();

    /**
     * Returns a new object of class '<em>Beans Jar Item</em>'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return a new object of class '<em>Beans Jar Item</em>'.
     * @generated
     */
    BeansJarItem createBeansJarItem();

    /**
     * Returns a new object of class '<em>Camel Process Item</em>'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return a new object of class '<em>Camel Process Item</em>'.
     * @generated
     */
    CamelProcessItem createCamelProcessItem();

    /**
     * Returns a new object of class '<em>Route Resource Item</em>'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return a new object of class '<em>Route Resource Item</em>'.
     * @generated
     */
    RouteResourceItem createRouteResourceItem();

    /**
     * Returns a new object of class '<em>Route Document Item</em>'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return a new object of class '<em>Route Document Item</em>'.
     * @generated
     */
	RouteDocumentItem createRouteDocumentItem();

				/**
     * Returns a new object of class '<em>Routelet Process Item</em>'.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @return a new object of class '<em>Routelet Process Item</em>'.
     * @generated
     */
	RouteletProcessItem createRouteletProcessItem();

				/**
     * Returns the package supported by this factory.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the package supported by this factory.
     * @generated
     */
    CamelPropertiesPackage getCamelPropertiesPackage();

} //CamelPropertiesFactory
