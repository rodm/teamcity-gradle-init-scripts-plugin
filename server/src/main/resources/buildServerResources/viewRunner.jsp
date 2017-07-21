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

<%@include file="/include.jsp"%>
<%@ page import="com.github.rodm.teamcity.gradle.scripts.GradleInitScriptsPlugin" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>

<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>

<c:set var="INIT_SCRIPT_NAME_PARAMETER" value="<%=GradleInitScriptsPlugin.INIT_SCRIPT_NAME_PARAMETER%>"/>

<div class="parameter">
    Gradle initialization script: <props:displayValue name="${INIT_SCRIPT_NAME_PARAMETER}" emptyValue="not specified"/>
</div>
