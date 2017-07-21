
<%@ include file="/include-internal.jsp"%>
<%@ page import="com.github.rodm.teamcity.gradle.scripts.GradleInitScriptsPlugin" %>

<c:set var="INIT_SCRIPT_NAME_PARAMETER" value="<%=GradleInitScriptsPlugin.INIT_SCRIPT_NAME_PARAMETER%>"/>

<l:settingsGroup title="Gradle Init Scripts" className="advancedSetting">
    <tr class="advancedSetting">
    <th class="noBorder"><label for="${INIT_SCRIPT_NAME_PARAMETER}">Initialization script:</label></th>
    <td class="noBorder">
        <jsp:include page="/admin/initScripts.html?projectId=${buildForm.project.projectId}&chooserName=${INIT_SCRIPT_NAME_PARAMETER}"/>
        <span class="smallNote">Select one of the predefined scripts.</span>
        <span>
            <admin:editProjectLink projectId="${buildForm.project.externalId}" addToUrl="&tab=gradleInitScripts">
                Manage init scripts
            </admin:editProjectLink>
        </span>
    </td>
    </tr>
</l:settingsGroup>
