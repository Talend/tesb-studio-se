// ============================================================================
//
// Copyright (C) 2006-2014 Talend Inc. - www.talend.com
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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.talend.camel.designer.ui.editor.RouteProcess;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.Property;
import org.talend.core.model.utils.JavaResourcesHelper;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.utils.emf.talendfile.ConnectionType;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.ElementValueType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;
import org.talend.designer.publish.core.models.FeatureModel;
import org.talend.designer.publish.core.models.FeaturesModel;
import org.talend.repository.model.IRepositoryNode;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Camel component feature
 * 
 * http://jira.talendforge.org/browse/TESB-5375
 * 
 * @author LiXiaopeng
 * 
 */
public final class CamelFeatureUtil {

	private static final String MAPPING_XML_FILE = "CamelFeatures.xml";

	private static final String CAMEL_VERSION_RANGE = "[2,5)";

	private static final FeatureModel FEATURE_CAMEL_GROOVY = new FeatureModel("camel-groovy", CAMEL_VERSION_RANGE);
	private static final FeatureModel FEATURE_CAMEL_SCRIPT_JAVASCRIPT = new FeatureModel("camel-script-javascript", CAMEL_VERSION_RANGE);
	private static final FeatureModel FEATURE_CAMEL_SCRIPT = new FeatureModel("camel-script", CAMEL_VERSION_RANGE);

	private static final FeatureModel FEATURE_ACTIVEMQ_OPTIONAL = new FeatureModel("activemq-http", "[5,10)");

	private static final FeatureModel FEATURE_ESB_SAM = new FeatureModel("tesb-sam-agent", FeaturesModel.ESB_FEATURE_VERSION_RANGE);
	private static final FeatureModel FEATURE_ESB_LOCATOR = new FeatureModel("tesb-locator-client", FeaturesModel.ESB_FEATURE_VERSION_RANGE);

	private static final Map<String, Collection<FeatureModel>> camelFeaturesMap =
			new HashMap<String, Collection<FeatureModel>>();

	private static final String JAVA_SCRIPT = "javaScript";

	private static final String LANGUAGES = "LANGUAGES";
	private static final String LOOP_TYPE = "LOOP_TYPE";


	/**
	 * Check the node is Route
	 * 
	 * @param node
	 * @return
	 */
	private static boolean checkNode(IRepositoryNode node) {
		if (node == null) {
			return false;
		}
		if (node.getObjectType() != CamelRepositoryNodeType.repositoryRoutesType) {
			return false;
		}
		return true;
	}

	/**
	 * Compute check field parameter value with a given parameter name
	 * 
	 * @param paramName
	 * @param elementParameterTypes
	 * @return
	 */
	protected static boolean computeCheckElementValue(String paramName,
			List<?> elementParameterTypes) {
		ElementParameterType cpType = findElementParameterByName(paramName,
				elementParameterTypes);
		if (cpType == null) {
			return false;
		}
		String isNone = cpType.getValue();
		return "true".equals(isNone);
	}

	/**
	 * 
	 * @param libraryName
	 * @return
	 */
	private static Collection<FeatureModel> computeFeature(String libraryName) {
		if (camelFeaturesMap.isEmpty()) {
			initMap();
		}
		String nameWithoutVersion = getNameWithoutVersion(libraryName);
		return camelFeaturesMap.get(nameWithoutVersion);
	}

	/**
	 * 
	 * @param libraryName
	 * @return
	 */
//	private static Collection<BundleModel> computeBundle(String libraryName) {
//		if (camelBundlesMap.isEmpty()) {
//			initMap();
//		}
//		String nameWithoutVersion = getNameWithoutVersion(libraryName);
//		return camelBundlesMap.get(nameWithoutVersion);
//	}

	private static String getNameWithoutVersion(String libraryName){
		if(libraryName == null || libraryName.isEmpty() || !libraryName.endsWith(".jar")){
			return libraryName;
		}
		String interName = libraryName;
		int lastIndexOf = interName.lastIndexOf('-');
		while(lastIndexOf != -1){
			char followChar = interName.charAt(lastIndexOf+1);
			boolean isFollowNum = followChar >='0' && followChar<='9';
			interName = interName.substring(0,lastIndexOf);
			if(isFollowNum) {
				break;
			}
			lastIndexOf = interName.lastIndexOf('-');
		}
		return interName;
	}

	protected static ElementParameterType findElementParameterByName(
			String paramName, List<?> elementParameterTypes) {
		for (Object obj : elementParameterTypes) {
			ElementParameterType cpType = (ElementParameterType) obj;
			if (paramName.equals(cpType.getName())) {
				return cpType;
			}
		}
		return null;
	}
	
