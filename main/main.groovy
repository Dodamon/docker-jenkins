pipeline {
  agent any
  stages {
    stage('clone git lab repository') {
      steps {
        git branch: 'main', credentialsId: 'gitlab', url: 'https://lab.ssafy.com/s08-bigdata-recom-sub2/S08P22A504.git'
      }
    }
    stage('stop staging server') {
      steps {
        script {
          try {
            sh 'docker rm -f client server'
          } catch(e) {
            echo 'no running staging server'
          }
        }
      }
    }
    stage('run staging server') {
      steps {
        script {
          try {
            echo 'start staging server'
            sh 'docker login -u nowgnas -p dltkddnjs!!'
            sh 'cd /home/ubuntu/deploy && docker-compose up -d'
          } catch (e) {
            echo 'production server run fail'
            mattermostSend(
              color: "#DF2E38",
              message: "[RUN PRODUCTION SERVER FAIL]: ${env.JOB_NAME} | #${env.BUILD_NUMBER} | URL: ${env.BUILD_URL} link to build"
            )
          }
        }
      }
    }
    stage('release jenkins job finish') {
      steps {
        mattermostSend(
          color: "good",
          message: "[PRODUCTION JENKINS JOB SUCCESS]: ${env.JOB_NAME} | #${env.BUILD_NUMBER} | URL: ${env.BUILD_URL} link to build"
        )
      }
    }
  }
}