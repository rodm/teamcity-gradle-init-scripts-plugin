<%--
  * Copyright 2017 Rod MacKenzie.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  --%>

<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="admin" tagdir="/WEB-INF/tags/admin" %>
<%@ include file="/include-internal.jsp" %>

<jsp:useBean id="currentProject" type="jetbrains.buildServer.serverSide.SProject" scope="request"/>

<%--@elvariable id="scripts" type="java.util.List"--%>
<div class="section noMargin">
    <h2 class="noBorder">Gradle Init Scripts</h2>
    <bs:smallNote>In this section you can manage the Gradle initialization scripts to reuse them within the project</bs:smallNote>

    <bs:messages key="initScriptsMessage"/>
    <bs:messages key="initScriptsError" className="error"/>

    <c:choose>
        <%--@elvariable id="fileName" type="java.lang.String"--%>
        <%--@elvariable id="fileContent" type="java.lang.String"--%>
        <c:when test="${fileContent ne null}">
            <c:url var="homeUrl" value="/admin/editProject.html?projectId=${currentProject.externalId}&tab=gradleInitScripts"/>
            <div class="headMessage">
                <span style="font-weight: bold; padding-right: 2em">
                    <c:out value="${fileName}"/>
                </span>
                <span class="fileOperations">
                <a href="${homeUrl}">&laquo; All files</a>
                <span class="separator">|</span>
                <i class="icon-trash"></i>
                <a href="#" onclick="BS.GradleInitScripts.deleteScript('${currentProject.projectId}', '${fileName}')" title="Remove this file"> Delete</a>
              </span>
            </div>
            <pre class="fileContent">
                <c:out value="${fileContent}"/>
            </pre>
        </c:when>

        <c:otherwise>
            <%--@elvariable id="inspections" type="java.util.List"--%>
            <c:if test="${not empty inspections}">
                <%@include file="health/report.jspf"%>
            </c:if>
            <authz:authorize projectId="${currentProject.projectId}" allPermissions="EDIT_PROJECT">
                <jsp:attribute name="ifAccessGranted">
                    <div class="upload">
                        <forms:addButton
                                onclick="return BS.GradleAddInitScripts.show();">Upload init script file</forms:addButton>
                    </div>
                    <bs:dialog dialogId="addInitScripts"
                               dialogClass="uploadDialog"
                               title="Upload Gradle Init Scripts"
                               titleId="addInitScriptsTitle"
                               closeCommand="BS.GradleAddInitScripts.close();">
                        <c:url var="actionUrl" value="/admin/uploadInitScript.html"/>
                        <forms:multipartForm id="addInitScriptsForm"
                                             action="${actionUrl}"
                                             onsubmit="return BS.GradleAddInitScripts.validate();"
                                             targetIframe="hidden-iframe">
                            <div>
                                <table class="runnerFormTable">
                                    <tr>
                                        <th><label for="fileName">Name: </label></th>
                                        <td><input type="text" id="fileName" name="fileName" value=""/></td>
                                    </tr>
                                    <tr>
                                        <th>File: <l:star/></th>
                                        <td>
                                            <forms:file name="fileToUpload" size="28"/>
                                            <span id="uploadError" class="error hidden"></span>
                                        </td>
                                    </tr>
                                </table>
                            </div>
                            <div class="popupSaveButtonsBlock">
                                <forms:submit label="Save"/>
                                <forms:cancel onclick="BS.GradleAddInitScripts.close()" showdiscardchangesmessage="false"/>
                                <input type="hidden" id="projectId" name="project" value="${currentProject.externalId}"/>
                                <forms:saving id="saving"/>
                            </div>
                        </forms:multipartForm>
                    </bs:dialog>
                </jsp:attribute>
            </authz:authorize>

            <script type="text/javascript">
                BS.GradleAddInitScripts.setFiles([<c:forEach var="script" items="${scripts}">'${script}', </c:forEach>]);
                BS.GradleAddInitScripts.prepareFileUpload();
            </script>

            <c:if test="${not empty scripts}">
                <table class="highlightable parametersTable" style="width: 100%">
                    <tr>
                        <th colspan="3">Script Name</th>
                    </tr>
                    <%--@elvariable id="usage" type="java.util.Map<String, List<SBuildType>>"--%>
                    <c:forEach var="script" items="${scripts}">
                        <c:url var="fileUrl" value="/admin/editProject.html?tab=gradleInitScripts&projectId=${currentProject.externalId}&file=${script}"/>
                        <c:set var="onclick" value="document.location.href = '${fileUrl}';"/>
                        <c:url var="usagesReportUrl" value="/admin/editProject.html?tab=usagesReport&projectId=${currentProject.externalId}&scriptName=${script}"/>
                        <c:set var="usages" value='<a href="${usagesReportUrl}">View usages</a>'/>
                        <tr>
                            <authz:authorize projectId="${currentProject.projectId}" allPermissions="EDIT_PROJECT">
                                <jsp:attribute name="ifAccessGranted">
                                    <td class="highlight beforeActions" onclick="${onclick}">
                                        <span style="float:right; padding-right: 2em">${usages}</span>
                                        <c:out value="${script}"/>
                                    </td>
                                    <td class="edit highlight"><a href="#" onclick="${onclick}">View</a></td>
                                    <td class="edit highlight">
                                        <a href="#" onclick="BS.GradleInitScripts.deleteScript('${currentProject.projectId}', '${script}')">Delete</a>
                                    </td>
                                </jsp:attribute>
                                <jsp:attribute name="ifAccessDenied">
                                    <td class="beforeActions">
                                        <span style="float:right; padding-right: 2em">${usages}</span>
                                        <c:out value="${script}"/>
                                    </td>
                                    <td class="edit"><div class="clearfix" style="color: #737577"><i>View</i></div></td>
                                    <td class="edit"><div class="clearfix" style="color: #737577"><i>Delete</i></div></td>
                                </jsp:attribute>
                            </authz:authorize>
                        </tr>
                    </c:forEach>
                </table>
            </c:if>
        </c:otherwise>
    </c:choose>
</div>
