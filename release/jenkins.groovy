pipeline {
  agent any
  stages {
    stage('repository clone') {
      steps {
        git branch: 'release', credentialsId: 'gitlab', url: 'https://lab.ssafy.com/s08-webmobile1-sub2/S08P12A705.git'
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
    stage('stop web service') {
      steps {
        script {
          try {
            sh 'docker rm -f backend && docker rm -f client'
        }catch (e) {
            echo 'no container running'
          }
        }
      }
    }
    stage('parallel build') {
      parallel {
        stage('client build') {
          steps {
            sh 'docker build -f client/Dockerfile -t nowgnas/agora:client .'
          }
        }
        stage('server build') {
          steps {
            sh 'docker build -f server/Dockerfile -t nowgnas/agora:backend .'
          }
        }
      }
    }
    stage('push build images') {
      steps {
        sh 'docker login -u nowgnas -p dltkddnjs!!'
        sh 'docker push nowgnas/agora:backend'
        sh 'docker push nowgnas/agora:client '
      }
    }
  }
}
