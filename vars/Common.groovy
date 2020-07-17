package janda.dot
import groovy.json.JsonSlurperClassic

class Global {
   static Object gitVersion;
}

properties([[$class: 'GitLabConnectionProperty', gitLabConnection: 'NAS']])

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

def Object checkoutBranch() {

    checkout([
        $class: 'GitSCM',
        branches: scm.branches,
        extensions: [[$class: 'CloneOption', noTags: false, shallow: false, depth: 0, reference: '']],
        userRemoteConfigs: scm.userRemoteConfigs,
        ])

    Global.gitVersion = getGitVersion();
    currentBuild.description = gitVersion.InformationalVersion
    return gitVersion;
}


def void buildInit() {
    milestone Integer.parseInt(env.BUILD_ID)
    deleteDir()  
}


def void buildCleanup() {
    milestone()
    deleteDir()  
}
