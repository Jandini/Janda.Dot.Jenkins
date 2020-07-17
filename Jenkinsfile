library 'Janda.Dot.Jenkins@develop'

node("master") {

    try 
    {      
        stage('Init') {
            Common.checkoutBranch();              
            Common.updateStatus('running');
        }
        
        stage('Seed') {
           
            Seeder.gitLabGetProjects(Seeder.GITLAB_URL, Seeder.GITLAB_TOKEN).each {
                println("Project: ${it.name}; Path: ${it.path}; Url: ${it.http_url_to_repo}")
            
                // check if Jenkinsfile exist in master or develop branch
                if (Seeder.gitLabHasJenkinsfile(it, Seeder.GITLAB_TOKEN) || Seeder.gitLabHasJenkinsfile(it, Seeder.GITLAB_TOKEN, "develop")) {                    
                    println "Jenkinsfile found."

                    def result = Seeder.jenkinsCreateMultiBranchProject(Seeder.JENKINS_URL, it.name, it.path, it.http_url_to_repo)
                    println("Result: ${result}");
                    
                    if (result.equals(200)) {
                        
                    }

                    def hooks = Seeder.gitLabGetWebHooks(it, Seeder.GITLAB_TOKEN)
                    
                    if (hooks.size() == 0) {  
                        println "No GitLab project webhook found."
                        def json = getGitlabWebHookJson(it, Seeder.JENKINS_URL)                    
                        println "Creating new GitLab hook"
                        println json

                        Seeder.gitLabCreateWebHook(it, Seeder.GITLAB_TOKEN, Seeder.JENKINS_URL) 

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
        
        Common.updateStatus('success');
    }
    catch (e) {
        Common.updateStatus('failed');
        throw e
    }
    finally {
        deleteDir()
    }
}