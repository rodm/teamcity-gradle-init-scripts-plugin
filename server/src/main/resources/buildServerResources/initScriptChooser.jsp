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

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>

<props:selectProperty id="${chooserId}" name="${chooserName}" enableFilter="true" className="longField">
    <%--@elvariable id="missingScript" type="String"--%>
    <c:if test="${not empty missingScript}">
        <props:option value="${missingScript}"><c:out value="Missing script: ${missingScript}"/></props:option>
    </c:if>

    <props:option value="">-- Choose an initialization script --</props:option>
    <c:forEach var="projects" items="${scripts}">
        <c:set var="project" value="${projects.key}"/>
        <optgroup label="&ndash;&ndash; ${project.name} scripts &ndash;&ndash;">
            <c:forEach var="script" items="${scripts[project]}">
                <props:option value="${script}" className="user-depth-2">${script}</props:option>
            </c:forEach>
        </optgroup>
    </c:forEach>
</props:selectProperty>
