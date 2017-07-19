/**
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
 */

BS.GradleAddInitScripts = OO.extend(BS.AbstractWebForm, OO.extend(BS.AbstractModalDialog, OO.extend(BS.FileBrowse, {
    getContainer: function () {
        return $('addInitScripts');
    },

    formElement: function() {
        return $('addInitScriptsForm');
    },

    refresh: function() {
        BS.reload();
    }
})));

BS.GradleInitScripts = {

    actionsUrl: window['base_uri'] + "/admin/initScriptsActions.html",

    deleteScript: function(projectId, scriptName) {
        if (confirm('Are you sure you want to delete this script?')) {
            BS.ajaxRequest(this.actionsUrl, {
                parameters: Object.toQueryString({action: 'deleteScript', projectId: projectId, name: scriptName}),
                onComplete: function(transport) {
                    window.location.reload();
                }
            });
        }
    },

    errorDetailsVisible: false,

    toggleErrorDisplay: function() {
        if (this.errorDetailsVisible) {
            $j("#errorDetailsToggle").html("Show details...");
        } else {
            $j("#errorDetailsToggle").html("Hide");
        }
        this.errorDetailsVisible = !this.errorDetailsVisible;
        BS.Util.toggleVisible("errorDetails")
    }
};
