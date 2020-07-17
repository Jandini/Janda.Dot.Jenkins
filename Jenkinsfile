library 'Janda.Dot.Jenkins@develop'

node("master") {

    try 
    {        
        stage('Init') {
            checkout([
                $class: 'GitSCM',
                branches: scm.branches,
                extensions: [[$class: 'CloneOption', noTags: false, shallow: false, depth: 0, reference: '']],
                userRemoteConfigs: scm.userRemoteConfigs,
            ])
              
            updateGitlabCommitStatus(state: 'running');
        }
        
        stage('Seed') {
           
            gitLabGetProjects(Seeder.GITLAB_URL, Seeder.GITLAB_TOKEN).each {
                println("Project: ${it.name}; Path: ${it.path}; Url: ${it.http_url_to_repo}")
            
                // check if Jenkinsfile exist in master or develop branch
                if (gitLabHasJenkinsfile(it, Seeder.GITLAB_TOKEN) || gitLabHasJenkinsfile(it, Seeder.GITLAB_TOKEN, "develop")) {                    
                    println "Jenkinsfile found."

                    def result = jenkinsCreateMultiBranchProject(Seeder.JENKINS_URL, it.name, it.path, it.http_url_to_repo)
                    println("Result: ${result}");
                    
                    if (result.equals(200)) {
                        
                    }

                    def hooks = gitLabGetWebHooks(it, Seeder.GITLAB_TOKEN)
                    
                    if (hooks.size() == 0) {  
                        println "No GitLab project webhook found."
                        def json = getGitlabWebHookJson(it, Seeder.JENKINS_URL)                    
                        println "Creating new GitLab hook"
                        println json

                        gitLabCreateWebHook(it, Seeder.GITLAB_TOKEN, Seeder.JENKINS_URL) 

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