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


def gitLabHasJenkinsfile(Object gitLabProject, String privateToken, String branchName = "master") {
    
    def fileUrl = new URL("${gitLabProject._links.self}/repository/files/Jenkinsfile?ref=$branchName&private_token=$privateToken");
    
    println "Looking up Jenkinsfile in ${gitLabProject._links.self}"
    
    try
    {
        def jenkinsFile = new groovy.json.JsonSlurper().parse(fileUrl.newReader());
        println "Jenkinsfile found."
        return true
    }
    catch (FileNotFoundException e) {
        println "Jenkinsfile not found."
        return false
    }
}

def gitLabGetProjects(String gitLabUrl, String privateToken) {
    
   def projects = new URL("${gitLabUrl}/api/v4/projects?private_token=${privateToken}");
   return new groovy.json.JsonSlurper().parse(projects.newReader());
}


def createMultiBranchProject(String jenkinsUrl, String projectName, String projectPath, String projectGitUrl) {
    
    def post = new URL("${jenkinsUrl}/createItem?name=${projectPath}").openConnection()
    def xml = getWorkflowMultiBranchProjectXml(projectName, projectGitUrl)
    
    post.setRequestMethod("POST")
    post.setDoOutput(true)
    post.setRequestProperty("Content-Type", "text/xml")
    post.setRequestProperty("Authorization", "Basic ${"matt:112bd7be12a2ea1df43b2efb0bbd39b5d4".bytes.encodeBase64().toString()}");
    post.getOutputStream().write(xml.getBytes("UTF-8"));
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
                if (gitLabHasJenkinsfile(it, "B7f8DnDsNpFeF95pXFF9")) {
                    
                    println("Project: ${it.name}; Path: ${it.path}; Url: ${it.http_url_to_repo}")
                    def result = createMultiBranchProject("http://nas.home:8081", it.name, it.path, it.http_url_to_repo)
                    println("Result: ${result}");
                    
                    if (result.equals(200)) {
                        
                    }
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