// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.camel.designer.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.EList;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.model.general.Project;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.utils.JavaResourcesHelper;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.designer.core.IDesignerCoreService;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.utils.emf.talendfile.ConnectionType;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.ElementValueType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;
import org.talend.designer.maven.utils.PomIdsHelper;
import org.talend.designer.publish.core.models.FeatureModel;
import org.talend.designer.publish.core.models.FeaturesModel;
import org.talend.repository.ProjectManager;
import org.talend.repository.RepositoryPlugin;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.utils.EmfModelUtils;

/**
 * Camel component feature
 * 
 * http://jira.talendforge.org/browse/TESB-5375
 * 
 * @author LiXiaopeng
 * 
 */
public final class CamelFeatureUtil {

	private static final FeatureModel FEATURE_CAMEL_GROOVY = new FeatureModel("camel-groovy"); //$NON-NLS-1$
	private static final FeatureModel FEATURE_CAMEL_SCRIPT_JAVASCRIPT = new FeatureModel("camel-script-javascript"); //$NON-NLS-1$
	private static final FeatureModel FEATURE_CAMEL_SCRIPT = new FeatureModel("camel-script"); //$NON-NLS-1$

	// ActiveMQ Karaf integration issue
	private static final FeatureModel FEATURE_ACTIVEMQ_OPTIONAL = new FeatureModel("camel-http4"); //$NON-NLS-1$

	private static final FeatureModel FEATURE_ESB_SAM = new FeatureModel("tesb-sam-agent"); //$NON-NLS-1$
	private static final FeatureModel FEATURE_ESB_LOCATOR = new FeatureModel("tesb-locator-client"); //$NON-NLS-1$

    @SuppressWarnings("serial")
    private static final Map<String, FeatureModel[]> camelFeaturesMap = new HashMap<String, FeatureModel[]>() {{
        //put("camel-cxf", new FeatureModel[] { new FeatureModel("camel-cxf"), new FeatureModel("cxf") });
        put("camel-http", new FeatureModel[] { new FeatureModel("camel-http"), new FeatureModel("http") });
        put("camel-http-common", new FeatureModel[] { });
        put("activemq-all", new FeatureModel[] { new FeatureModel("activemq-camel") });
        put("tdm-camel", new FeatureModel[] { new FeatureModel("talend-data-mapper") });
        //put("camel-talendjob", new FeatureModel[] { new FeatureModel("camel-talendjob") });
        put("camel-cxf-transport", new FeatureModel[] { });
        put("camel-jetty-common", new FeatureModel[] { });
        put("camel-jetty8", new FeatureModel[] { });
        put("camel-jetty", new FeatureModel[] { new FeatureModel("camel-jetty9") });
    }};

	private static final String JAVA_SCRIPT = "javaScript"; //$NON-NLS-1$

	private static final String LANGUAGES = "LANGUAGES"; //$NON-NLS-1$
	private static final String LOOP_TYPE = "LOOP_TYPE"; //$NON-NLS-1$


    private static Collection<FeatureModel> computeFeature(String libraryName) {
        FeatureModel[] features = camelFeaturesMap.get(libraryName);
        if (null == features && libraryName.startsWith("camel-")) { //$NON-NLS-1$
            features = new FeatureModel[] { new FeatureModel(
                libraryName.endsWith("-alldep") //$NON-NLS-1$
                ? libraryName.substring(0, libraryName.length() - "-alldep".length()) //$NON-NLS-1$
                : libraryName) };
        }
        return features != null ? Arrays.asList(features) : null;
    }

    private static String getNameWithoutVersion(String libraryName) {
        if (!libraryName.endsWith(".jar")) {
            return libraryName;
        }
        int index = 0;
        int lastIndexOf;
        while ((lastIndexOf = libraryName.indexOf('-', index + 1)) != -1) {
            char ch = libraryName.charAt(lastIndexOf + 1);
            index = lastIndexOf;
            if (Character.isDigit(ch)) {
                break;
            }
        }
        return index != 0 ? libraryName.substring(0, index) : libraryName;
    }

