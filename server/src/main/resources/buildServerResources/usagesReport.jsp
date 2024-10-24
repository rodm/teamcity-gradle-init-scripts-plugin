<%--
  * Copyright 2024 Rod MacKenzie.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     https://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  --%>

<%@ include file="/include-internal.jsp" %>
<%--@elvariable id="currentProject" type="jetbrains.buildServer.serverSide.SProject"--%>
<%--@elvariable id="scriptName" type="java.lang.String"--%>
<%--@elvariable id="buildTypeUsages" type="java.util.List"--%>
<%--@elvariable id="templateUsages" type="java.util.List"--%>

<h2 class="noBorder"><c:out value="${scriptName}"/> Gradle Init Script</h2>

<c:if test="${empty buildTypeUsages and empty templateUsages}">
  <div class="usagesSection">This Gradle Init Script is not used.</div>
</c:if>

<c:if test="${not empty templateUsages}">
<div class="usagesSection">
  <div>
    Used in <b>${fn:length(templateUsages)}</b> Template<bs:s val="${fn:length(templateUsages)}"/>:
  </div>
  <ul>
    <c:forEach items="${templateUsages}" var="tpl" varStatus="pos">
      <li>
        <c:set var="canEdit" value="${afn:permissionGrantedForProject(tpl.project, 'EDIT_PROJECT')}"/>
        <c:choose>
          <c:when test="${canEdit}">
            <admin:editTemplateLink step="runType" templateId="${tpl.externalId}" ><c:out value="${tpl.fullName}"/></admin:editTemplateLink>
          </c:when>
          <c:otherwise><c:out value="${tpl.fullName}"/></c:otherwise>
        </c:choose>
      </li>
    </c:forEach>
  </ul>
</div>
</c:if>

<c:if test="${not empty buildTypeUsages}">
<div class="usagesSection">
  <div>
    Used in <b>${fn:length(buildTypeUsages)}</b> Build configuration<bs:s val="${fn:length(buildTypeUsages)}"/>:
  </div>
  <ul>
    <c:forEach items="${buildTypeUsages}" var="bt" varStatus="pos">
      <li>
        <c:set var="canEdit" value="${afn:permissionGrantedForBuildType(bt, 'EDIT_PROJECT')}"/>
        <c:choose>
          <c:when test="${canEdit}">
            <admin:editBuildTypeLinkFull step="runType" buildType="${bt}"/>
          </c:when>
          <c:otherwise><c:out value="${bt.fullName}"/></c:otherwise>
        </c:choose>
      </li>
    </c:forEach>
  </ul>
</div>
</c:if>
