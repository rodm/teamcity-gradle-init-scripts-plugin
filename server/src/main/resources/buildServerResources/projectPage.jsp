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

<div class="section noMargin">
    <h2 class="noBorder">Gradle Init Scripts</h2>
    <bs:smallNote>In this section you can manage the Gradle initialization scripts to reuse them within the project</bs:smallNote>

    <div class="upload">
        <forms:addButton onclick="return BS.GradleAddInitScripts.show();">Upload init script file</forms:addButton>
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
        BS.GradleAddInitScripts.setFiles([<c:forEach var="script" items="${scripts}">'${script}',</c:forEach>]);
        BS.GradleAddInitScripts.prepareFileUpload();
    </script>

    <c:if test="${not empty scripts}">
        <table class="parametersTable" style="width: 100%">
            <tr>
                <th style="width: 45%">Script Name</th>
                <th colspan="1"></th>
            </tr>
            <c:forEach var="script" items="${scripts}">
                <tr>
                    <td>
                        <c:out value="${script}"/>
                    </td>
                    <td class="edit">
                        <a href="#" onclick="BS.GradleInitScripts.deleteScript('${currentProject.projectId}', '${script}')">Delete</a>
                    </td>
                </tr>
            </c:forEach>
        </table>
    </c:if>
</div>
