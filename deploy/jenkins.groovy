pipeline {
  agent any 
  stages {
    stage('stop web service') {
      steps {
        script {
          try {
            sh 'docker rm -f server && docker rm -f client'
        }catch (e) {
            echo 'no container running'
          }
        }
      }
    }
    stage('run docker compose') {
      steps {
        sh 'docker login -u nowgnas -p dltkddnjs!!'
        sh 'cd /home/ubuntu && docker-compose up -d'
      }
    }
    stage('remove unused images') {
      steps {
        script {
          try {
            sh 'docker images -qf dangling=true | xargs -I{} docker rmi {}'
        } catch (e) {
            echo 'no unused images'
          }
        }
      }
    }
  }
}