package janda.dot.jenkins
import groovy.json.JsonSlurperClassic

properties([[$class: 'GitLabConnectionProperty', gitLabConnection: 'NAS']])

class Global {
  static Object gitVersion;
}

def updateStatus(String status) {
    updateGitlabCommitStatus(state: status);
}

def dot(String command) {
    milestone()
    bat """
        call .${command}
        exit /b %ERRORLEVEL%
    """
}

def Object getGitVersion() {
  jsonText = bat(returnStdout: true, script: '@gitversion')
  println "${jsonText}"
  return new JsonSlurperClassic().parseText(jsonText)
}

def String getPackageFtpLinkText(String link, String text) {
  def projectName = env.JOB_NAME.substring(0, env.JOB_NAME.indexOf("/")).toLowerCase() 
  def ftpUri = "ftp://nas/builds/${projectName}/" + link
  return hudson.console.ModelHyperlinkNote.encodeTo(ftpUri, text);
}

def void getPackageLinks(Object gitVersion) {
  branch = getPackageFtpLinkText("${gitVersion.BranchName}", gitVersion.BranchName)
  version = getPackageFtpLinkText("${gitVersion.BranchName}/${gitVersion.InformationalVersion}", gitVersion.InformationalVersion)	
  println "Branch:    ${branch}\nVersion:   ${version}\n          "
}

def Object checkout() {

    checkout([
        $class: 'GitSCM',
        branches: scm.branches,
        extensions: [[$class: 'CloneOption', noTags: false, shallow: false, depth: 0, reference: '']],
        userRemoteConfigs: scm.userRemoteConfigs,
        ])

    Global.gitVersion = getGitVersion();
    currentBuild.description = Global.gitVersion.InformationalVersion
    return gitVersion;
}


def void init() {
    milestone Integer.parseInt(env.BUILD_ID)
    deleteDir()  
}


def void cleanup() {
    milestone()
    deleteDir()  
}

def void done() {
    getPackageLinks(Global.gitVersion)
}


def void running() {
    updateStatus('running')
}

def void success() {
    updateStatus('success')
}

def void failed(Exception e) {
    updateStatus('failed')
    throw e
}
