/**
 * <copyright> </copyright>
 * 
 * $Id$
 */
package org.talend.camel.core.model.camelProperties.impl;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.impl.EPackageImpl;
import org.eclipse.emf.ecore.xml.type.XMLTypePackage;
import org.eclipse.gmf.runtime.notation.NotationPackage;
import org.talend.camel.core.model.camelProperties.BeanItem;
import org.talend.camel.core.model.camelProperties.BeansJarItem;
import org.talend.camel.core.model.camelProperties.CamelProcessItem;
import org.talend.camel.core.model.camelProperties.CamelPropertiesFactory;
import org.talend.camel.core.model.camelProperties.CamelPropertiesPackage;
import org.talend.camel.core.model.camelProperties.RouteDocumentItem;
import org.talend.camel.core.model.camelProperties.RouteResourceItem;
import org.talend.camel.core.model.camelProperties.RouteletProcessItem;
import org.talend.core.model.metadata.builder.connection.ConnectionPackage;
import org.talend.core.model.properties.PropertiesPackage;
import org.talend.designer.business.model.business.BusinessPackage;
import org.talend.designer.core.model.utils.emf.component.ComponentPackage;
import org.talend.designer.core.model.utils.emf.talendfile.TalendFilePackage;
import org.talend.designer.joblet.model.JobletPackage;
import orgomg.cwm.analysis.businessnomenclature.BusinessnomenclaturePackage;
import orgomg.cwm.analysis.datamining.DataminingPackage;
import orgomg.cwm.analysis.informationvisualization.InformationvisualizationPackage;
import orgomg.cwm.analysis.olap.OlapPackage;
import orgomg.cwm.analysis.transformation.TransformationPackage;
import orgomg.cwm.foundation.businessinformation.BusinessinformationPackage;
import orgomg.cwm.foundation.datatypes.DatatypesPackage;
import orgomg.cwm.foundation.expressions.ExpressionsPackage;
import orgomg.cwm.foundation.keysindexes.KeysindexesPackage;
import orgomg.cwm.foundation.softwaredeployment.SoftwaredeploymentPackage;
import orgomg.cwm.foundation.typemapping.TypemappingPackage;
import orgomg.cwm.management.warehouseoperation.WarehouseoperationPackage;
import orgomg.cwm.management.warehouseprocess.WarehouseprocessPackage;
import orgomg.cwm.objectmodel.behavioral.BehavioralPackage;
import orgomg.cwm.objectmodel.core.CorePackage;
import orgomg.cwm.objectmodel.instance.InstancePackage;
import orgomg.cwm.objectmodel.relationships.RelationshipsPackage;
import orgomg.cwm.resource.multidimensional.MultidimensionalPackage;
import orgomg.cwm.resource.record.RecordPackage;
import orgomg.cwm.resource.relational.RelationalPackage;
import orgomg.cwm.resource.xml.XmlPackage;
import orgomg.cwmmip.CwmmipPackage;
import orgomg.cwmx.analysis.informationreporting.InformationreportingPackage;
import orgomg.cwmx.analysis.informationset.InformationsetPackage;
import orgomg.cwmx.foundation.er.ErPackage;
import orgomg.cwmx.resource.coboldata.CoboldataPackage;
import orgomg.cwmx.resource.dmsii.DmsiiPackage;
import orgomg.cwmx.resource.essbase.EssbasePackage;
import orgomg.cwmx.resource.express.ExpressPackage;
import orgomg.cwmx.resource.imsdatabase.ImsdatabasePackage;
import orgomg.mof.model.ModelPackage;

/**
 * <!-- begin-user-doc --> An implementation of the model <b>Package</b>. <!-- end-user-doc -->
 * @generated
 */
public class CamelPropertiesPackageImpl extends EPackageImpl implements CamelPropertiesPackage {

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    private EClass beanItemEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass beansJarItemEClass = null;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    private EClass camelProcessItemEClass = null;

