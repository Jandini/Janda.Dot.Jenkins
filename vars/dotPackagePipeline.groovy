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

            stage('Pack') {
                Common.dot('nuget --pack')
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