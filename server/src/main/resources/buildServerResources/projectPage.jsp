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

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="admin" tagdir="/WEB-INF/tags/admin" %>
<%@ include file="/include-internal.jsp" %>

<jsp:useBean id="currentProject" type="jetbrains.buildServer.serverSide.SProject" scope="request"/>

<%--@elvariable id="scripts" type="java.util.Map"--%>
<div class="section noMargin">
    <h2 class="noBorder">Gradle Init Scripts</h2>
    <bs:smallNote>In this section you can manage the Gradle initialization scripts to reuse them within the project</bs:smallNote>

    <bs:messages key="initScriptsMessage"/>

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
            <c:if test="${not empty inspections}">
                <%@include file="health/report.jspf"%>
            </c:if>
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
            <script type="text/javascript">
                BS.GradleAddInitScripts.setFiles([<c:forEach var="script" items="${scripts[currentProject]}">'${script}', </c:forEach>]);
                BS.GradleAddInitScripts.prepareFileUpload();
            </script>

            <c:if test="${empty scripts[currentProject]}">
                <p>There are no Gradle initialization scripts defined in the current project.</p>
            </c:if>
            <c:if test="${not empty scripts[currentProject]}">
                <p style="margin-top: 2em">Gradle initialization scripts defined in the current project</p>
                <table class="highlightable parametersTable" style="width: 100%">
                    <tr>
                        <th style="width: 45%">Script Name</th>
                        <th colspan="3">Usage</th>
                    </tr>
                    <%--@elvariable id="usage" type="java.util.Map<String, List<SBuildType>>"--%>
                    <c:forEach var="script" items="${scripts[currentProject]}">
                        <c:url var="fileUrl" value="/admin/editProject.html?projectId=${currentProject.externalId}&tab=gradleInitScripts&file=${script}"/>
                        <c:set var="onclick" value="document.location.href = '${fileUrl}';"/>
                        <tr>
                            <td class="highlight" onclick="${onclick}"><c:out value="${script}"/></td>
                            <td class="highlight" onclick="${onclick}" style="white-space: nowrap">
                                <%@ include file="scriptUsage.jspf" %>
                            </td>
                            <td class="edit highlight"><a href="#" onclick="${onclick}">View</a></td>
                            <td class="edit">
                                <a href="#" onclick="BS.GradleInitScripts.deleteScript('${currentProject.projectId}', '${script}')">Delete</a>
                            </td>
                        </tr>
                    </c:forEach>
                </table>
            </c:if>

            <c:forEach items="${scripts}" var="projects">
                <%--@elvariable id="project" type="jetbrains.buildServer.serverSide.SProject"--%>
                <c:set var="project" value="${projects.key}"/>
                <c:if test="${project ne currentProject}">
                    <p style="margin-top: 2em">Scripts inherited from
                        <admin:editProjectLink projectId="${project.externalId}" addToUrl="&tab=gradleInitScripts">
                            <c:out value="${project.fullName}"/>
                        </admin:editProjectLink>
                    </p>
                    <table class="parametersTable" style="width: 100%">
                        <tr>
                            <th style="width: 45%">Script Name</th>
                            <th colspan="1">Usage</th>
                        </tr>
                        <c:forEach var="script" items="${scripts[project]}">
                            <tr>
                                <td>
                                    <c:out value="${script}"/>
                                </td>
                                <td>
                                    <%@ include file="scriptUsage.jspf" %>
                                </td>
                            </tr>
                        </c:forEach>
                    </table>
                </c:if>
            </c:forEach>
        </c:otherwise>
    </c:choose>
</div>