	protected static Map<String, ElementParameterType> findElementParameterByNames(
			List<String> paramNames, List<?> elementParameterTypes) {
		Map<String, ElementParameterType> map = new HashMap<String, ElementParameterType>(paramNames.size());
		for (Object obj : elementParameterTypes) {
			ElementParameterType cpType = (ElementParameterType) obj;
			String name = cpType.getName();
			if(paramNames.contains(name)){
				map.put(name, cpType);
				paramNames.remove(name);
			}
			
		}
		return map;
	}

	/**
	 * 
	 * @param node
	 * @return
	 */
	private static Collection<FeatureModel> getFeaturesOfRoute(
			Collection<String> neededLibraries, ProcessType processType) {

		Collection<FeatureModel> features = new HashSet<FeatureModel>();
		for (String lib : neededLibraries) {
			Collection<FeatureModel> featureModel = computeFeature(lib);
			if (featureModel != null) {
				features.addAll(featureModel);
			}
		}

		addProcessSpecialFeatures(features, processType);

		return features;
	}

	private static void addProcessSpecialFeatures(Collection<FeatureModel> features,
			ProcessType processType) {
		addNodesSpecialFeatures(features, processType);
		addConnectionsSpecialFeatures(features, processType);
	}

	private static void addNodesSpecialFeatures(Collection<FeatureModel> features,
			ProcessType processType) {
		for (Object o : processType.getNode()) {
			if (o instanceof NodeType) {
				NodeType currentNode = (NodeType) o;
				String componentName = currentNode.getComponentName();
				if ("cCXF".equals(componentName) || "cCXFRS".equals(componentName)) {
					handleCXFcase(features, currentNode);
				}else if("cLoop".equals(componentName)){
					handleLoopCase(features, currentNode);
				}else if("cMessageFilter".equals(componentName)){
					handleMessageFilterCase(features, currentNode);
				}else if("cRecipientList".equals(componentName)){
					handleRecipientListCase(features, currentNode);
				}else if("cSetBody".equals(componentName)){
					handleSetBodyCase(features, currentNode);
				}else if("cSetHeader".equals(componentName)){
					handleSetHeaderCase(features, currentNode);
				}else if("cMQConnectionFactory".equals(componentName)){
					handleMQConnectionFactory(features, currentNode);
				}
			}
		}
	}

	private static void handleMQConnectionFactory(
			Collection<FeatureModel> features, NodeType currentNode) {
		ElementParameterType mqType = findElementParameterByName("MQ_TYPE", currentNode.getElementParameter());
		if("ActiveMQ".equals(mqType.getValue())){
			ElementParameterType useHttpBroker = findElementParameterByName("IS_AMQ_HTTP_BROKER", currentNode.getElementParameter());
			if (null != useHttpBroker && "true".equals(useHttpBroker.getValue())) {
				features.add(FEATURE_ACTIVEMQ_OPTIONAL);
			}
		}
	}