	private static void addNodesSpecialFeatures(Collection<FeatureModel> features, ProcessType processType) {
		for (Object o : processType.getNode()) {
			if (o instanceof NodeType) {
				NodeType currentNode = (NodeType) o;
				if (!EmfModelUtils.isComponentActive(currentNode)) {
				    continue;
				}
				String componentName = currentNode.getComponentName();
				if ("cSOAP".equals(componentName) || "cREST".equals(componentName)) {
					handleCXFcase(features, currentNode);
				} else if ("cLoop".equals(componentName)) {
					handleLoopCase(features, currentNode);
				} else if ("cMessageFilter".equals(componentName)) {
				    handleLanguagesJavascript(features, currentNode);
				} else if ("cRecipientList".equals(componentName)) {
				    handleLanguagesJavascript(features, currentNode);
				} else if ("cSetBody".equals(componentName)) {
					handleLanguagesJavascript(features, currentNode);
				} else if ("cSetHeader".equals(componentName)) {
					handleSetHeaderCase(features, currentNode);
				} else if ("cMQConnectionFactory".equals(componentName)) {
					handleMQConnectionFactory(features, currentNode);
				}
			}
		}
	}

    private static void handleMQConnectionFactory(Collection<FeatureModel> features, NodeType currentNode) {
        if ("ActiveMQ".equals(EmfModelUtils.findElementParameterByName("MQ_TYPE", currentNode).getValue())
            && EmfModelUtils.computeCheckElementValue("IS_AMQ_HTTP_BROKER", currentNode)) {
            features.add(FEATURE_ACTIVEMQ_OPTIONAL);
        }
    }

	private static void addConnectionsSpecialFeatures(
			Collection<FeatureModel> features, ProcessType processType) {
		EList<?> connections = processType.getConnection();
		Iterator<?> iterator = connections.iterator();
		while(iterator.hasNext()){
			Object next = iterator.next();
			if(!(next instanceof ConnectionType)){
				continue;
			}
			ConnectionType con = (ConnectionType) next;
			if(!EConnectionType.ROUTE_WHEN.getName().equals(con.getConnectorName())){
				continue;
			}
			EList<?> elementParameters = con.getElementParameter();
			Iterator<?> paraIter = elementParameters.iterator();
			while(paraIter.hasNext()){
				Object paraNext = paraIter.next();
				if(!(paraNext instanceof ElementParameterType)){
					continue;
				}
				ElementParameterType ept = (ElementParameterType) paraNext;
				if(!EParameterName.ROUTETYPE.getName().equals(ept.getName())){
					continue;
				}
//	            String[] strList = { "constant", "el", "groovy", "header", "javaScript", "jxpath", "mvel", "ognl", "php", "property",
//	                    "python", "ruby", "simple", "spel", "sql", "xpath", "xquery" };
				if("groovy".equals(ept.getValue())){
					features.add(FEATURE_CAMEL_GROOVY);
				} else if ("javaScript".equals(ept.getValue())) {
					features.add(FEATURE_CAMEL_SCRIPT);
					features.add(FEATURE_CAMEL_SCRIPT_JAVASCRIPT);
				}
			}
		}
	}

	private static void handleSetHeaderCase(Collection<FeatureModel> features, NodeType currentNode) {
		ElementParameterType element = EmfModelUtils.findElementParameterByName("VALUES", currentNode);
		Iterator<?> iterator = element.getElementValue().iterator();
		while (iterator.hasNext()) {
			Object next = iterator.next();
			if(!(next instanceof ElementValueType)) {
				continue;
			}
			ElementValueType evt = (ElementValueType) next;
			String elementRef = evt.getElementRef();
			if ("LANGUAGE".equals(elementRef) && JAVA_SCRIPT.equals(evt.getValue())) {
				features.add(FEATURE_CAMEL_SCRIPT_JAVASCRIPT);
				break;
			}
		}
	}

    private static void handleLanguagesJavascript(Collection<FeatureModel> features, NodeType currentNode) {
        if (JAVA_SCRIPT.equals(EmfModelUtils.findElementParameterByName(LANGUAGES, currentNode).getValue())){
            features.add(FEATURE_CAMEL_SCRIPT_JAVASCRIPT);
        }
    }

