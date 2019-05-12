properties([[$class: 'GitLabConnectionProperty', gitLabConnection: 'NAS']])

def getWorkflowMultiBranchProjectXml(String displayName, String httpUrlToRepo, String credentialsId = "f38cce97-8302-4196-8e4b-677c26717dea" ) {
    
    return """<?xml version='1.1' encoding='UTF-8'?>
            <org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject plugin="workflow-multibranch@2.20">
              <actions/>
              <description></description>
              <displayName>${displayName}</displayName>
              <properties>
                <org.jenkinsci.plugins.pipeline.modeldefinition.config.FolderConfig plugin="pipeline-model-definition@1.3.4">
                  <dockerLabel></dockerLabel>
                  <registry plugin="docker-commons@1.13"/>
                </org.jenkinsci.plugins.pipeline.modeldefinition.config.FolderConfig>
              </properties>
              <folderViews class="jenkins.branch.MultiBranchProjectViewHolder" plugin="branch-api@2.1.2">
                <owner class="org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject" reference="../.."/>
              </folderViews>
              <healthMetrics>
                <com.cloudbees.hudson.plugins.folder.health.WorstChildHealthMetric plugin="cloudbees-folder@6.7">
                  <nonRecursive>false</nonRecursive>
                </com.cloudbees.hudson.plugins.folder.health.WorstChildHealthMetric>
              </healthMetrics>
              <icon class="jenkins.branch.MetadataActionFolderIcon" plugin="branch-api@2.1.2">
                <owner class="org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject" reference="../.."/>
              </icon>
              <orphanedItemStrategy class="com.cloudbees.hudson.plugins.folder.computed.DefaultOrphanedItemStrategy" plugin="cloudbees-folder@6.7">
                <pruneDeadBranches>true</pruneDeadBranches>
                <daysToKeep>1</daysToKeep>
                <numToKeep>-1</numToKeep>
              </orphanedItemStrategy>
              <triggers/>
              <disabled>false</disabled>
              <sources class="jenkins.branch.MultiBranchProject\$BranchSourceList" plugin="branch-api@2.1.2">
                <data>
                  <jenkins.branch.BranchSource>
                    <source class="jenkins.plugins.git.GitSCMSource" plugin="git@3.9.1">
                      <id>96745952-0838-4e37-84a5-1d0ad8b68da6</id>
                      <remote>${httpUrlToRepo}</remote>
                      <credentialsId>${credentialsId}</credentialsId>
                      <traits>
                        <jenkins.plugins.git.traits.BranchDiscoveryTrait/>
                      </traits>
                    </source>
                    <strategy class="jenkins.branch.DefaultBranchPropertyStrategy">
                      <properties class="empty-list"/>
                    </strategy>
                  </jenkins.branch.BranchSource>
                </data>
                <owner class="org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject" reference="../.."/>
              </sources>
              <factory class="org.jenkinsci.plugins.workflow.multibranch.WorkflowBranchProjectFactory">
                <owner class="org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject" reference="../.."/>
                <scriptPath>Jenkinsfile</scriptPath>
              </factory>
            </org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject>"""
}


def getGitlabWebHookJson(Object gitLabProject, String jenkinsUrl) {
  
    return groovy.json.JsonOutput.toJson([
        "url": "${jenkinsUrl}/project/${gitLabProject.path}",
        "push_events": true,
        "tag_push_events": true,
        "merge_requests_events": false,
        "repository_update_events": false,
        "enable_ssl_verification": false,
        "project_id": "${gitLabProject.id}",
        "issues_events": false,
        "confidential_issues_events": false,
        "note_events": false,
        "confidential_note_events": false,
        "pipeline_events": false,
        "wiki_page_events": false,
        "job_events": false,
        "push_events_branch_filter": ""     
     ])
}


def gitLabGetProjects(String gitLabUrl, String privateToken) {
    
   def projects = new URL("${gitLabUrl}/api/v4/projects?private_token=${privateToken}");
   return new groovy.json.JsonSlurper().parse(projects.newReader());
}


def gitLabHasJenkinsfile(Object gitLabProject, String privateToken, String branchName = "master") {
    
    def fileUrl = new URL("${gitLabProject._links.self}/repository/files/Jenkinsfile?ref=$branchName&private_token=$privateToken");
    
    try {
        def jenkinsFile = new groovy.json.JsonSlurper().parse(fileUrl.newReader());
        return true
    }
    catch (FileNotFoundException e) {
        return false
    }
}

def gitLabGetWebHooks(Object gitLabProject, String privateToken) {
    
    def hooksUrl = new URL("${gitLabProject._links.self}/hooks?private_token=$privateToken");
    def hooks = new groovy.json.JsonSlurper().parse(hooksUrl.newReader());

    return hooks
}


def jenkinsCreateMultiBranchProject(String jenkinsUrl, String projectName, String projectPath, String projectGitUrl) {
    
    def post = new URL("${jenkinsUrl}/createItem?name=${projectPath}").openConnection()
    def xml = getWorkflowMultiBranchProjectXml(projectName, projectGitUrl)
    
    post.setRequestMethod("POST")
    post.setDoOutput(true)
    post.setRequestProperty("Content-Type", "text/xml")
    post.setRequestProperty("Authorization", "Basic ${"matt:112bd7be12a2ea1df43b2efb0bbd39b5d4".bytes.encodeBase64().toString()}");
    post.getOutputStream().write(xml.getBytes("UTF-8"));
    return post.getResponseCode();
}


def gitLabCreateWebHook(Object gitLabProject, String privateToken, String jenkinsUrl) {
    
    def post = new URL("${gitLabProject._links.self}/hooks?private_token=$privateToken").openConnection()
    def json = getGitlabWebHookJson(gitLabProject, jenkinsUrl)
    
    post.setRequestMethod("POST")
    post.setDoOutput(true)
    post.setRequestProperty("Content-Type", "application/json")
    post.getOutputStream().write(json.getBytes("UTF-8"));
    return post.getResponseCode();
}


node("master") {

    try 
    {        
        stage('init') {
            checkout scm
            updateGitlabCommitStatus(state: 'running');
        }
        
        stage('seed') {
           
            gitLabGetProjects("http://nas.home", "B7f8DnDsNpFeF95pXFF9").each {
                println("Project: ${it.name}; Path: ${it.path}; Url: ${it.http_url_to_repo}")
            
                if (gitLabHasJenkinsfile(it, "B7f8DnDsNpFeF95pXFF9")) {                    
                    println "Jenkinsfile found."

                    def result = jenkinsCreateMultiBranchProject("http://nas.home:8081", it.name, it.path, it.http_url_to_repo)
                    println("Result: ${result}");
                    
                    if (result.equals(200)) {
                        
                    }

                    def hooks = gitLabGetWebHooks(it, "B7f8DnDsNpFeF95pXFF9")
                    
                    if (hooks.size() == 0) {  
                        println "No GitLab project webhook found."
                        def json = getGitlabWebHookJson(it, "http://nas.home:8081")                    
                        println "Creating new GitLab hook"
                        println json

                        gitLabCreateWebHook(it, "B7f8DnDsNpFeF95pXFF9", "http://nas.home:8081") 

                    }
                    else {
                        println "Existing hooks:"
                        println (hooks)
                    }
 
                }  
                else {
                    println "Jenkinsfile not found."
                }
            }
        }
        
        updateGitlabCommitStatus(state: 'success');
    }
    catch (e) {
        updateGitlabCommitStatus(state: 'failed');
        throw e
    }
    finally {
        deleteDir()
    }
}