	/**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
	private EClass routeResourceItemEClass = null;

    /**
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	private EClass routeDocumentItemEClass = null;

				/**
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	private EClass routeletProcessItemEClass = null;

				/**
     * Creates an instance of the model <b>Package</b>, registered with {@link org.eclipse.emf.ecore.EPackage.Registry
     * EPackage.Registry} by the package package URI value.
     * <p>
     * Note: the correct way to create the package is via the static factory method {@link #init init()}, which also
     * performs initialization of the package, or returns the registered package, if one already exists. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @see org.eclipse.emf.ecore.EPackage.Registry
     * @see org.talend.camel.core.model.camelProperties.CamelPropertiesPackage#eNS_URI
     * @see #init()
     * @generated
     */
    private CamelPropertiesPackageImpl() {
        super(eNS_URI, CamelPropertiesFactory.eINSTANCE);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    private static boolean isInited = false;

    /**
     * Creates, registers, and initializes the <b>Package</b> for this model, and for any others upon which it depends.
     * 
     * <p>
     * This method is used to initialize {@link CamelPropertiesPackage#eINSTANCE} when that field is accessed. Clients
     * should not invoke it directly. Instead, they should simply access that field to obtain the package. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @see #eNS_URI
     * @see #createPackageContents()
     * @see #initializePackageContents()
     * @generated
     */
    public static CamelPropertiesPackage init() {
        if (isInited) return (CamelPropertiesPackage)EPackage.Registry.INSTANCE.getEPackage(CamelPropertiesPackage.eNS_URI);

        // Obtain or create and register package
        Object registeredCamelPropertiesPackage = EPackage.Registry.INSTANCE.get(eNS_URI);
        CamelPropertiesPackageImpl theCamelPropertiesPackage = registeredCamelPropertiesPackage instanceof CamelPropertiesPackageImpl ? (CamelPropertiesPackageImpl)registeredCamelPropertiesPackage : new CamelPropertiesPackageImpl();

        isInited = true;

        // Initialize simple dependencies
        BusinessPackage.eINSTANCE.eClass();
        ComponentPackage.eINSTANCE.eClass();
        CorePackage.eINSTANCE.eClass();
        BehavioralPackage.eINSTANCE.eClass();
        RelationshipsPackage.eINSTANCE.eClass();
        InstancePackage.eINSTANCE.eClass();
        BusinessinformationPackage.eINSTANCE.eClass();
        DatatypesPackage.eINSTANCE.eClass();
        ExpressionsPackage.eINSTANCE.eClass();
        KeysindexesPackage.eINSTANCE.eClass();
        SoftwaredeploymentPackage.eINSTANCE.eClass();
        TypemappingPackage.eINSTANCE.eClass();
        RelationalPackage.eINSTANCE.eClass();
        RecordPackage.eINSTANCE.eClass();
        MultidimensionalPackage.eINSTANCE.eClass();
        XmlPackage.eINSTANCE.eClass();
        TransformationPackage.eINSTANCE.eClass();
        OlapPackage.eINSTANCE.eClass();
        DataminingPackage.eINSTANCE.eClass();
        InformationvisualizationPackage.eINSTANCE.eClass();
        BusinessnomenclaturePackage.eINSTANCE.eClass();
        WarehouseprocessPackage.eINSTANCE.eClass();
        WarehouseoperationPackage.eINSTANCE.eClass();
        ErPackage.eINSTANCE.eClass();
        CoboldataPackage.eINSTANCE.eClass();
        DmsiiPackage.eINSTANCE.eClass();
        ImsdatabasePackage.eINSTANCE.eClass();
        EssbasePackage.eINSTANCE.eClass();
        ExpressPackage.eINSTANCE.eClass();
        InformationsetPackage.eINSTANCE.eClass();
        InformationreportingPackage.eINSTANCE.eClass();
        CwmmipPackage.eINSTANCE.eClass();
        ModelPackage.eINSTANCE.eClass();
        EcorePackage.eINSTANCE.eClass();
        JobletPackage.eINSTANCE.eClass();
        ConnectionPackage.eINSTANCE.eClass();
        NotationPackage.eINSTANCE.eClass();
        PropertiesPackage.eINSTANCE.eClass();
        TalendFilePackage.eINSTANCE.eClass();
        XMLTypePackage.eINSTANCE.eClass();

        // Create package meta-data objects
        theCamelPropertiesPackage.createPackageContents();

        // Initialize created meta-data
        theCamelPropertiesPackage.initializePackageContents();

        // Mark meta-data to indicate it can't be changed
        theCamelPropertiesPackage.freeze();

        // Update the registry and return the package
        EPackage.Registry.INSTANCE.put(CamelPropertiesPackage.eNS_URI, theCamelPropertiesPackage);
        return theCamelPropertiesPackage;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    public EClass getBeanItem() {
        return beanItemEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getBeansJarItem() {
        return beansJarItemEClass;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    public EClass getCamelProcessItem() {
        return camelProcessItemEClass;
    }

    /**
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	public EAttribute getCamelProcessItem_SpringContent() {
        return (EAttribute)camelProcessItemEClass.getEStructuralFeatures().get(0);
    }

				/**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getCamelProcessItem_ExportMicroService() {
        return (EAttribute)camelProcessItemEClass.getEStructuralFeatures().get(1);
    }

                /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    public CamelPropertiesFactory getCamelPropertiesFactory() {
        return (CamelPropertiesFactory)getEFactoryInstance();
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    private boolean isCreated = false;

    /**
     * Creates the meta-model objects for the package.  This method is
     * guarded to have no affect on any invocation but its first.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    public void createPackageContents() {
        if (isCreated) return;
        isCreated = true;

        // Create classes and their features
        beanItemEClass = createEClass(BEAN_ITEM);

        beansJarItemEClass = createEClass(BEANS_JAR_ITEM);

        camelProcessItemEClass = createEClass(CAMEL_PROCESS_ITEM);
        createEAttribute(camelProcessItemEClass, CAMEL_PROCESS_ITEM__SPRING_CONTENT);
        createEAttribute(camelProcessItemEClass, CAMEL_PROCESS_ITEM__EXPORT_MICRO_SERVICE);

        routeResourceItemEClass = createEClass(ROUTE_RESOURCE_ITEM);
        createEAttribute(routeResourceItemEClass, ROUTE_RESOURCE_ITEM__BINDING_EXTENSION);

        routeDocumentItemEClass = createEClass(ROUTE_DOCUMENT_ITEM);
        createEAttribute(routeDocumentItemEClass, ROUTE_DOCUMENT_ITEM__BINDING_EXTENSION);

        routeletProcessItemEClass = createEClass(ROUTELET_PROCESS_ITEM);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    private boolean isInitialized = false;

    /**
     * Complete the initialization of the package and its meta-model.  This
     * method is guarded to have no affect on any invocation but its first.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    public void initializePackageContents() {
        if (isInitialized) return;
        isInitialized = true;

        // Initialize package
        setName(eNAME);
        setNsPrefix(eNS_PREFIX);
        setNsURI(eNS_URI);

        // Obtain other dependent packages
        PropertiesPackage thePropertiesPackage = (PropertiesPackage)EPackage.Registry.INSTANCE.getEPackage(PropertiesPackage.eNS_URI);
        XMLTypePackage theXMLTypePackage = (XMLTypePackage)EPackage.Registry.INSTANCE.getEPackage(XMLTypePackage.eNS_URI);

        // Create type parameters

        // Set bounds for type parameters

        // Add supertypes to classes
        beanItemEClass.getESuperTypes().add(thePropertiesPackage.getRoutineItem());
        beansJarItemEClass.getESuperTypes().add(thePropertiesPackage.getRoutinesJarItem());
        camelProcessItemEClass.getESuperTypes().add(thePropertiesPackage.getProcessItem());
        routeResourceItemEClass.getESuperTypes().add(thePropertiesPackage.getFileItem());
        routeDocumentItemEClass.getESuperTypes().add(thePropertiesPackage.getDocumentationItem());
        routeletProcessItemEClass.getESuperTypes().add(thePropertiesPackage.getProcessItem());

        // Initialize classes and features; add operations and parameters
        initEClass(beanItemEClass, BeanItem.class, "BeanItem", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

        initEClass(beansJarItemEClass, BeansJarItem.class, "BeansJarItem", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

        initEClass(camelProcessItemEClass, CamelProcessItem.class, "CamelProcessItem", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getCamelProcessItem_SpringContent(), theXMLTypePackage.getString(), "springContent", "", 0, 1, CamelProcessItem.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getCamelProcessItem_ExportMicroService(), ecorePackage.getEBoolean(), "exportMicroService", null, 0, 1, CamelProcessItem.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(routeResourceItemEClass, RouteResourceItem.class, "RouteResourceItem", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getRouteResourceItem_BindingExtension(), theXMLTypePackage.getString(), "bindingExtension", null, 0, 1, RouteResourceItem.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(routeDocumentItemEClass, RouteDocumentItem.class, "RouteDocumentItem", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getRouteDocumentItem_BindingExtension(), theXMLTypePackage.getString(), "bindingExtension", null, 0, 1, RouteDocumentItem.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(routeletProcessItemEClass, RouteletProcessItem.class, "RouteletProcessItem", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

        // Create resource
        createResource(eNS_URI);
    }

	public EClass getRouteResourceItem() {
		return routeResourceItemEClass;
	}

	/**
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	public EAttribute getRouteResourceItem_BindingExtension() {
        return (EAttribute)routeResourceItemEClass.getEStructuralFeatures().get(0);
    }

	/**
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	public EClass getRouteDocumentItem() {
        return routeDocumentItemEClass;
    }

	/**
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	public EAttribute getRouteDocumentItem_BindingExtension() {
        return (EAttribute)routeDocumentItemEClass.getEStructuralFeatures().get(0);
    }

	/**
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	public EClass getRouteletProcessItem() {
        return routeletProcessItemEClass;
    }

} // CamelPropertiesPackageImpl
