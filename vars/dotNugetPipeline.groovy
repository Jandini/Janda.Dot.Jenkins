package janda.dot.jenkins

def call() {

    node("matt10") {
        
        try {
            
            Common.running()

            stage('Init') {
                Common.init()
            }
            
            stage('Checkout') {
                Common.checkout()
            }

            stage('Restore') {
                Common.dot('restore')
            }

            stage('Build') {
                Common.dot('build')
            }

            stage('Test') {
                Common.dot('test')
            }

            stage('Pack') {
                Common.dot('pack')
            }
                    
            stage('Cleanup') {
                Common.cleanup();
            } 

            stage ('Package') {
                Common.done()	
            }
        
            Common.success()
        }
        catch (e) {
            Common.failed(e)
        }
    }   
}