    private static void handleLoopCase(Collection<FeatureModel> features, NodeType currentNode) {
        if ("EXPRESSION_TYPE".equals(EmfModelUtils.findElementParameterByName(LOOP_TYPE, currentNode).getValue())
            && JAVA_SCRIPT.equals(EmfModelUtils.findElementParameterByName(LANGUAGES, currentNode).getValue())) {
            features.add(FEATURE_CAMEL_SCRIPT_JAVASCRIPT);
        }
    }

    private static void handleCXFcase(Collection<FeatureModel> features, NodeType currentNode) {
        if (EmfModelUtils.computeCheckElementValue("ENABLE_SAM", currentNode)) {
            features.add(FEATURE_ESB_SAM);
        }
        if (EmfModelUtils.computeCheckElementValue("ENABLE_SL", currentNode)) {
            // http://jira.talendforge.org/browse/TESB-5461
            features.add(FEATURE_ESB_LOCATOR);
        }
    }

	/**
	 * Add feature and bundle to Feature Model
	 * 
	 * @param node
	 * @param featuresModel
	 */
    public static void addFeatureAndBundles(ProcessItem routeProcess, FeaturesModel featuresModel) {
        IDesignerCoreService designerService = RepositoryPlugin.getDefault().getDesignerCoreService();
        IProcess process = designerService.getProcessFromProcessItem(routeProcess, false);

        Collection<FeatureModel> features = new HashSet<FeatureModel>();
        for (String lib : process.getNeededLibraries(true)) {      	
            Collection<FeatureModel> featureModel = computeFeature(getNameWithoutVersion(lib));
            if (featureModel != null) {
                features.addAll(featureModel);
            }
        }

        addNodesSpecialFeatures(features, routeProcess.getProcess());
        addConnectionsSpecialFeatures(features, routeProcess.getProcess());

        for (FeatureModel model : features) {
            featuresModel.addFeature(model);
        }
    }

	/**
	 * 
	 * DOC ggu Comment method "getMavenGroupId".
	 * 
	 * @param item
	 * @return
	 */
	public static String getMavenGroupId(Item item) {
		if (item != null) {
		    return PomIdsHelper.getJobGroupId(item.getProperty());
		}
		return null;
	}

	@Deprecated
	public static String getMavenGroupId(String jobId, String jobName, String defaultProject) {
		return JavaResourcesHelper.getGroupItemName(
				getJobProjectName(jobId, jobName, defaultProject), jobName);
	}

	private static String getJobProjectName(String jobId, String jobName, String defaultProject) {

        if (jobId == null || jobId.isEmpty()) {
            return defaultProject;
        }

        if (jobName == null || jobName.isEmpty()) {
            return defaultProject;
        }

        IRepositoryNode referencedJobNode = null;
        Project referenceProject = null;
        try {
            List<Project> projects = ProjectManager.getInstance().getAllReferencedProjects();
            if (projects == null) {
                return defaultProject;
            }
            for (Project p : projects) {
                List<IRepositoryViewObject> jobs = ProxyRepositoryFactory.getInstance().getAll(
                		p, ERepositoryObjectType.PROCESS);
                if (jobs == null) {
                    continue;
                }
                for (IRepositoryViewObject job : jobs) {
                    if (job.getId().equals(jobId)) {
                        referencedJobNode = new RepositoryNode(
                        		job, null, IRepositoryNode.ENodeType.REPOSITORY_ELEMENT);
                        referenceProject = p;
                        break;
                    }
                }
                if (referenceProject != null) {
                    break;
                }
            }
        } catch (PersistenceException e) {
            return defaultProject;
        }

        if (referencedJobNode == null) {
            return defaultProject;
        }

        Property p = referencedJobNode.getObject().getProperty();
        String jobNameFound = p.getDisplayName();
        String jobLabelFound = p.getLabel();

        if ((jobNameFound == null || !jobNameFound.equals(jobName)) &&
        		(jobLabelFound == null || !jobNameFound.equals(jobName))) {
            return defaultProject;
        }

        if (referenceProject != null) {
            return referenceProject.getLabel().toLowerCase();
        }

        return defaultProject;
	}
}
