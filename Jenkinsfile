library 'Janda.Dot.Jenkins@develop'

node("master") {

    try 
    {      
        Common.running();

        stage('Init') {
            checkout scm       
        }
        
        stage('Seed') {
            Seeder.seed();            
        }
        
        Common.success();
    }
    catch (e) {
        Common.failed(e);
    }
    finally {
        deleteDir()
    }
}