package org.talend.camel.designer.util;

import java.util.Iterator;

import org.eclipse.emf.common.util.EList;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.PluginChecker;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.ui.IJobletProviderService;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;
import org.talend.repository.utils.EmfModelUtils;

public class CamelDesignerUtil {

	private static IJobletProviderService service = null;
	
	static{
		if (PluginChecker.isJobLetPluginLoaded()) {
			service = (IJobletProviderService) GlobalServiceRegister.getDefault().getService(
					IJobletProviderService.class);
		}
	}

	public static boolean checkRouteInputExistInJob(ProcessItem pi) {
		if(pi == null){
			return false;
		}
		return recursiveCheckRouteInputExistInProcess(pi.getProcess());
	}

	public static boolean checkRouteInputExistInJoblet(ProcessType jobletProcess) {
		
		return recursiveCheckRouteInputExistInProcess(jobletProcess);
	}

	private static boolean recursiveCheckRouteInputExistInProcess(
			ProcessType process) {
		if(process == null){
			return false;
		}
		EList<?> node = process.getNode();
		Iterator<?> iterator = node.iterator();
		while(iterator.hasNext()){
			Object next = iterator.next();
			if(!(next instanceof NodeType)){
				continue;
			}
			NodeType nt = (NodeType) next;
			if(!EmfModelUtils.isComponentActive(nt) && !EmfModelUtils.computeCheckElementValue("ACTIVATE", nt)){
				continue;
			}
			String componentName = nt.getComponentName();
			if ("tRouteInput".equals(componentName)) {
				return true;
			}else if(service != null){
				ProcessType subProcess = service.getJobletProcess(nt);
				if(recursiveCheckRouteInputExistInProcess(subProcess)){
					return true;
				}
			}
		}
		return false;
	}
}