	private static void addConnectionsSpecialFeatures(
			Collection<FeatureModel> features, ProcessType processType) {
		List<?> connections = processType.getConnection();
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
			List<?> elementParameters = con.getElementParameter();
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
	
	protected static void handleSetHeaderCase(Collection<FeatureModel> features,
			NodeType currentNode) {
		ElementParameterType element = findElementParameterByName("VALUES", currentNode.getElementParameter());
		List<?> elementValue = element.getElementValue();
		Iterator<?> iterator = elementValue.iterator();
		while(iterator.hasNext()){
			Object next = iterator.next();
			if(!(next instanceof ElementValueType)){
				continue;
			}
			ElementValueType evt = (ElementValueType) next;
			String elementRef = evt.getElementRef();
			if("LANGUAGE".equals(elementRef) && JAVA_SCRIPT.equals(evt.getValue())){
				features.add(FEATURE_CAMEL_SCRIPT_JAVASCRIPT);
				break;
			}
		}
		
	}

	protected static void handleSetBodyCase(Collection<FeatureModel> features,
			NodeType currentNode) {
		List<?> parameters = currentNode.getElementParameter();

		ElementParameterType languages = findElementParameterByName(LANGUAGES, parameters);
		if(!JAVA_SCRIPT.equals(languages.getValue())){
			return;
		}
		features.add(FEATURE_CAMEL_SCRIPT_JAVASCRIPT);
	}

	private static void handleRecipientListCase(Collection<FeatureModel> features,
			NodeType currentNode) {
		List<?> parameters = currentNode.getElementParameter();

		ElementParameterType languages = findElementParameterByName(LANGUAGES, parameters);
		if(!JAVA_SCRIPT.equals(languages.getValue())){
			return;
		}
		features.add(FEATURE_CAMEL_SCRIPT_JAVASCRIPT);
	}

	protected static void handleMessageFilterCase(Collection<FeatureModel> features,
			NodeType currentNode) {
		List<?> parameters = currentNode.getElementParameter();

		ElementParameterType languages = findElementParameterByName(LANGUAGES, parameters);
		if(!JAVA_SCRIPT.equals(languages.getValue())){
			return;
		}
		features.add(FEATURE_CAMEL_SCRIPT_JAVASCRIPT);
	}

	protected static void handleLoopCase(Collection<FeatureModel> features,
			NodeType currentNode) {
		List<?> parameters = currentNode.getElementParameter();
		List<String> paraNames = new ArrayList<String>();
		paraNames.add(LOOP_TYPE);
		paraNames.add(LANGUAGES);

		Map<String, ElementParameterType> found = findElementParameterByNames(paraNames, parameters);
		ElementParameterType loopType = found.get(LOOP_TYPE);
		if(!"EXPRESSION_TYPE".equals(loopType.getValue())){
			return;
		}
		ElementParameterType languages = found.get(LANGUAGES);
		if(!JAVA_SCRIPT.equals(languages.getValue())){
			return;
		}
		features.add(FEATURE_CAMEL_SCRIPT_JAVASCRIPT);
	}

	protected static void handleCXFcase(Collection<FeatureModel> features,
			NodeType currentNode) {
		boolean sam = computeCheckElementValue("ENABLE_SAM",
				currentNode.getElementParameter());
		if (sam) {
			features.add(FEATURE_ESB_SAM);
		}

		boolean sl = computeCheckElementValue("ENABLE_SL",
				currentNode.getElementParameter());
		if (sl) {
			// http://jira.talendforge.org/browse/TESB-5461
			features.add(FEATURE_ESB_LOCATOR);
		}
	}

	/**
	 * 
	 */
	private static void initMap() {
		XPathFactory xpFactory = XPathFactory.newInstance();
		XPath newXPath = xpFactory.newXPath();

		try {
			InputStream input = CamelFeatureUtil.class
					.getResourceAsStream(MAPPING_XML_FILE);
			DocumentBuilder builder = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder();
			Document document = builder.parse(input);

			try {
				NodeList list = (NodeList) newXPath.evaluate("//FeatureMaps",
						document, XPathConstants.NODESET);
				list = (NodeList) newXPath.evaluate("//FeatureMap/Feature",
						document, XPathConstants.NODESET);

				for (int index = 0; index < list.getLength(); index++) {

					Node node = list.item(index);
					String hotLib = node.getParentNode().getAttributes()
							.getNamedItem("HotLib").getNodeValue();
					Collection<FeatureModel> features = camelFeaturesMap
							.get(hotLib);
					if (features == null) {
						features = new HashSet<FeatureModel>();
						camelFeaturesMap.put(hotLib, features);
					}

					String featureVersion = node.getAttributes()
							.getNamedItem("version").getNodeValue();
					String featureName = node.getFirstChild().getNodeValue();
					features.add(new FeatureModel(featureName,
							featureVersion));
				}
			} finally {
				input.close();
			}

		} catch (Exception e) {
			ExceptionHandler.process(e);
		}

	}

	/**
	 * Add feature and bundle to Feature Model
	 * 
	 * @param node
	 * @param featuresModel
	 */
	public static void addFeatureAndBundles(IRepositoryNode node,
			FeaturesModel featuresModel) {

		if (!checkNode(node)) {
			return;
		}

		Property property = node.getObject().getProperty();
		//changed for TDI-24563
//		Process process = new org.talend.designer.core.ui.editor.process.Process(
//				property);
		RouteProcess process = new RouteProcess(property);
		process.loadXmlFile();
		Collection<String> neededLibraries = process.getNeededLibraries(true);

		Collection<FeatureModel> features = getFeaturesOfRoute(neededLibraries,
				((ProcessItem) property.getItem()).getProcess());
		for (FeatureModel model : features) {
			featuresModel.addFeature(model);
		}

		process.dispose();

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
			String projectName = JavaResourcesHelper.getProjectFolderName(item);
			String itemName = item.getProperty().getDisplayName(); // .getLabel()
																	// ?

			return projectName + '.' + itemName;
		}
		return null;
	}
}