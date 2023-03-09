pipeline {
  agent any
  stages {
    stage('clone git lab repository') {
      steps {
        git branch: 'main', credentialsId: 'gitlab', url: 'https://lab.ssafy.com/s08-bigdata-recom-sub2/S08P22A504.git'
      }
    }
    stage('run dev server') {
      steps {
        script {
          try {
            echo 'start dev server'
            
          } catch (e) {
            echo 'development server run fail'
          }
        }
      }
    }
    stage('release jenkins job finish') {
      steps {
        mattermostSend(
          color: "good",
          message: "[RELEASE JENKINS JOB SUCCESS]: ${env.JOB_NAME} | #${env.BUILD_NUMBER} | URL: ${env.BUILD_URL} link to build"
        )
      }
    }
  }
}