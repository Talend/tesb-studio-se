**************************************************
**** Readme - Non UI based installation      *****
**** Talend ESB - cMessageEndpoint Libraries *****
**************************************************

Starting with Talend Studio (Subscription Studio) 6.3.1 we do not ship the RouteBuilder cMessageEndpoint Libraries by default with the Talend Studio anymore.
As the overall size of the libraries reach a critical size of >= 1.0 GB.  
Beside the installation via the Additional Packages Screen in the Studio there is also a way to install the complete set of libraries in the following way: 
 
Example using a Windows 6.5 (EXE) and Windows path for the P2 Folder:
Talend-Studio-win-x86_64.exe -nosplash -consoleLog -application org.eclipse.equinox.p2.director -repository file:///<Path>//Talend_ESB_cMessageEndpoint_p2_Repository-<timestamp>-V<version> -installIU org.talend.esb.camel.alldeps.feature.feature.group

You will see an operation complete message in the console, after you executed the above command. Once this is done please launch Studio/CommandLine with -clean and -talendReload parameters:
e.g. for Studio on Windows 64bit use:  Talend-Studio-win-x86_64.exe -clean -talendReload
After the Studio/Commandline was started once in this way the subsequent starts can just be done as usual (without specific parameter).

The above will install the libraries to the Studio/plugins/org.talend.libraries.camel.alldeps_xxxx folder using the local p2 repository which we ship with all Talend Subscription Products (Talend_ESB_cMessageEndpoint_p2_Repository-<timestamp>-V<version>.zip). 
This command allows an offline installation and would not require the 1.0GB to be downloaded via the internet. 
The -repository parameter above requires a folder and by this you must 'unzip' the Talend_ESB_cMessageEndpoint_p2_Repository-<timestamp>-V<version>.zip which comes with the cMessageEndpoint Libraries to a local folder before you can use the command above.

The above can be used for the Studio but also for the 'CommandLine' as it provides an offline and non-UI based installation of the cMessageEndpoint Libraries.

The Talend Team, 25th Aug 2017